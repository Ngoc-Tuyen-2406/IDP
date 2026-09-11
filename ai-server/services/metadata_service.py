from __future__ import annotations

import re
from typing import Iterable

from models.registry import model_registry
from schemas.common import MetadataFieldPayload, ModelPayload
from schemas.pipeline import MetadataResponse, OcrPagePayload, OcrResponse, PipelineRequest
from services.ocr_service import ocr_service
from utils.text_utils import fold_text


class MetadataService:
    """Extract reviewable contract metadata from text produced by OCR."""

    def extract(self, payload: PipelineRequest) -> MetadataResponse:
        """Run OCR for the standalone metadata endpoint, including scanned PDFs."""
        return self.extract_from_ocr(ocr_service.run(payload))

    def extract_from_ocr(self, ocr: OcrResponse) -> MetadataResponse:
        return self.extract_from_pages(ocr.pages)

    def extract_from_pages(self, pages: Iterable[OcrPagePayload]) -> MetadataResponse:
        return self.extract_from_text("\n\n".join(page.text for page in pages if page.text))

    def extract_from_text(self, text: str) -> MetadataResponse:
        normalized = fold_text(text)
        fields = [
            self._field(
                "contract_number",
                "string",
                text,
                normalized,
                (
                    r"(?:hop\s*dong|contract)\s*(?:so|no\.?|number)\s*[:#-]?\s*([a-z0-9][a-z0-9._/-]{2,})",
                    r"^\s*(?:so|no\.?|number)\s*[:#-]\s*([a-z0-9][a-z0-9._/-]{2,})",
                ),
                0.93,
            ),
            self._field(
                "signed_date",
                "date",
                text,
                normalized,
                (rf"(?:ngay\s*(?:ky|lap)|signed\s*date)\D{{0,24}}{_DATE_VALUE}",),
                0.9,
            ),
            self._field(
                "effective_date",
                "date",
                text,
                normalized,
                (rf"(?:ngay\s*hieu\s*luc|effective\s*date)\D{{0,24}}{_DATE_VALUE}",),
                0.88,
            ),
            self._field(
                "expired_date",
                "date",
                text,
                normalized,
                (rf"(?:ngay\s*(?:het\s*han|ket\s*thuc)|expiration\s*date|expiry\s*date|termination\s*date)\D{{0,24}}{_DATE_VALUE}",),
                0.86,
            ),
            self._field(
                "party_a",
                "organization",
                text,
                normalized,
                (r"(?:ben|party)\s*a\s*(?:\([^\n)]{0,60}\))?\s*[:\-]\s*([^\n]{3,160})",),
                0.82,
            ),
            self._field(
                "party_b",
                "organization",
                text,
                normalized,
                (r"(?:ben|party)\s*b\s*(?:\([^\n)]{0,60}\))?\s*[:\-]\s*([^\n]{3,160})",),
                0.82,
            ),
            self._field(
                "tax_code",
                "string",
                text,
                normalized,
                (r"(?:ma\s*so\s*thue|mst|tax\s*(?:code|id))\s*[:#\-]?\s*([0-9]{10,14}(?:-[0-9]{3})?)",),
                0.9,
            ),
            self._field(
                "total_value",
                "currency",
                text,
                normalized,
                (
                    r"(?:tong\s*gia\s*tri(?:\s*hop\s*dong)?|gia\s*tri\s*hop\s*dong|total\s*value)\s*(?:la|is)?\s*[:\-]?\s*([0-9][0-9., ]{2,40}(?:\s*(?:vnd|usd|eur|dong))?)",
                ),
                0.84,
            ),
        ]
        return MetadataResponse(
            fields=[field for field in fields if field.current_value],
            model=self._model_payload(),
        )

    def _field(
        self,
        field_name: str,
        field_type: str,
        source_text: str,
        normalized_text: str,
        patterns: tuple[str, ...],
        confidence: float,
    ) -> MetadataFieldPayload:
        value = _first_value(source_text, normalized_text, patterns)
        return MetadataFieldPayload(
            field_name=field_name,
            field_type=field_type,
            original_value=value,
            current_value=value,
            confidence=confidence if value else None,
        )

    @staticmethod
    def _model_payload() -> ModelPayload:
        descriptor = model_registry.get_first_by_type("metadata")
        if descriptor:
            return ModelPayload(
                model_name=descriptor.model_name,
                model_type=descriptor.model_type,
                version=descriptor.version,
                description=descriptor.description,
            )
        return ModelPayload(
            model_name="idp-metadata-rules",
            model_type="metadata",
            version="1.0",
            description="Vietnamese and English regex extraction over PaddleOCR text; results require user review.",
        )


_DATE_VALUE = r"([0-3]?\d\s*(?:[/.-]\s*[01]?\d\s*(?:[/.-]\s*(?:19|20)?\d{2})|thang\s*[01]?\d\s*nam\s*(?:19|20)\d{2}))"


def _first_value(source_text: str, normalized_text: str, patterns: tuple[str, ...]) -> str | None:
    for pattern in patterns:
        match = re.search(pattern, normalized_text, flags=re.IGNORECASE | re.MULTILINE)
        if not match:
            continue
        start, end = match.span(1)
        value = re.sub(r"\s+", " ", source_text[start:end]).strip(" :-;,.")
        if value:
            return value
    return None


metadata_service = MetadataService()
