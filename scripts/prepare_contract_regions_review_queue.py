#!/usr/bin/env python3
"""Create a non-destructive review queue for real contract-region annotation."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import shutil
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

from PIL import Image, ImageDraw


PROJECT_ROOT = Path(__file__).resolve().parents[1]
IMAGE_EXTENSIONS = {".bmp", ".jpeg", ".jpg", ".png", ".tif", ".tiff", ".webp"}
CONTRACT_CATEGORIES = ("CONTRACTS", "FORMS", "TRADE DOCUMENTS")
CLASS_NAMES = ("signature", "seal", "table", "signature_block")
CLASS_COLORS = {
    0: (37, 99, 235),
    1: (220, 38, 38),
    2: (22, 163, 74),
    3: (147, 51, 234),
}


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--raw-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "raw" / "vietnamese_ocr_archive" / "IMG_OCR_VIE_CN",
        help="Root containing the staged Vietnamese OCR document categories.",
    )
    parser.add_argument(
        "--candidate-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "annotations" / "auto_label_candidates_contract_v1",
        help="Pseudo-label output created by generate_auto_label_candidates.py.",
    )
    parser.add_argument(
        "--output-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "annotations" / "contract_regions" / "review_queue_v1",
        help="Destination annotation queue. Existing outputs are never deleted.",
    )
    parser.add_argument(
        "--overwrite",
        action="store_true",
        help="Create a timestamped queue beside an existing output instead of deleting it.",
    )
    return parser.parse_args()


def safe_output_root(output_root: Path, overwrite: bool) -> Path:
    if not output_root.exists():
        return output_root
    if not overwrite:
        raise FileExistsError(f"Output already exists: {output_root}. Use --overwrite for a safe timestamped rebuild.")
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    return output_root.parent / f"{output_root.name}__rebuild_{timestamp}"


def source_images(raw_root: Path) -> list[Path]:
    images: list[Path] = []
    for category in CONTRACT_CATEGORIES:
        category_root = raw_root / category
        if not category_root.is_dir():
            continue
        images.extend(path for path in category_root.rglob("*") if path.is_file() and path.suffix.lower() in IMAGE_EXTENSIONS)
    return sorted(images)


def candidate_labels(candidate_root: Path) -> dict[Path, Path]:
    manifest_path = candidate_root / "manifest.csv"
    if not manifest_path.is_file():
        return {}

    candidates: dict[Path, Path] = {}
    with manifest_path.open(encoding="utf-8", newline="") as file:
        for row in csv.DictReader(file):
            source = Path(row["source_image"]).resolve()
            label = candidate_root / row["label_file"]
            if label.is_file():
                candidates[source] = label
    return candidates


def yolo_boxes(label_path: Path) -> list[tuple[int, float, float, float, float]]:
    boxes: list[tuple[int, float, float, float, float]] = []
    for raw_line in label_path.read_text(encoding="utf-8").splitlines():
        if not raw_line.strip():
            continue
        values = raw_line.split()
        if len(values) != 5:
            continue
        try:
            class_id = int(values[0])
            x_center, y_center, width, height = (float(value) for value in values[1:])
        except ValueError:
            continue
        if class_id in range(len(CLASS_NAMES)):
            boxes.append((class_id, x_center, y_center, width, height))
    return boxes


def write_preview(image_path: Path, label_path: Path | None, output_path: Path) -> int:
    with Image.open(image_path) as source:
        image = source.convert("RGB")
    draw = ImageDraw.Draw(image)
    box_count = 0
    if label_path is not None:
        for class_id, x_center, y_center, width, height in yolo_boxes(label_path):
            left = int((x_center - width / 2) * image.width)
            top = int((y_center - height / 2) * image.height)
            right = int((x_center + width / 2) * image.width)
            bottom = int((y_center + height / 2) * image.height)
            color = CLASS_COLORS[class_id]
            draw.rectangle((left, top, right, bottom), outline=color, width=max(2, image.width // 500))
            draw.text((left, max(0, top - 16)), f"CANDIDATE: {CLASS_NAMES[class_id]}", fill=color)
            box_count += 1
    image.save(output_path, quality=95)
    return box_count


def main() -> None:
    args = parse_arguments()
    raw_root = args.raw_root.resolve()
    candidate_root = args.candidate_root.resolve()
    output_root = safe_output_root(args.output_root.resolve(), args.overwrite)
    images_root = output_root / "images"
    prelabels_root = output_root / "prelabels"
    previews_root = output_root / "previews"
    for directory in (images_root, prelabels_root, previews_root):
        directory.mkdir(parents=True, exist_ok=True)

    candidates = candidate_labels(candidate_root)
    tasks: list[dict[str, str]] = []
    review_rows: list[dict[str, str]] = []
    counts: Counter[str] = Counter()

    for source_image in source_images(raw_root):
        relative_path = source_image.relative_to(raw_root)
        task_id = hashlib.sha1(relative_path.as_posix().encode("utf-8")).hexdigest()[:16]
        image_name = f"{task_id}__{source_image.name}"
        copied_image = images_root / image_name
        shutil.copy2(source_image, copied_image)

        candidate_label = candidates.get(source_image.resolve())
        copied_prelabel = ""
        candidate_count = 0
        if candidate_label is not None:
            copied_label = prelabels_root / f"{Path(image_name).stem}.txt"
            shutil.copy2(candidate_label, copied_label)
            copied_prelabel = str(copied_label.relative_to(output_root))
            candidate_count = len(yolo_boxes(copied_label))
            for class_id, *_ in yolo_boxes(copied_label):
                counts[CLASS_NAMES[class_id]] += 1

        preview_path = previews_root / f"{Path(image_name).stem}.jpg"
        write_preview(copied_image, prelabels_root / f"{Path(image_name).stem}.txt" if copied_prelabel else None, preview_path)
        document_id = f"{relative_path.parts[0].lower()}::{source_image.stem}"
        task_type = "contract_page" if relative_path.parts[0] == "CONTRACTS" else "supporting_document_page"
        tasks.append(
            {
                "task_id": task_id,
                "image_name": image_name,
                "source_image": str(source_image),
                "source_relative_path": relative_path.as_posix(),
                "document_id": document_id,
                "task_type": task_type,
                "candidate_prelabel": copied_prelabel,
                "candidate_box_count": str(candidate_count),
                "status": "pending_review",
            }
        )
        review_rows.append(
            {
                "task_id": task_id,
                "image_name": image_name,
                "document_id": document_id,
                "source_image": str(source_image),
                "review_status": "pending_review",
                "annotation_complete": "false",
                "reviewer": "",
                "notes": "",
            }
        )

    with (output_root / "task_manifest.csv").open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=tasks[0].keys() if tasks else ("task_id", "image_name", "source_image", "source_relative_path", "document_id", "task_type", "candidate_prelabel", "candidate_box_count", "status"))
        writer.writeheader()
        writer.writerows(tasks)
    with (output_root / "review_manifest.csv").open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=review_rows[0].keys() if review_rows else ("task_id", "image_name", "document_id", "source_image", "review_status", "annotation_complete", "reviewer", "notes"))
        writer.writeheader()
        writer.writerows(review_rows)
    (output_root / "classes.txt").write_text("\n".join(CLASS_NAMES) + "\n", encoding="utf-8")

    report = {
        "dataset_id": "contract_regions_review_queue_v1",
        "created_at": datetime.now(timezone.utc).isoformat(),
        "source_root": str(raw_root),
        "task_count": len(tasks),
        "candidate_images": sum(1 for task in tasks if task["candidate_prelabel"]),
        "candidate_class_counts": dict(counts),
        "ground_truth_images": 0,
        "warning": "Prelabels are review hints only and must not be used for training.",
    }
    (output_root / "report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
