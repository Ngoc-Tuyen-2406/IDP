#!/usr/bin/env python3
"""Build a trainable four-class YOLO dataset from fully reviewed contract pages."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import shutil
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

from PIL import Image


PROJECT_ROOT = Path(__file__).resolve().parents[1]
CLASS_NAMES = ("signature", "seal", "table", "signature_block")
SPLITS = ("train", "valid", "test")
IMAGE_EXTENSIONS = {".bmp", ".jpeg", ".jpg", ".png", ".tif", ".tiff", ".webp"}


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--review-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "annotations" / "contract_regions" / "reviewed",
        help="Reviewed YOLO labels in images/, labels/, and review_manifest.csv.",
    )
    parser.add_argument(
        "--output-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "processed" / "contract_regions_yolo_v1",
        help="Destination for the generated YOLO dataset.",
    )
    parser.add_argument("--seed", default="idp-contract-regions-v1", help="Stable split seed.")
    parser.add_argument("--train-ratio", type=float, default=0.75)
    parser.add_argument("--valid-ratio", type=float, default=0.15)
    parser.add_argument("--overwrite", action="store_true", help="Create a timestamped rebuild instead of deleting an existing dataset.")
    return parser.parse_args()


def output_path(path: Path, overwrite: bool) -> Path:
    if not path.exists():
        return path
    if not overwrite:
        raise FileExistsError(f"Output already exists: {path}. Use --overwrite for a safe timestamped rebuild.")
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    return path.parent / f"{path.name}__rebuild_{timestamp}"


def yolo_error(raw_line: str) -> str | None:
    values = raw_line.split()
    if len(values) != 5:
        return "expected five YOLO values"
    try:
        class_id = int(values[0])
        x_center, y_center, width, height = (float(value) for value in values[1:])
    except ValueError:
        return "non-numeric value"
    if class_id not in range(len(CLASS_NAMES)):
        return f"class id {class_id} is outside 0..{len(CLASS_NAMES) - 1}"
    if not all(0.0 <= value <= 1.0 for value in (x_center, y_center, width, height)):
        return "coordinates outside [0, 1]"
    if width <= 0.0 or height <= 0.0:
        return "non-positive bounding box"
    if x_center - width / 2 < 0 or x_center + width / 2 > 1 or y_center - height / 2 < 0 or y_center + height / 2 > 1:
        return "bounding box exceeds image bounds"
    return None


def split_for_document(document_id: str, seed: str, train_ratio: float, valid_ratio: float) -> str:
    value = int(hashlib.sha256(f"{seed}:{document_id}".encode("utf-8")).hexdigest()[:12], 16) / float(16**12)
    if value < train_ratio:
        return "train"
    if value < train_ratio + valid_ratio:
        return "valid"
    return "test"


def read_review_manifest(path: Path) -> list[dict[str, str]]:
    required_columns = {"image_name", "document_id", "review_status", "annotation_complete"}
    with path.open(encoding="utf-8", newline="") as file:
        reader = csv.DictReader(file)
        actual_columns = set(reader.fieldnames or ())
        missing_columns = required_columns - actual_columns
        if missing_columns:
            raise ValueError(f"Review manifest missing columns: {', '.join(sorted(missing_columns))}")
        return [dict(row) for row in reader]


def main() -> None:
    args = parse_arguments()
    if not 0 < args.train_ratio < 1 or not 0 < args.valid_ratio < 1 or args.train_ratio + args.valid_ratio >= 1:
        raise SystemExit("Split ratios must be positive and train_ratio + valid_ratio must be below 1.")

    review_root = args.review_root.resolve()
    images_root = review_root / "images"
    labels_root = review_root / "labels"
    manifest_path = review_root / "review_manifest.csv"
    if not images_root.is_dir() or not labels_root.is_dir() or not manifest_path.is_file():
        raise SystemExit("Review input must contain images/, labels/, and review_manifest.csv.")

    reviewed_rows = read_review_manifest(manifest_path)
    approved_rows = [
        row
        for row in reviewed_rows
        if row["review_status"].strip().lower() == "approved" and row["annotation_complete"].strip().lower() == "true"
    ]
    if not approved_rows:
        raise SystemExit("No fully reviewed images found. Set review_status=approved and annotation_complete=true only after exhaustive review.")

    destination_root = output_path(args.output_root.resolve(), args.overwrite)
    for split in SPLITS:
        (destination_root / split / "images").mkdir(parents=True, exist_ok=True)
        (destination_root / split / "labels").mkdir(parents=True, exist_ok=True)

    report: dict[str, object] = {
        "dataset_id": "contract_regions_yolo_v1",
        "created_at": datetime.now(timezone.utc).isoformat(),
        "source_review_root": str(review_root),
        "class_names": list(CLASS_NAMES),
        "split_seed": args.seed,
        "splits": {split: Counter() for split in SPLITS},
        "skipped_unreviewed_rows": len(reviewed_rows) - len(approved_rows),
        "rejected": [],
    }
    source_rows: list[dict[str, str]] = []
    document_splits: dict[str, str] = {}

    for row in approved_rows:
        image_name = row["image_name"].strip()
        image_path = images_root / image_name
        label_path = labels_root / f"{Path(image_name).stem}.txt"
        if image_path.suffix.lower() not in IMAGE_EXTENSIONS or not image_path.is_file():
            report["rejected"].append({"image": image_name, "reason": "missing or unsupported image"})  # type: ignore[index]
            continue
        if not label_path.is_file():
            report["rejected"].append({"image": image_name, "reason": "missing reviewed label file"})  # type: ignore[index]
            continue
        try:
            with Image.open(image_path) as image:
                image.verify()
        except (OSError, ValueError) as error:
            report["rejected"].append({"image": image_name, "reason": f"invalid image: {error}"})  # type: ignore[index]
            continue

        normalized_lines: list[str] = []
        invalid_line = False
        for line_number, raw_line in enumerate(label_path.read_text(encoding="utf-8").splitlines(), start=1):
            if not raw_line.strip():
                continue
            error = yolo_error(raw_line)
            if error:
                report["rejected"].append({"image": image_name, "reason": f"label line {line_number}: {error}"})  # type: ignore[index]
                invalid_line = True
                break
            normalized_lines.append(" ".join(raw_line.split()))
        if invalid_line:
            continue

        document_id = row["document_id"].strip()
        if not document_id:
            report["rejected"].append({"image": image_name, "reason": "empty document_id"})  # type: ignore[index]
            continue
        split = document_splits.setdefault(document_id, split_for_document(document_id, args.seed, args.train_ratio, args.valid_ratio))
        target_image = destination_root / split / "images" / image_name
        target_label = destination_root / split / "labels" / f"{Path(image_name).stem}.txt"
        shutil.copy2(image_path, target_image)
        target_label.write_text("\n".join(normalized_lines) + ("\n" if normalized_lines else ""), encoding="utf-8")

        split_counts: Counter[str] = report["splits"][split]  # type: ignore[index]
        split_counts["images"] += 1
        for line in normalized_lines:
            class_id = int(line.split()[0])
            split_counts[CLASS_NAMES[class_id]] += 1
        source_rows.append(
            {
                "image_name": image_name,
                "document_id": document_id,
                "split": split,
                "source_image": row.get("source_image", ""),
                "reviewer": row.get("reviewer", ""),
            }
        )

    if not source_rows:
        raise SystemExit("No valid reviewed image/label pairs were available. Inspect review_manifest.csv and labels before training.")

    yaml_content = (
        f"path: {destination_root.as_posix()}\n"
        "train: train/images\n"
        "val: valid/images\n"
        "test: test/images\n\n"
        "nc: 4\n"
        "names:\n"
        "  0: signature\n"
        "  1: seal\n"
        "  2: table\n"
        "  3: signature_block\n"
    )
    (destination_root / "data.yaml").write_text(yaml_content, encoding="utf-8")
    with (destination_root / "source_manifest.csv").open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=("image_name", "document_id", "split", "source_image", "reviewer"))
        writer.writeheader()
        writer.writerows(source_rows)
    report["output_root"] = str(destination_root)
    report["splits"] = {split: dict(counter) for split, counter in report["splits"].items()}  # type: ignore[union-attr]
    (destination_root / "validation_report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
