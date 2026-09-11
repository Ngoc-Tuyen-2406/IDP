from __future__ import annotations

from schemas.pipeline import EmbeddingRequest, EmbeddingResponse
from services.rag_service import rag_service


class EmbeddingService:
    def create(self, payload: EmbeddingRequest) -> EmbeddingResponse:
        artifact = rag_service.index_document(payload.text)
        return EmbeddingResponse(
            vector_index=artifact.vector_index,
            chunk_count=len(artifact.chunks),
            chunks=artifact.chunks,
            model=artifact.model,
        )


embedding_service = EmbeddingService()
