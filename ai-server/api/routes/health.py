from fastapi import APIRouter

from core.config import settings

router = APIRouter(tags=["health"])


@router.get("/api/v1/health")
def health_check() -> dict[str, str]:
    return {"status": "UP", "service": settings.app_name, "version": settings.app_version}
