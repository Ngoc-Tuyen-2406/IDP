from fastapi import APIRouter

from schemas.pipeline import SummaryRequest, SummaryResponse
from services.summary_service import summary_service

router = APIRouter(prefix="/api/v1/summary", tags=["summary"])


@router.post("/generate", response_model=SummaryResponse)
def generate_summary(payload: SummaryRequest) -> SummaryResponse:
    return summary_service.generate(payload)
