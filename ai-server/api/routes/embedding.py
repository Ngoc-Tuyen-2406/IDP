from fastapi import APIRouter

from schemas.pipeline import EmbeddingRequest, EmbeddingResponse
from services.embedding_service import embedding_service

router = APIRouter(prefix="/api/v1/embedding", tags=["embedding"])


@router.post("/create", response_model=EmbeddingResponse)
def create_embedding(payload: EmbeddingRequest) -> EmbeddingResponse:
    return embedding_service.create(payload)
