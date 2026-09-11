from __future__ import annotations

from typing import Any

from pydantic import Field

from schemas.base import CamelModel
from schemas.common import MetadataFieldPayload, ModelPayload


class PipelineRequest(CamelModel):
    file_path: str
    file_name: str
    contract_id: int
    version_id: int


class OcrLinePayload(CamelModel):
    text: str
    confidence: float | None = None
    x_min: float
    y_min: float
    x_max: float
    y_max: float


class OcrPagePayload(CamelModel):
    page_number: int
    language: str | None = None
    engine: str | None = None
    text: str = ""
    confidence: float | None = None
    image_width: int | None = None
    image_height: int | None = None
    lines: list[OcrLinePayload] = Field(default_factory=list)


class OcrResponse(CamelModel):
    pages: list[OcrPagePayload]
    model: ModelPayload | None = None


class DetectionRegionPayload(CamelModel):
    page_number: int
    label: str
    x_min: float
    y_min: float
    x_max: float
    y_max: float
    confidence: float | None = None


class DetectionResponse(CamelModel):
    regions: list[DetectionRegionPayload]
    model: ModelPayload | None = None


class ClausePayload(CamelModel):
    clause_type: str
    title: str
    text: str
    matched_keywords: list[str] = Field(default_factory=list)
    page_number: int | None = None
    confidence: float | None = None


class ClauseResponse(CamelModel):
    clauses: list[ClausePayload]
    model: ModelPayload | None = None


class MetadataResponse(CamelModel):
    fields: list[MetadataFieldPayload]
    model: ModelPayload | None = None


class SummaryRequest(CamelModel):
    text: str
    metadata: list[MetadataFieldPayload] = Field(default_factory=list)


class SummaryResponse(CamelModel):
    summary: str
    summary_json: dict[str, Any] | None = None
    model: ModelPayload | None = None


class RiskRequest(CamelModel):
    text: str
    metadata: list[MetadataFieldPayload] = Field(default_factory=list)


class RiskResponse(CamelModel):
    risk_level: str
    risk_score: float | None = None
    risk_summary: str
    recommendation: str | None = None
    risk_details: dict[str, Any] | None = None
    model: ModelPayload | None = None


class EmbeddingChunkPayload(CamelModel):
    chunk_index: int
    chunk_text: str
    vector_id: str
    page_number: int | None = None


class EmbeddingRequest(CamelModel):
    text: str


class EmbeddingResponse(CamelModel):
    vector_index: str
    chunk_count: int
    chunks: list[EmbeddingChunkPayload]
    model: ModelPayload | None = None


class FullPipelineResponse(CamelModel):
    ocr: OcrResponse
    detection: DetectionResponse
    metadata: MetadataResponse
    clauses: ClauseResponse
    summary: SummaryResponse
    risk: RiskResponse
    embedding: EmbeddingResponse
