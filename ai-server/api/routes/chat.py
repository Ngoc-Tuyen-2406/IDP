from fastapi import APIRouter

from schemas.chat import ChatRequest, ChatResponse
from services.chat_service import chat_service

router = APIRouter(prefix="/api/v1/chat", tags=["chat"])


@router.post("/ask", response_model=ChatResponse)
def ask_question(payload: ChatRequest) -> ChatResponse:
    return chat_service.answer(payload)
