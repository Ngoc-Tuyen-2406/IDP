from __future__ import annotations

from schemas.pipeline import RiskRequest, RiskResponse
from models.registry import model_registry
from services.clause_service import clause_service


class RiskService:
    def analyze(self, payload: RiskRequest) -> RiskResponse:
        descriptor = model_registry.get_first_by_type("risk")
        clause_types = {clause.clause_type for clause in clause_service.extract_from_text(payload.text).clauses}
        score = 0.2
        findings: list[str] = []

        if "confidentiality" not in clause_types:
            findings.append("Confidentiality clause is not clearly detected.")
            score += 0.2
        if "payment" not in clause_types:
            findings.append("Payment clause is not clearly detected.")
            score += 0.2
        if "termination" not in clause_types:
            findings.append("Termination condition is not clearly detected.")
            score += 0.15
        if not any(field.field_name == "expired_date" for field in payload.metadata):
            findings.append("Expired date was not extracted.")
            score += 0.15

        level = "Low"
        if score >= 0.75:
            level = "Critical"
        elif score >= 0.6:
            level = "High"
        elif score >= 0.4:
            level = "Medium"

        return RiskResponse(
            risk_level=level,
            risk_score=round(score, 2),
            risk_summary="; ".join(findings) if findings else "No major risk signals were found from the extracted text.",
            recommendation="Review missing clauses and verify extracted dates before final approval.",
            risk_details={
                "findings": findings,
                "metadata_fields": [field.field_name for field in payload.metadata],
                "detected_clause_types": sorted(clause_types),
            },
            model=descriptor and {
                "model_name": descriptor.model_name,
                "model_type": descriptor.model_type,
                "version": descriptor.version,
                "description": descriptor.description,
            },
        )


risk_service = RiskService()
