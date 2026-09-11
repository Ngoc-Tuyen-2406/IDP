from fastapi import APIRouter

from schemas.pipeline import OcrResponse, PipelineRequest
from services.ocr_service import ocr_service

router = APIRouter(prefix="/api/v1/ocr", tags=["ocr"])


@router.post("/run", response_model=OcrResponse)
def run_ocr(payload: PipelineRequest) -> OcrResponse:
    return ocr_service.run(payload)
