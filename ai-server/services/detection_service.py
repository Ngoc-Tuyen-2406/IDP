from __future__ import annotations

import json
import logging
from pathlib import Path
from tempfile import TemporaryDirectory
from threading import Lock
from typing import Any

from core.config import settings
from schemas.pipeline import DetectionRegionPayload, DetectionResponse, PipelineRequest


logger = logging.getLogger(__name__)

IMAGE_SUFFIXES = {".bmp", ".jpeg", ".jpg", ".png", ".tif", ".tiff", ".webp"}
SIGNATURE_MODEL_ID = "signature_yolo_v1"
CONTRACT_REGIONS_MODEL_ID = "contract_regions_yolo_v1"
MODEL_ROOTS = {
    SIGNATURE_MODEL_ID: settings.model_root / SIGNATURE_MODEL_ID / "baseline",
    CONTRACT_REGIONS_MODEL_ID: settings.model_root / CONTRACT_REGIONS_MODEL_ID / "baseline",
}
SIGNATURE_MODEL_ROOT = MODEL_ROOTS[SIGNATURE_MODEL_ID]
CONTRACT_MODEL_ROOT = MODEL_ROOTS[CONTRACT_REGIONS_MODEL_ID]

_models: dict[str, Any] = {}
_manifests: dict[str, dict[str, Any]] = {}
_model_lock = Lock()


def _model_root(model_id: str) -> Path:
    try:
        return MODEL_ROOTS[model_id]
    except KeyError as error:
        raise ValueError(f"Unsupported YOLO model id: {model_id}") from error


def _load_manifest(model_root: Path) -> dict[str, Any]:
    manifest_path = model_root / "manifest.json"
    if not manifest_path.is_file():
        raise FileNotFoundError(f"Manifest not found: {manifest_path}")
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("model_type") != "yolo":
        raise ValueError(f"Manifest must declare model_type='yolo': {manifest_path}")
    if not isinstance(manifest.get("weights"), str) or not manifest["weights"]:
        raise ValueError(f"Manifest must declare weights: {manifest_path}")
    return manifest


def _manifest_for(model_id: str) -> dict[str, Any]:
    if model_id not in _manifests:
        _manifests[model_id] = _load_manifest(_model_root(model_id))
    return _manifests[model_id]


def _weights_path(model_id: str) -> Path:
    return (_model_root(model_id) / str(_manifest_for(model_id)["weights"])).resolve()


def _model_is_available(model_root: Path) -> bool:
    try:
        manifest = _load_manifest(model_root)
        return (model_root / str(manifest["weights"])).is_file()
    except (FileNotFoundError, ValueError, json.JSONDecodeError):
        return False


def is_model_installed(model_id: str) -> bool:
    return _model_is_available(_model_root(model_id))


def _load_model(model_id: str) -> Any:
    if model_id in _models:
        return _models[model_id]
    with _model_lock:
        if model_id in _models:
            return _models[model_id]
        weights_path = _weights_path(model_id)
        if not weights_path.is_file():
            raise FileNotFoundError(f"Model weights not found: {weights_path}")
        try:
            from ultralytics import YOLO
        except ImportError as error:
            raise RuntimeError("Ultralytics is required for YOLO detection.") from error
        logger.info("Loading %s from %s", model_id, weights_path)
        _models[model_id] = YOLO(str(weights_path))
        return _models[model_id]


def load_signature_model() -> Any:
    """Load the evaluated signature-only model used by the main workflow."""
    return _load_model(SIGNATURE_MODEL_ID)


def load_contract_regions_model() -> Any:
    """Load the opt-in four-class bootstrap model."""
    return _load_model(CONTRACT_REGIONS_MODEL_ID)


def load_detection_model() -> Any:
    """Backward-compatible production loader. It intentionally selects signature-only."""
    return load_signature_model()


def get_active_model_info() -> dict[str, Any]:
    """Return the production model, never the experimental bootstrap model."""
    model = load_signature_model()
    return {
        "model_root": str(SIGNATURE_MODEL_ROOT),
        "classes": model.names,
        "manifest": _manifest_for(SIGNATURE_MODEL_ID),
    }


