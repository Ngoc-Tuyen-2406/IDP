from fastapi import APIRouter

from schemas.pipeline import ClauseResponse, PipelineRequest
from services.clause_service import clause_service

router = APIRouter(prefix="/api/v1/clauses", tags=["clauses"])


@router.post("/extract", response_model=ClauseResponse)
def extract_clauses(payload: PipelineRequest) -> ClauseResponse:
    return clause_service.extract(payload)
