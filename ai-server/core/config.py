from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import os


AI_SERVER_ROOT = Path(__file__).resolve().parents[1]


def _resolve_runtime_path(environment_name: str, default_relative_path: str) -> Path:
    configured_path = Path(os.getenv(environment_name, default_relative_path))
    if not configured_path.is_absolute():
        configured_path = AI_SERVER_ROOT / configured_path
    return configured_path.resolve()


@dataclass(slots=True)
class Settings:
    app_name: str = os.getenv("AI_APP_NAME", "idp-ai-server")
    app_version: str = os.getenv("AI_APP_VERSION", "v1")
    host: str = os.getenv("AI_HOST", "0.0.0.0")
    port: int = int(os.getenv("AI_PORT", "8000"))
    model_root: Path = _resolve_runtime_path("AI_MODEL_ROOT", "trained_models")
    upload_root: Path = _resolve_runtime_path("AI_UPLOAD_ROOT", "uploads")
    output_root: Path = _resolve_runtime_path("AI_OUTPUT_ROOT", "outputs")
    log_root: Path = _resolve_runtime_path("AI_LOG_ROOT", "logs")
    rag_index_root: Path = _resolve_runtime_path("AI_RAG_INDEX_ROOT", "outputs/vector_indexes")
    rag_embedding_backend: str = os.getenv("AI_RAG_EMBEDDING_BACKEND", "lexical")
    rag_chat_backend: str = os.getenv("AI_RAG_CHAT_BACKEND", "extractive")
    rag_ollama_base_url: str = os.getenv("AI_RAG_OLLAMA_BASE_URL", "http://localhost:11434")
    rag_ollama_embedding_model: str = os.getenv("AI_RAG_OLLAMA_EMBEDDING_MODEL", "embeddinggemma")
    rag_ollama_chat_model: str = os.getenv("AI_RAG_OLLAMA_CHAT_MODEL", "qwen2.5:3b")
    rag_request_timeout_seconds: int = int(os.getenv("AI_RAG_REQUEST_TIMEOUT_SECONDS", "90"))
    rag_hash_dimension: int = int(os.getenv("AI_RAG_HASH_DIMENSION", "768"))


settings = Settings()
