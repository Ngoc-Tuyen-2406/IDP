from __future__ import annotations

from datetime import datetime, timezone
from uuid import UUID, uuid4

from schemas.chat import ChatRequest, ChatResponse
from services.rag_service import rag_service


class ChatService:
    def answer(self, payload: ChatRequest) -> ChatResponse:
        started = datetime.now(tz=timezone.utc)
        rag_answer = rag_service.answer(payload.question, payload.text, payload.summary)

        finished = datetime.now(tz=timezone.utc)
        return ChatResponse(
            answer=rag_answer.answer,
            response_time_ms=int((finished - started).total_seconds() * 1000),
            conversation_id=payload.conversation_id or uuid4(),
            source_chunk_ids=rag_answer.source_chunk_ids,
        )


chat_service = ChatService()
