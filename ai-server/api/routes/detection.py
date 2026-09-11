from fastapi import APIRouter, HTTPException, status

from schemas.pipeline import DetectionResponse, PipelineRequest
from services.detection_service import detection_service

router = APIRouter(prefix="/api/v1/detection", tags=["detection"])


@router.post("/run", response_model=DetectionResponse)
def run_detection(payload: PipelineRequest) -> DetectionResponse:
    return detection_service.run_signature(payload)


@router.post("/regions/experimental", response_model=DetectionResponse)
def run_experimental_region_detection(payload: PipelineRequest) -> DetectionResponse:
    try:
        return detection_service.run_contract_regions_experimental(payload)
    except FileNotFoundError as error:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(error)) from error
