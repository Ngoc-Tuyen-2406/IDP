from fastapi import APIRouter

from schemas.pipeline import MetadataResponse, PipelineRequest
from services.metadata_service import metadata_service

router = APIRouter(prefix="/api/v1/metadata", tags=["metadata"])


@router.post("/extract", response_model=MetadataResponse)
def extract_metadata(payload: PipelineRequest) -> MetadataResponse:
    return metadata_service.extract(payload)
