from fastapi import FastAPI

from api.routes import chat, clauses, detection, embedding, health, metadata, ocr, pipeline, risk, summary
from core.config import settings
from core.logging import configure_logging

configure_logging()

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="IDP AI server for OCR, metadata extraction, summary, risk analysis, embeddings, and chat.",
)

app.include_router(health.router)
app.include_router(pipeline.router)
app.include_router(ocr.router)
app.include_router(detection.router)
app.include_router(metadata.router)
app.include_router(clauses.router)
app.include_router(summary.router)
app.include_router(risk.router)
app.include_router(embedding.router)
app.include_router(chat.router)
