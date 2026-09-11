from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import json
import re
import unicodedata

from PIL import Image, ImageOps


@dataclass(frozen=True, slots=True)
class AutoLabelCandidate:
    class_id: int
    label: str
    confidence: float
    source: str
    x_min: float
    y_min: float
    x_max: float
    y_max: float

    def to_yolo(self) -> str:
        width = self.x_max - self.x_min
        height = self.y_max - self.y_min
        return f"{self.class_id} {self.x_min + width / 2:.6f} {self.y_min + height / 2:.6f} {width:.6f} {height:.6f}"

    def to_dict(self) -> dict[str, object]:
        return {
            "class_id": self.class_id,
            "label": self.label,
            "confidence": round(self.confidence, 4),
            "source": self.source,
            "bbox": [round(self.x_min, 6), round(self.y_min, 6), round(self.x_max, 6), round(self.y_max, 6)],
        }


class AutoLabelService:
    """Produces reviewable pseudo-labels without treating heuristics as ground truth."""

    _SIGNATURE_BLOCK_KEYWORDS = (
        "chu ky", "ky ten", "nguoi ky", "dai dien", "giam doc", "tong giam doc",
        "ben a", "ben b", "signature", "signatory",
    )

    def detect(self, image_path: Path, minimum_confidence: float = 0.55) -> list[AutoLabelCandidate]:
        with Image.open(image_path) as source_image:
            image = source_image.convert("RGB")
        width, height = image.size
        candidates = self._detect_tables(image, width, height)
        candidates.extend(self._detect_red_seals(image, width, height))
        candidates.extend(self._detect_signature_blocks(image_path, width, height, candidates))
        return self._deduplicate([candidate for candidate in candidates if candidate.confidence >= minimum_confidence])

    def _detect_tables(self, image: Image.Image, original_width: int, original_height: int) -> list[AutoLabelCandidate]:
        # Layout candidates do not need native resolution; keep this bounded for
        # predictable batch runs while returning coordinates on the original page.
        scaled, scale = self._downscale(image, 640)
        width, height = scaled.size
        gray = ImageOps.grayscale(scaled)
        pixels = gray.load()
        row_counts = [sum(1 for x in range(width) if pixels[x, y] < 115) for y in range(height)]
        column_counts = [sum(1 for y in range(height) if pixels[x, y] < 115) for x in range(width)]
        horizontal = self._line_centers(row_counts, max(25, int(width * 0.14)))
        vertical = self._line_centers(column_counts, max(25, int(height * 0.12)))
        if len(horizontal) < 2 or len(vertical) < 2:
            return []

        x_min, x_max = min(vertical), max(vertical)
        y_min, y_max = min(horizontal), max(horizontal)
        box_width, box_height = x_max - x_min, y_max - y_min
        if box_width < width * 0.20 or box_height < height * 0.05:
            return []
        density = min(1.0, (len(horizontal) * len(vertical)) / 40)
        confidence = min(0.88, 0.48 + density * 0.30 + min(0.12, box_width * box_height / (width * height)))
        return [self._candidate(2, "table", confidence, "pillow_grid", x_min / scale, y_min / scale, x_max / scale, y_max / scale, original_width, original_height)]

    def _detect_red_seals(self, image: Image.Image, original_width: int, original_height: int) -> list[AutoLabelCandidate]:
        scaled, scale = self._downscale(image, 640)
        width, height = scaled.size
        red_pixels = []
        for y in range(height):
            for x in range(width):
                red, green, blue = scaled.getpixel((x, y))
                if red > 105 and red > green * 1.30 and red > blue * 1.15:
                    red_pixels.append((x, y))
        if len(red_pixels) < 100:
            return []
        x_min, x_max = min(point[0] for point in red_pixels), max(point[0] for point in red_pixels)
        y_min, y_max = min(point[1] for point in red_pixels), max(point[1] for point in red_pixels)
        box_width, box_height = x_max - x_min, y_max - y_min
        if not box_width or not box_height:
            return []
        aspect_ratio = min(box_width, box_height) / max(box_width, box_height)
        coverage = len(red_pixels) / float(box_width * box_height)
        if aspect_ratio < 0.55 or coverage < 0.025 or box_width * box_height > width * height * 0.20:
            return []
        confidence = min(0.80, 0.42 + aspect_ratio * 0.25 + min(0.13, coverage * 0.45))
        return [self._candidate(1, "seal", confidence, "pillow_red_stamp", x_min / scale, y_min / scale, x_max / scale, y_max / scale, original_width, original_height)]

    def _detect_signature_blocks(
        self,
        image_path: Path,
        width: int,
        height: int,
        candidates: list[AutoLabelCandidate],
    ) -> list[AutoLabelCandidate]:
        json_path = image_path.with_suffix(".json")
        if not json_path.is_file():
            return []
        try:
            shapes = json.loads(json_path.read_text(encoding="utf-8")).get("shapes", [])
        except (OSError, ValueError, json.JSONDecodeError):
            return []

        keyword_boxes: list[tuple[float, float, float, float]] = []
        for shape in shapes:
            points = shape.get("points", [])
            text = self._normalize_text(str(shape.get("label", "")))
            if len(points) != 2 or not any(keyword in text for keyword in self._SIGNATURE_BLOCK_KEYWORDS):
                continue
            (x1, y1), (x2, y2) = points
            if max(y1, y2) >= height * 0.40:
                keyword_boxes.append((min(x1, x2), min(y1, y2), max(x1, x2), max(y1, y2)))
        if not keyword_boxes:
            return []

        x_min, y_min = min(box[0] for box in keyword_boxes), min(box[1] for box in keyword_boxes)
        x_max, y_max = max(box[2] for box in keyword_boxes), max(box[3] for box in keyword_boxes)
        for candidate in candidates:
            if candidate.label == "seal" and candidate.y_min * height >= y_min - height * 0.15:
                x_min, y_min = min(x_min, candidate.x_min * width), min(y_min, candidate.y_min * height)
                x_max, y_max = max(x_max, candidate.x_max * width), max(y_max, candidate.y_max * height)
        return [self._candidate(3, "signature_block", 0.60, "ocr_signature_keywords", x_min - width * 0.05, y_min - height * 0.06, x_max + width * 0.05, y_max + height * 0.06, width, height)]

    @staticmethod
    def _downscale(image: Image.Image, maximum_dimension: int) -> tuple[Image.Image, float]:
        width, height = image.size
        scale = min(1.0, maximum_dimension / max(width, height))
        if scale == 1.0:
            return image, scale
        return image.resize((round(width * scale), round(height * scale))), scale

    @staticmethod
    def _line_centers(counts: list[int], threshold: int) -> list[int]:
        centers: list[int] = []
        start: int | None = None
        for index, count in enumerate(counts + [0]):
            if count >= threshold and start is None:
                start = index
            elif count < threshold and start is not None:
                centers.append((start + index - 1) // 2)
                start = None
        return centers

    @staticmethod
    def _normalize_text(value: str) -> str:
        try:
            value = value.encode("latin1").decode("utf-8")
        except UnicodeError:
            pass
        decomposed = unicodedata.normalize("NFD", value.lower())
        value = "".join(char for char in decomposed if unicodedata.category(char) != "Mn")
        return re.sub(r"\s+", " ", value).strip()

    @staticmethod
    def _candidate(class_id: int, label: str, confidence: float, source: str, x1: float, y1: float, x2: float, y2: float, width: int, height: int) -> AutoLabelCandidate:
        x1, x2 = sorted((max(0.0, x1), min(float(width), x2)))
        y1, y2 = sorted((max(0.0, y1), min(float(height), y2)))
        return AutoLabelCandidate(class_id, label, confidence, source, x1 / width, y1 / height, x2 / width, y2 / height)

    @staticmethod
    def _deduplicate(candidates: list[AutoLabelCandidate]) -> list[AutoLabelCandidate]:
        accepted: list[AutoLabelCandidate] = []
        for candidate in sorted(candidates, key=lambda item: item.confidence, reverse=True):
            if not any(candidate.class_id == item.class_id and AutoLabelService._iou(candidate, item) > 0.75 for item in accepted):
                accepted.append(candidate)
        return accepted

    @staticmethod
    def _iou(first: AutoLabelCandidate, second: AutoLabelCandidate) -> float:
        x1, y1 = max(first.x_min, second.x_min), max(first.y_min, second.y_min)
        x2, y2 = min(first.x_max, second.x_max), min(first.y_max, second.y_max)
        intersection = max(0.0, x2 - x1) * max(0.0, y2 - y1)
        union = (first.x_max - first.x_min) * (first.y_max - first.y_min) + (second.x_max - second.x_min) * (second.y_max - second.y_min) - intersection
        return intersection / union if union else 0.0


auto_label_service = AutoLabelService()
