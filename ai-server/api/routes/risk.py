from fastapi import APIRouter

from schemas.pipeline import RiskRequest, RiskResponse
from services.risk_service import risk_service

router = APIRouter(prefix="/api/v1/risk", tags=["risk"])


@router.post("/analyze", response_model=RiskResponse)
def analyze_risk(payload: RiskRequest) -> RiskResponse:
    return risk_service.analyze(payload)
