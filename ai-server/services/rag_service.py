from __future__ import annotations

from dataclasses import dataclass
import hashlib
import json
import logging
from pathlib import Path
import re
from threading import Lock
from typing import Any

import numpy as np
import requests

from core.config import settings
from schemas.common import ModelPayload
from schemas.pipeline import EmbeddingChunkPayload
from utils.hash_utils import sha256_hex
from utils.text_utils import chunk_text, fold_text


logger = logging.getLogger(__name__)


@dataclass(frozen=True, slots=True)
class RagIndexArtifact:
    vector_index: str
    chunks: list[EmbeddingChunkPayload]
    model: ModelPayload


@dataclass(frozen=True, slots=True)
class RetrievalResult:
    chunk_index: int
    chunk_text: str
    score: float


@dataclass(frozen=True, slots=True)
class RagAnswer:
    answer: str
    source_chunk_ids: list[int]


class EmbeddingProvider:
    model: ModelPayload

    def embed(self, texts: list[str]) -> np.ndarray:
        raise NotImplementedError


class HashEmbeddingProvider(EmbeddingProvider):
    """Deterministic local fallback that indexes real OCR text without a remote model."""

    def __init__(self, dimension: int) -> None:
        self.dimension = dimension
        self.model = ModelPayload(
            model_name="idp-local-lexical-faiss",
            model_type="embedding",
            version="1.0",
            description="Local lexical vectors for development; configure Ollama for semantic embeddings.",
        )

    def embed(self, texts: list[str]) -> np.ndarray:
        vectors = np.zeros((len(texts), self.dimension), dtype=np.float32)
        for row, text in enumerate(texts):
            tokens = re.findall(r"[a-z0-9]{2,}", fold_text(text))
            terms = tokens + [f"{left}_{right}" for left, right in zip(tokens, tokens[1:])]
            for term in terms:
                digest = hashlib.blake2b(term.encode("utf-8"), digest_size=8).digest()
                number = int.from_bytes(digest, byteorder="little", signed=False)
                vectors[row, number % self.dimension] += 1.0 if number & 1 else -1.0
        return _normalize_vectors(vectors)


class OllamaEmbeddingProvider(EmbeddingProvider):
    def __init__(self) -> None:
        self.model = ModelPayload(
            model_name=settings.rag_ollama_embedding_model,
            model_type="embedding",
            version="ollama",
            description="Semantic embedding served by local Ollama.",
        )

    def embed(self, texts: list[str]) -> np.ndarray:
        response = requests.post(
            f"{settings.rag_ollama_base_url.rstrip('/')}/api/embed",
            json={"model": settings.rag_ollama_embedding_model, "input": texts, "truncate": True},
            timeout=settings.rag_request_timeout_seconds,
        )
        response.raise_for_status()
        embeddings = response.json().get("embeddings")
        if not isinstance(embeddings, list) or len(embeddings) != len(texts):
            raise RuntimeError("Ollama returned an invalid embedding payload.")
        return _normalize_vectors(np.asarray(embeddings, dtype=np.float32))


