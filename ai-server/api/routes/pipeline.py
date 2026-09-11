from fastapi import APIRouter

from schemas.pipeline import FullPipelineResponse, PipelineRequest
from services.pipeline_service import pipeline_service

router = APIRouter(prefix="/api/v1/pipeline", tags=["pipeline"])


@router.post("/process", response_model=FullPipelineResponse)
def process_pipeline(payload: PipelineRequest) -> FullPipelineResponse:
    return pipeline_service.process(payload)