def _predict(image_path: str | Path, model_id: str, confidence: float) -> list[dict[str, Any]]:
    if not 0.0 < confidence <= 1.0:
        raise ValueError("confidence must be in the interval (0, 1].")
    path = Path(image_path)
    if not path.is_file():
        raise FileNotFoundError(f"Image not found: {path}")
    if path.suffix.lower() not in IMAGE_SUFFIXES:
        raise ValueError(f"Unsupported image type: {path.suffix}")

    manifest = _manifest_for(model_id)
    model = _load_model(model_id)
    results = model.predict(
        source=str(path),
        conf=confidence,
        iou=0.4,
        imgsz=int(manifest.get("input_size", 1024)),
        verbose=False,
    )
    detections: list[dict[str, Any]] = []
    for result in results:
        if result.boxes is None:
            continue
        for box in result.boxes:
            class_id = int(box.cls[0].item())
            x1, y1, x2, y2 = (float(value) for value in box.xyxy[0].tolist())
            detections.append(
                {
                    "class_id": class_id,
                    "class_name": str(result.names[class_id]),
                    "confidence": round(float(box.conf[0].item()), 4),
                    "bbox": {
                        "x1": round(x1, 2),
                        "y1": round(y1, 2),
                        "x2": round(x2, 2),
                        "y2": round(y2, 2),
                    },
                }
            )
    return detections


def detect_signatures(image_path: str | Path, confidence: float = 0.5) -> list[dict[str, Any]]:
    return _predict(image_path, SIGNATURE_MODEL_ID, confidence)


def detect_regions(image_path: str | Path, confidence: float = 0.25) -> list[dict[str, Any]]:
    """Run the four-class bootstrap only when explicitly requested."""
    if not is_model_installed(CONTRACT_REGIONS_MODEL_ID):
        raise FileNotFoundError("Experimental contract-regions model is not installed.")
    return _predict(image_path, CONTRACT_REGIONS_MODEL_ID, confidence)


class DetectionService:
    def run(self, payload: PipelineRequest) -> DetectionResponse:
        return self.run_signature(payload)

    def run_signature(self, payload: PipelineRequest) -> DetectionResponse:
        return self._run_with_model(payload, SIGNATURE_MODEL_ID, confidence=0.5)

    def run_contract_regions_experimental(self, payload: PipelineRequest) -> DetectionResponse:
        if not is_model_installed(CONTRACT_REGIONS_MODEL_ID):
            raise FileNotFoundError("Experimental contract-regions model is not installed.")
        return self._run_with_model(payload, CONTRACT_REGIONS_MODEL_ID, confidence=0.25)

    def _run_with_model(self, payload: PipelineRequest, model_id: str, confidence: float) -> DetectionResponse:
        path = Path(payload.file_path)
        if not path.is_file():
            raise FileNotFoundError(f"Input file not found: {path}")
        if path.suffix.lower() in IMAGE_SUFFIXES:
            regions = self._detect_page(path, 1, model_id, confidence)
        elif path.suffix.lower() == ".pdf":
            regions = self._detect_pdf(path, model_id, confidence)
        else:
            raise ValueError("Detection supports PDF and image files only.")

        manifest = _manifest_for(model_id)
        return DetectionResponse(
            regions=regions,
            model={
                "model_name": manifest.get("model_name", model_id),
                "model_type": "yolo",
                "version": manifest.get("version"),
                "description": manifest.get("description"),
            },
        )

    def _detect_pdf(self, pdf_path: Path, model_id: str, confidence: float) -> list[DetectionRegionPayload]:
        try:
            import fitz
        except ImportError as error:
            raise RuntimeError("PyMuPDF is required to render PDF pages for detection.") from error

        regions: list[DetectionRegionPayload] = []
        with TemporaryDirectory(prefix="idp-detection-", dir=str(_temporary_output_root())) as temporary_directory:
            document = fitz.open(str(pdf_path))
            try:
                for page_number, page in enumerate(document, start=1):
                    image_path = Path(temporary_directory) / f"page-{page_number}.png"
                    page.get_pixmap(matrix=fitz.Matrix(2, 2), alpha=False).save(str(image_path))
                    regions.extend(self._detect_page(image_path, page_number, model_id, confidence))
            finally:
                document.close()
        return regions

    @staticmethod
    def _detect_page(image_path: Path, page_number: int, model_id: str, confidence: float) -> list[DetectionRegionPayload]:
        from PIL import Image

        with Image.open(image_path) as image:
            width, height = image.size
        return [
            DetectionRegionPayload(
                page_number=page_number,
                label=str(detection["class_name"]).replace("_", " ").title(),
                x_min=_normalize_coordinate(detection["bbox"]["x1"], width),
                y_min=_normalize_coordinate(detection["bbox"]["y1"], height),
                x_max=_normalize_coordinate(detection["bbox"]["x2"], width),
                y_max=_normalize_coordinate(detection["bbox"]["y2"], height),
                confidence=float(detection["confidence"]),
            )
            for detection in _predict(image_path, model_id, confidence)
        ]


def _normalize_coordinate(value: float, dimension: int) -> float:
    if dimension <= 0:
        raise ValueError("Image dimensions must be positive.")
    return round(max(0.0, min(1.0, value / dimension)), 6)


def _temporary_output_root() -> Path:
    root = settings.output_root / "runtime"
    root.mkdir(parents=True, exist_ok=True)
    return root


detection_service = DetectionService()
