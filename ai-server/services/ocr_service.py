from __future__ import annotations

import json
import logging
import os
from pathlib import Path
from threading import Lock
from typing import Any, Iterable

from schemas.pipeline import OcrLinePayload, OcrPagePayload, OcrResponse, PipelineRequest


logger = logging.getLogger(__name__)

IMAGE_SUFFIXES = {".bmp", ".jpeg", ".jpg", ".png", ".tif", ".tiff", ".webp"}


class OCRService:
    """Run Vietnamese PaddleOCR over rendered PDF pages or source image files."""

    def __init__(self) -> None:
        self._engine: Any | None = None
        self._engine_lock = Lock()

    def run(self, payload: PipelineRequest) -> OcrResponse:
        source_path = Path(payload.file_path)
        if not source_path.is_file():
            raise FileNotFoundError(f"Input file not found: {source_path}")

        text_layer_pages = self._extract_pdf_text_layer(source_path)
        if text_layer_pages is not None:
            logger.info("Using embedded PDF text layer for %s.", source_path.name)
            return OcrResponse(
                pages=text_layer_pages,
                model={
                    "model_name": "pymupdf-text-layer",
                    "model_type": "ocr",
                    "version": "1.x",
                    "description": "Embedded PDF text extraction with normalized line coordinates.",
                },
            )

        pages = [
            self._recognize_page(self._preprocess(image), page_number)
            for page_number, image in self._load_page_images(source_path)
        ]

        return OcrResponse(
            pages=pages,
            model={
                "model_name": "paddleocr-vi",
                "model_type": "ocr",
                "version": "3.x",
                "description": "PaddleOCR Vietnamese document OCR with page-level text boxes.",
            },
        )

    @staticmethod
    def _extract_pdf_text_layer(source_path: Path) -> list[OcrPagePayload] | None:
        """Use selectable PDF text when every page contains a meaningful text layer."""
        if source_path.suffix.lower() != ".pdf":
            return None

        try:
            import fitz
        except ImportError:
            return None

        document = fitz.open(str(source_path))
        try:
            pages: list[OcrPagePayload] = []
            for page_number, page in enumerate(document, start=1):
                words = page.get_text("words", sort=True)
                character_count = sum(len(str(word[4]).strip()) for word in words)
                if character_count < 40:
                    return None

                grouped_words: dict[tuple[int, int], list[Any]] = {}
                for word in words:
                    grouped_words.setdefault((int(word[5]), int(word[6])), []).append(word)

                page_width = max(float(page.rect.width), 1.0)
                page_height = max(float(page.rect.height), 1.0)
                lines: list[OcrLinePayload] = []
                for line_words in grouped_words.values():
                    text = " ".join(str(word[4]).strip() for word in line_words if str(word[4]).strip())
                    if not text:
                        continue
                    x_min = min(float(word[0]) for word in line_words)
                    y_min = min(float(word[1]) for word in line_words)
                    x_max = max(float(word[2]) for word in line_words)
                    y_max = max(float(word[3]) for word in line_words)
                    lines.append(
                        OcrLinePayload(
                            text=text,
                            confidence=1.0,
                            x_min=_normalize_coordinate(x_min, page_width),
                            y_min=_normalize_coordinate(y_min, page_height),
                            x_max=_normalize_coordinate(x_max, page_width),
                            y_max=_normalize_coordinate(y_max, page_height),
                        )
                    )

                pages.append(
                    OcrPagePayload(
                        page_number=page_number,
                        language="vi",
                        engine="PyMuPDF text layer",
                        text="\n".join(line.text for line in lines),
                        confidence=1.0,
                        image_width=round(page_width),
                        image_height=round(page_height),
                        lines=lines,
                    )
                )
            return pages
        finally:
            document.close()

    def _load_page_images(self, source_path: Path) -> Iterable[tuple[int, Any]]:
        try:
            import cv2
        except ImportError as error:
            raise RuntimeError("OpenCV is required to prepare document images for OCR.") from error

        suffix = source_path.suffix.lower()
        if suffix in IMAGE_SUFFIXES:
            image = cv2.imread(str(source_path))
            if image is None:
                raise ValueError(f"Unable to read image for OCR: {source_path}")
            yield 1, image
            return
        if suffix != ".pdf":
            raise ValueError("OCR supports PDF and image files only.")

        try:
            import fitz
            import numpy as np
        except ImportError as error:
            raise RuntimeError("PyMuPDF is required to render PDF pages for OCR.") from error

        document = fitz.open(str(source_path))
        try:
            for page_number, page in enumerate(document, start=1):
                # Render at 2x so small Vietnamese characters remain legible to OCR.
                pixmap = page.get_pixmap(matrix=fitz.Matrix(2, 2), alpha=False)
                image = np.frombuffer(pixmap.samples, dtype=np.uint8).reshape(pixmap.height, pixmap.width, pixmap.n)
                yield page_number, cv2.cvtColor(image, cv2.COLOR_RGB2BGR)
        finally:
            document.close()

    @staticmethod
    def _preprocess(image: Any) -> Any:
        """Apply conservative contrast normalization without destroying colored marks."""
        import cv2

        grayscale = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        normalized = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8)).apply(grayscale)
        return cv2.cvtColor(normalized, cv2.COLOR_GRAY2BGR)

    def _recognize_page(self, image: Any, page_number: int) -> OcrPagePayload:
        height, width = image.shape[:2]
        lines = self._recognize_lines(image, width, height)
        text = "\n".join(line.text for line in lines)
        confidence = round(sum(line.confidence or 0.0 for line in lines) / len(lines), 4) if lines else 0.0
        return OcrPagePayload(
            page_number=page_number,
            language="vi",
            engine="PaddleOCR",
            text=text,
            confidence=confidence,
            image_width=width,
            image_height=height,
            lines=lines,
        )

    def _recognize_lines(self, image: Any, width: int, height: int) -> list[OcrLinePayload]:
        engine = self._get_engine()
        try:
            legacy_result = engine.ocr(image, cls=True)
        except (AttributeError, TypeError):
            legacy_result = None

        if legacy_result is not None:
            lines = self._parse_legacy_result(legacy_result, width, height)
        else:
            lines = self._parse_v3_result(engine.predict(image), width, height)
        return sorted(lines, key=lambda line: (line.y_min, line.x_min))

    def _get_engine(self) -> Any:
        if self._engine is not None:
            return self._engine
        with self._engine_lock:
            if self._engine is not None:
                return self._engine
            try:
                # Paddle 3.x on Windows CPU can fail in the oneDNN executor.
                os.environ.setdefault("FLAGS_use_mkldnn", "0")
                from paddleocr import PaddleOCR
                # PaddleOCR imports ModelScope, which loads Torch. On Windows,
                # importing Paddle first can prevent Torch from loading its DLLs.
                import paddle
            except ImportError as error:
                raise RuntimeError("PaddleOCR is required. Install ai-server requirements before calling OCR.") from error

            paddle.set_flags({"FLAGS_use_mkldnn": False})
            try:
                self._engine = PaddleOCR(
                    lang="vi",
                    use_doc_orientation_classify=True,
                    use_doc_unwarping=False,
                    use_textline_orientation=True,
                    enable_mkldnn=False,
                )
            except (TypeError, ValueError):
                # PaddleOCR 2.x-compatible initialization remains supported by 3.x.
                self._engine = PaddleOCR(lang="vi", use_angle_cls=True, enable_mkldnn=False)
            logger.info("PaddleOCR Vietnamese engine loaded successfully.")
            return self._engine

    @classmethod
    def _parse_legacy_result(cls, result: Any, width: int, height: int) -> list[OcrLinePayload]:
        lines: list[OcrLinePayload] = []
        for page_result in result or []:
            for item in page_result or []:
                if not isinstance(item, (list, tuple)) or len(item) < 2:
                    continue
                polygon, recognition = item[0], item[1]
                if not isinstance(recognition, (list, tuple)) or len(recognition) < 2:
                    continue
                text = str(recognition[0]).strip()
                if text:
                    lines.append(cls._line(text, recognition[1], polygon, width, height))
        return lines

    @classmethod
    def _parse_v3_result(cls, result: Any, width: int, height: int) -> list[OcrLinePayload]:
        lines: list[OcrLinePayload] = []
        for item in result or []:
            payload = cls._result_payload(item)
            values = payload.get("res", payload)
            texts = values.get("rec_texts", [])
            scores = values.get("rec_scores", [])
            polygons = values.get("rec_polys") or values.get("dt_polys") or values.get("rec_boxes") or []
            for text, score, polygon in zip(texts, scores, polygons):
                normalized_text = str(text).strip()
                if normalized_text:
                    lines.append(cls._line(normalized_text, score, polygon, width, height))
        return lines

    @staticmethod
    def _result_payload(item: Any) -> dict[str, Any]:
        if isinstance(item, dict):
            return item
        for attribute in ("json", "to_dict"):
            value = getattr(item, attribute, None)
            if callable(value):
                value = value()
            if isinstance(value, str):
                try:
                    value = json.loads(value)
                except json.JSONDecodeError:
                    continue
            if isinstance(value, dict):
                return value
        return {}

    @staticmethod
    def _line(text: str, score: Any, polygon: Any, width: int, height: int) -> OcrLinePayload:
        points = list(polygon.tolist()) if hasattr(polygon, "tolist") else list(polygon)
        if len(points) == 4 and all(isinstance(value, (int, float)) for value in points):
            x_min, y_min, x_max, y_max = (float(value) for value in points)
        else:
            coordinates = [(float(point[0]), float(point[1])) for point in points]
            x_min = min(point[0] for point in coordinates)
            y_min = min(point[1] for point in coordinates)
            x_max = max(point[0] for point in coordinates)
            y_max = max(point[1] for point in coordinates)
        return OcrLinePayload(
            text=text,
            confidence=round(float(score), 4),
            x_min=_normalize_coordinate(x_min, width),
            y_min=_normalize_coordinate(y_min, height),
            x_max=_normalize_coordinate(x_max, width),
            y_max=_normalize_coordinate(y_max, height),
        )


def _normalize_coordinate(value: float, dimension: float) -> float:
    if dimension <= 0:
        raise ValueError("Image dimensions must be positive.")
    return round(max(0.0, min(1.0, value / dimension)), 6)


ocr_service = OCRService()
