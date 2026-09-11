from __future__ import annotations

from typing import Any

from pydantic import Field

from schemas.base import CamelModel


class ModelPayload(CamelModel):
    model_name: str
    model_type: str | None = None
    version: str | None = None
    description: str | None = None


class MetadataFieldPayload(CamelModel):
    field_name: str = Field(..., max_length=100)
    field_type: str | None = Field(default=None, max_length=50)
    original_value: Any = None
    current_value: Any = None
    confidence: float | None = None
    verified: bool = False
