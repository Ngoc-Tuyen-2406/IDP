from __future__ import annotations

from schemas.pipeline import (
    EmbeddingRequest,
    FullPipelineResponse,
    PipelineRequest,
    RiskRequest,
    SummaryRequest,
)
from services.detection_service import detection_service
from services.clause_service import clause_service
from services.embedding_service import embedding_service
from services.metadata_service import metadata_service
from services.ocr_service import ocr_service
from services.risk_service import risk_service
from services.summary_service import summary_service


class PipelineService:
    def process(self, payload: PipelineRequest) -> FullPipelineResponse:
        ocr = ocr_service.run(payload)
        detection = detection_service.run(payload)
        metadata = metadata_service.extract_from_ocr(ocr)
        clauses = clause_service.extract_from_ocr(ocr)
        text = "\n\n".join(page.text for page in ocr.pages)
        summary = summary_service.generate(SummaryRequest(text=text, metadata=metadata.fields))
        risk = risk_service.analyze(RiskRequest(text=text, metadata=metadata.fields))
        embedding = embedding_service.create(EmbeddingRequest(text=text))
        return FullPipelineResponse(
            ocr=ocr,
            detection=detection,
            metadata=metadata,
            clauses=clauses,
            summary=summary,
            risk=risk,
            embedding=embedding,
        )


pipeline_service = PipelineService()
