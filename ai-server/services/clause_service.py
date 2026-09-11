from __future__ import annotations

import re
from dataclasses import dataclass
from typing import Iterable

from models.registry import model_registry
from schemas.common import ModelPayload
from schemas.pipeline import ClausePayload, ClauseResponse, OcrPagePayload, OcrResponse, PipelineRequest
from services.ocr_service import ocr_service
from utils.text_utils import fold_text, split_lines


@dataclass(frozen=True, slots=True)
class ClauseRule:
    clause_type: str
    title: str
    keywords: tuple[str, ...]


_RULES = (
    ClauseRule("confidentiality", "Confidentiality", ("bao mat", "thong tin mat", "confidential")),
    ClauseRule("payment", "Payment", ("thanh toan", "gia tri hop dong", "payment")),
    ClauseRule("penalty", "Penalty", ("phat vi pham", "penalty", "boi thuong")),
    ClauseRule("termination", "Termination", ("cham dut", "termination", "terminate")),
    ClauseRule("renewal", "Renewal", ("gia han", "renewal", "renew")),
    ClauseRule("dispute_resolution", "Dispute Resolution", ("giai quyet tranh chap", "tranh chap", "dispute")),
    ClauseRule("governing_law", "Governing Law", ("luat ap dung", "phap luat viet nam", "governing law")),
)

_SECTION_HEADING = re.compile(r"^\s*(?:(?:dieu|article)\s*)?\d+(?:\.\d+)*[.)]?\s+")


class ClauseService:
    """Locate contract clauses in OCR text and return their source context for review."""

    def extract(self, payload: PipelineRequest) -> ClauseResponse:
        return self.extract_from_ocr(ocr_service.run(payload))

    def extract_from_ocr(self, ocr: OcrResponse) -> ClauseResponse:
        clauses: list[ClausePayload] = []
        for page in ocr.pages:
            clauses.extend(self._extract_page(page))
        return ClauseResponse(clauses=_deduplicate(clauses), model=self._model_payload())

    def extract_from_pages(self, pages: Iterable[OcrPagePayload]) -> ClauseResponse:
        return self.extract_from_ocr(OcrResponse(pages=list(pages)))

    def extract_from_text(self, text: str) -> ClauseResponse:
        return self.extract_from_pages([OcrPagePayload(page_number=1, text=text)])

    def _extract_page(self, page: OcrPagePayload) -> list[ClausePayload]:
        lines = split_lines(page.text)
        folded_lines = [fold_text(line) for line in lines]
        clauses: list[ClausePayload] = []
        for rule in _RULES:
            match_index = next(
                (index for index, line in enumerate(folded_lines) if any(keyword in line for keyword in rule.keywords)),
                None,
            )
            if match_index is None:
                continue
            matched_keywords = [keyword for keyword in rule.keywords if keyword in folded_lines[match_index]]
            context = _section_context(lines, folded_lines, match_index)
            clauses.append(
                ClausePayload(
                    clause_type=rule.clause_type,
                    title=rule.title,
                    text=context,
                    matched_keywords=matched_keywords,
                    page_number=page.page_number,
                    confidence=0.9 if _SECTION_HEADING.match(folded_lines[match_index]) else 0.75,
                )
            )
        return clauses

    @staticmethod
    def _model_payload() -> ModelPayload:
        descriptor = model_registry.get_first_by_type("clause")
        if descriptor:
            return ModelPayload(
                model_name=descriptor.model_name,
                model_type=descriptor.model_type,
                version=descriptor.version,
                description=descriptor.description,
            )
        return ModelPayload(
            model_name="idp-clause-rules",
            model_type="clause",
            version="1.0",
            description="Accent-insensitive clause retrieval over PaddleOCR text; results require user review.",
        )


def _section_context(lines: list[str], folded_lines: list[str], start_index: int) -> str:
    end_index = min(len(lines), start_index + 8)
    for index in range(start_index + 1, len(lines)):
        if _SECTION_HEADING.match(folded_lines[index]):
            end_index = index
            break
    context_start = max(0, start_index - 1)
    return "\n".join(lines[context_start:end_index])[:1800]


def _deduplicate(clauses: list[ClausePayload]) -> list[ClausePayload]:
    unique: list[ClausePayload] = []
    seen: set[tuple[str, str]] = set()
    for clause in clauses:
        key = (clause.clause_type, fold_text(clause.text))
        if key not in seen:
            seen.add(key)
            unique.append(clause)
    return unique


clause_service = ClauseService()
