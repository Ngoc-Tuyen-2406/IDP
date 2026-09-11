#!/usr/bin/env python3
"""Assemble a trainable four-class YOLO bootstrap dataset from staged real sources."""

from __future__ import annotations

import argparse
import csv
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
        "--signature-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "processed" / "signature_yolo_v1",
        help="Validated real signature dataset.",
    )
    parser.add_argument(
        "--seal-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "processed" / "ndl_stamp_seed_v1",
        help="NDL sample with real 5_stamp ground-truth boxes.",
    )
    parser.add_argument(
        "--candidate-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "annotations" / "auto_label_candidates_contract_v1",
        help="Pseudo-label candidates generated from real Vietnamese document images.",
    )
    parser.add_argument(
        "--output-root",
        type=Path,
        default=PROJECT_ROOT / "datasets" / "processed" / "contract_regions_yolo_v1",
        help="Destination. Existing output is never deleted.",
    )
    parser.add_argument("--overwrite", action="store_true", help="Create a timestamped rebuild beside an existing output.")
    return parser.parse_args()


def safe_output_root(output_root: Path, overwrite: bool) -> Path:
    if not output_root.exists():
        return output_root
    if not overwrite:
        raise FileExistsError(f"Output already exists: {output_root}. Use --overwrite for a safe timestamped rebuild.")
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    return output_root.parent / f"{output_root.name}__bootstrap_{timestamp}"


def valid_label_lines(label_path: Path) -> list[str]:
    normalized: list[str] = []
    for line_number, raw_line in enumerate(label_path.read_text(encoding="utf-8").splitlines(), start=1):
        if not raw_line.strip():
            continue
        values = raw_line.split()
        if len(values) != 5:
            raise ValueError(f"{label_path}:{line_number} must contain five YOLO values")
        try:
            class_id = int(values[0])
            x_center, y_center, width, height = (float(value) for value in values[1:])
        except ValueError as error:
            raise ValueError(f"{label_path}:{line_number} contains non-numeric data") from error
        if class_id not in range(len(CLASS_NAMES)):
            raise ValueError(f"{label_path}:{line_number} uses unknown class id {class_id}")
        if not all(0.0 <= value <= 1.0 for value in (x_center, y_center, width, height)) or width <= 0 or height <= 0:
            raise ValueError(f"{label_path}:{line_number} has invalid coordinates")
        if x_center - width / 2 < 0 or x_center + width / 2 > 1 or y_center - height / 2 < 0 or y_center + height / 2 > 1:
            raise ValueError(f"{label_path}:{line_number} exceeds image bounds")
        normalized.append(" ".join(values))
    return normalized


def copy_pair(
    image_path: Path,
    label_path: Path,
    target_root: Path,
    split: str,
    target_stem: str,
    provenance: str,
    report: dict[str, object],
    source_rows: list[dict[str, str]],
) -> None:
    if image_path.suffix.lower() not in IMAGE_EXTENSIONS:
        raise ValueError(f"Unsupported image extension: {image_path}")
    with Image.open(image_path) as image:
        image.verify()
    labels = valid_label_lines(label_path)
    target_image = target_root / split / "images" / f"{target_stem}{image_path.suffix.lower()}"
    target_label = target_root / split / "labels" / f"{target_stem}.txt"
    shutil.copy2(image_path, target_image)
    target_label.write_text("\n".join(labels) + ("\n" if labels else ""), encoding="utf-8")

    split_counts: Counter[str] = report["splits"][split]  # type: ignore[index]
    split_counts["images"] += 1
    split_counts[f"provenance::{provenance}"] += 1
    for label in labels:
        split_counts[CLASS_NAMES[int(label.split()[0])]] += 1
    source_rows.append(
        {
            "image_name": target_image.name,
            "split": split,
            "source_image": str(image_path),
            "source_label": str(label_path),
            "provenance": provenance,
        }
    )


def main() -> None:
    args = parse_arguments()
    signature_root = args.signature_root.resolve()
    seal_root = args.seal_root.resolve()
    candidate_root = args.candidate_root.resolve()
    output_root = safe_output_root(args.output_root.resolve(), args.overwrite)
    for split in SPLITS:
        (output_root / split / "images").mkdir(parents=True, exist_ok=True)
        (output_root / split / "labels").mkdir(parents=True, exist_ok=True)

    report: dict[str, object] = {
        "dataset_id": "contract_regions_yolo_v1",
        "dataset_kind": "bootstrap",
        "created_at": datetime.now(timezone.utc).isoformat(),
        "class_names": list(CLASS_NAMES),
        "sources": {
            "signature": str(signature_root),
            "seal": str(seal_root),
            "pseudo_candidates": str(candidate_root),
        },
        "splits": {split: Counter() for split in SPLITS},
        "limitations": [
            "signature labels are validated ground truth from public real images.",
            "seal labels from NDL are real ground truth but extremely small in number.",
            "seal/table/signature_block candidates from Vietnamese pages are pseudo-labels and must not be reported as final evaluation ground truth.",
            "This dataset is trainable for a bootstrap model, not a production-quality four-class detector.",
        ],
    }
    source_rows: list[dict[str, str]] = []

    for split in SPLITS:
        image_directory = signature_root / split / "images"
        label_directory = signature_root / split / "labels"
        if not image_directory.is_dir() or not label_directory.is_dir():
            raise SystemExit(f"Missing signature split: {split}")
        for image_path in sorted(path for path in image_directory.iterdir() if path.is_file() and path.suffix.lower() in IMAGE_EXTENSIONS):
            label_path = label_directory / f"{image_path.stem}.txt"
            if not label_path.is_file():
                raise SystemExit(f"Missing signature label: {label_path}")
            copy_pair(image_path, label_path, output_root, split, f"signature_gt__{image_path.stem}", "ground_truth_signature", report, source_rows)

    seal_images = seal_root / "images"
    seal_labels = seal_root / "labels"
    if not seal_images.is_dir() or not seal_labels.is_dir():
        raise SystemExit("Missing NDL seal seed images/ or labels/.")
    for image_path in sorted(path for path in seal_images.iterdir() if path.is_file() and path.suffix.lower() in IMAGE_EXTENSIONS):
        label_path = seal_labels / f"{image_path.stem}.txt"
        if label_path.is_file():
            copy_pair(image_path, label_path, output_root, "train", f"seal_gt__{image_path.stem}", "ground_truth_seal", report, source_rows)

    candidate_manifest = candidate_root / "manifest.csv"
    if not candidate_manifest.is_file():
        raise SystemExit(f"Missing candidate manifest: {candidate_manifest}")
    with candidate_manifest.open(encoding="utf-8", newline="") as file:
        for row in csv.DictReader(file):
            image_path = Path(row["source_image"])
            label_path = candidate_root / row["label_file"]
            if not image_path.is_file() or not label_path.is_file():
                raise SystemExit(f"Missing candidate source or label for {row.get('candidate_id', '<unknown>')}")
            copy_pair(image_path, label_path, output_root, "train", f"vietnamese_pseudo__{row['candidate_id']}", "pseudo_label_vietnamese", report, source_rows)

    yaml_content = (
        f"path: {output_root.as_posix()}\n"
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
    (output_root / "data.yaml").write_text(yaml_content, encoding="utf-8")
    with (output_root / "source_manifest.csv").open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=("image_name", "split", "source_image", "source_label", "provenance"))
        writer.writeheader()
        writer.writerows(source_rows)
    report["output_root"] = str(output_root)
    report["splits"] = {split: dict(counts) for split, counts in report["splits"].items()}  # type: ignore[union-attr]
    (output_root / "bootstrap_report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
