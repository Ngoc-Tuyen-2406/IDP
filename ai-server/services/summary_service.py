from __future__ import annotations

from schemas.pipeline import SummaryRequest, SummaryResponse
from models.registry import model_registry


class SummaryService:
    def generate(self, payload: SummaryRequest) -> SummaryResponse:
        descriptor = model_registry.get_first_by_type("llm")
        highlights = [f"{field.field_name}: {field.current_value}" for field in payload.metadata[:6] if field.current_value]
        lines = [line.strip() for line in payload.text.splitlines() if line.strip()]
        summary = " ".join((highlights + lines[:6]))[:1200] or "No textual content could be extracted from the document."
        summary_json = {
            "highlights": highlights,
            "excerpt": lines[:6],
            "length": len(payload.text),
        }
        return SummaryResponse(
            summary=summary,
            summary_json=summary_json,
            model=descriptor and {
                "model_name": descriptor.model_name,
                "model_type": descriptor.model_type,
                "version": descriptor.version,
                "description": descriptor.description,
            },
        )


summary_service = SummaryService()
