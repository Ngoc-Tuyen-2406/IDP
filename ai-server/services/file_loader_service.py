from __future__ import annotations

from pathlib import Path

from pypdf import PdfReader
from PIL import Image

from utils.text_utils import normalize_text


class FileLoaderService:
    def load_pages(self, file_path: str) -> list[str]:
        path = Path(file_path)
        if not path.exists():
            raise FileNotFoundError(f"Input file not found: {file_path}")
        suffix = path.suffix.lower()
        if suffix == ".pdf":
            return self._read_pdf(path)
        if suffix in {".png", ".jpg", ".jpeg", ".bmp", ".tif", ".tiff"}:
            return self._read_image(path)
        return [normalize_text(path.read_text(encoding="utf-8", errors="ignore"))]

    def _read_pdf(self, path: Path) -> list[str]:
        reader = PdfReader(str(path))
        pages: list[str] = []
        for page in reader.pages:
            text = page.extract_text() or ""
            pages.append(normalize_text(text))
        return pages

    def _read_image(self, path: Path) -> list[str]:
        try:
            from paddleocr import PaddleOCR  # type: ignore
        except Exception:
            Image.open(path).close()
            return [""]

        ocr = PaddleOCR(use_angle_cls=True, lang="vi")
        result = ocr.ocr(str(path), cls=True)
        lines: list[str] = []
        for page in result or []:
            for item in page or []:
                if len(item) >= 2 and item[1]:
                    lines.append(str(item[1][0]))
        return [normalize_text("\n".join(lines))]


file_loader_service = FileLoaderService()