class RagService:
    def __init__(self, index_root: Path | None = None) -> None:
        self.index_root = index_root or settings.rag_index_root
        self.index_root.mkdir(parents=True, exist_ok=True)
        self._provider: EmbeddingProvider | None = None
        self._provider_lock = Lock()

    def index_document(self, text: str) -> RagIndexArtifact:
        chunks = chunk_text(text, chunk_size=700)
        vector_index = sha256_hex(text)[:32] if text.strip() else "empty-index"
        provider = self._get_provider()
        metadata_path = self._metadata_path(vector_index)

        if chunks and self._is_current_index(metadata_path, provider.model, text):
            return self._artifact_from_metadata(metadata_path)

        payloads = [
            EmbeddingChunkPayload(
                chunk_index=index,
                chunk_text=chunk,
                vector_id=sha256_hex(chunk)[:24],
                page_number=None,
            )
            for index, chunk in enumerate(chunks)
        ]
        self._save_index(vector_index, text, payloads, provider)
        return RagIndexArtifact(vector_index=vector_index, chunks=payloads, model=provider.model)

    def retrieve(self, text: str, question: str, limit: int = 3) -> list[RetrievalResult]:
        artifact = self.index_document(text)
        if not artifact.chunks or not question.strip():
            return []
        try:
            import faiss
        except ImportError as error:
            raise RuntimeError("faiss-cpu is required for RAG retrieval.") from error

        index = faiss.read_index(str(self._index_path(artifact.vector_index)))
        query = self._get_provider().embed([question])
        if query.shape[1] != index.d:
            raise RuntimeError("Embedding dimensions do not match the persisted FAISS index.")
        scores, indexes = index.search(np.ascontiguousarray(query), min(limit, len(artifact.chunks)))
        return [
            RetrievalResult(
                chunk_index=artifact.chunks[int(chunk_id)].chunk_index,
                chunk_text=artifact.chunks[int(chunk_id)].chunk_text,
                score=round(float(score), 4),
            )
            for score, chunk_id in zip(scores[0], indexes[0])
            if chunk_id >= 0
        ]

    def answer(self, question: str, text: str, summary: str | None) -> RagAnswer:
        matches = self.retrieve(text, question)
        if not matches:
            return RagAnswer(
                answer="Khong tim thay doan noi dung du de tra loi chinh xac trong hop dong.",
                source_chunk_ids=[],
            )
        answer = self._generate_with_ollama(question, matches, summary)
        if not answer:
            answer = self._extractive_answer(matches)
        return RagAnswer(answer=answer, source_chunk_ids=[item.chunk_index for item in matches])

    def _get_provider(self) -> EmbeddingProvider:
        if self._provider is not None:
            return self._provider
        with self._provider_lock:
            if self._provider is not None:
                return self._provider
            backend = settings.rag_embedding_backend.lower()
            if backend == "lexical":
                self._provider = HashEmbeddingProvider(settings.rag_hash_dimension)
            elif backend == "ollama":
                self._provider = OllamaEmbeddingProvider()
            else:
                raise ValueError("AI_RAG_EMBEDDING_BACKEND must be lexical or ollama.")
            logger.info("Using embedding provider: %s", self._provider.model.model_name)
            return self._provider

    def _save_index(
        self,
        vector_index: str,
        text: str,
        chunks: list[EmbeddingChunkPayload],
        provider: EmbeddingProvider,
    ) -> None:
        if not chunks:
            self._metadata_path(vector_index).write_text(
                json.dumps(self._metadata(vector_index, text, chunks, provider.model), ensure_ascii=False, indent=2),
                encoding="utf-8",
            )
            return
        try:
            import faiss
        except ImportError as error:
            raise RuntimeError("faiss-cpu is required to persist RAG indexes.") from error
        vectors = provider.embed([chunk.chunk_text for chunk in chunks])
        index = faiss.IndexFlatIP(vectors.shape[1])
        index.add(np.ascontiguousarray(vectors))
        faiss.write_index(index, str(self._index_path(vector_index)))
        self._metadata_path(vector_index).write_text(
            json.dumps(self._metadata(vector_index, text, chunks, provider.model), ensure_ascii=False, indent=2),
            encoding="utf-8",
        )

    def _is_current_index(self, metadata_path: Path, model: ModelPayload, text: str) -> bool:
        if not metadata_path.is_file():
            return False
        try:
            metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            return False
        return (
            metadata.get("source_hash") == sha256_hex(text)
            and metadata.get("model", {}).get("model_name") == model.model_name
            and self._index_path(metadata.get("vector_index", "")).is_file()
        )

    def _artifact_from_metadata(self, metadata_path: Path) -> RagIndexArtifact:
        metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        return RagIndexArtifact(
            vector_index=metadata["vector_index"],
            chunks=[EmbeddingChunkPayload(**chunk) for chunk in metadata.get("chunks", [])],
            model=ModelPayload(**metadata["model"]),
        )

    def _generate_with_ollama(
        self,
        question: str,
        matches: list[RetrievalResult],
        summary: str | None,
    ) -> str | None:
        backend = settings.rag_chat_backend.lower()
        if backend == "extractive":
            return None
        if backend != "ollama":
            raise ValueError("AI_RAG_CHAT_BACKEND must be extractive or ollama.")
        sources = "\n\n".join(f"[Chunk {item.chunk_index}]\n{item.chunk_text}" for item in matches)
        prompt = (
            "Tra loi bang tieng Viet dua duy nhat tren cac doan hop dong duoc cung cap. "
            "Neu thong tin khong co, noi ro khong tim thay. Khong tu suy dien. "
            "Ket thuc bang nguon [Chunk n].\n\n"
            f"Tom tat neu co:\n{summary or 'Khong co'}\n\nCau hoi: {question}\n\nNguon:\n{sources}"
        )
        response = requests.post(
            f"{settings.rag_ollama_base_url.rstrip('/')}/api/chat",
            json={
                "model": settings.rag_ollama_chat_model,
                "messages": [{"role": "user", "content": prompt}],
                "stream": False,
            },
            timeout=settings.rag_request_timeout_seconds,
        )
        response.raise_for_status()
        content = response.json().get("message", {}).get("content", "")
        return str(content).strip() or None

    @staticmethod
    def _extractive_answer(matches: list[RetrievalResult]) -> str:
        excerpts = "\n\n".join(f"[Chunk {item.chunk_index}] {item.chunk_text}" for item in matches)
        return "Che do truy xuat cuc bo: cac doan sau lien quan nhat den cau hoi.\n\n" + excerpts

    def _metadata_path(self, vector_index: str) -> Path:
        return self.index_root / f"{vector_index}.json"

    def _index_path(self, vector_index: str) -> Path:
        return self.index_root / f"{vector_index}.faiss"

    @staticmethod
    def _metadata(
        vector_index: str,
        text: str,
        chunks: list[EmbeddingChunkPayload],
        model: ModelPayload,
    ) -> dict[str, Any]:
        return {
            "schema_version": 1,
            "vector_index": vector_index,
            "source_hash": sha256_hex(text),
            "model": model.model_dump(),
            "chunks": [chunk.model_dump() for chunk in chunks],
        }


def _normalize_vectors(vectors: np.ndarray) -> np.ndarray:
    if vectors.ndim != 2:
        raise RuntimeError("Embedding provider must return a two-dimensional vector array.")
    norms = np.linalg.norm(vectors, axis=1, keepdims=True)
    norms[norms == 0] = 1.0
    return np.ascontiguousarray(vectors / norms, dtype=np.float32)


rag_service = RagService()
