from __future__ import annotations

from uuid import UUID

from pydantic import Field

from schemas.base import CamelModel


class ChatRequest(CamelModel):
    question: str
    text: str
    summary: str | None = None
    conversation_id: UUID | None = None


class ChatResponse(CamelModel):
    answer: str
    response_time_ms: int
    conversation_id: UUID
    source_chunk_ids: list[int] = Field(default_factory=list)
