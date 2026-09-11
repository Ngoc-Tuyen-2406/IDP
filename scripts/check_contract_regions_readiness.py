#!/usr/bin/env python3
"""Check whether a four-class contract-region YOLO dataset is safe to train."""

from __future__ import annotations

import argparse
import csv
import json
from collections import Counter
from pathlib import Path


CLASS_NAMES = ("signature", "seal", "table", "signature_block")
SPLITS = ("train", "valid", "test")


def main() -> None:
    project_root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dataset-root", type=Path, default=project_root / "datasets/processed/contract_regions_yolo_v1")
    parser.add_argument("--minimum-train-instances", type=int, default=300)
    args = parser.parse_args()

    counts = {split: Counter() for split in SPLITS}
    quality_errors: list[str] = []
    for split in SPLITS:
        images_directory = args.dataset_root / split / "images"
        labels_directory = args.dataset_root / split / "labels"
        if not images_directory.is_dir() or not labels_directory.is_dir():
            quality_errors.append(f"{split}: missing images/ or labels/ directory")
            continue
        images = {path.stem for path in images_directory.iterdir() if path.is_file()}
        labels = {path.stem for path in labels_directory.glob("*.txt")}
        for stem in sorted(images - labels):
            quality_errors.append(f"{split}: missing label for {stem}")
        for stem in sorted(labels - images):
            quality_errors.append(f"{split}: missing image for {stem}")
        for label_file in labels_directory.glob("*.txt"):
            for line_number, line in enumerate(label_file.read_text(encoding="utf-8").splitlines(), start=1):
                if line.strip():
                    values = line.split()
                    if len(values) != 5:
                        quality_errors.append(f"{split}: {label_file.name}:{line_number} does not contain five YOLO values")
                        continue
                    try:
                        class_id = int(values[0])
                        x_center, y_center, width, height = (float(value) for value in values[1:])
                    except ValueError:
                        quality_errors.append(f"{split}: {label_file.name}:{line_number} contains non-numeric YOLO values")
                        continue
                    if class_id not in range(len(CLASS_NAMES)):
                        quality_errors.append(f"{split}: {label_file.name}:{line_number} has invalid class id {class_id}")
                        continue
                    if not all(0.0 <= value <= 1.0 for value in (x_center, y_center, width, height)) or width <= 0 or height <= 0:
                        quality_errors.append(f"{split}: {label_file.name}:{line_number} has invalid coordinates")
                        continue
                    if x_center - width / 2 < 0 or x_center + width / 2 > 1 or y_center - height / 2 < 0 or y_center + height / 2 > 1:
                        quality_errors.append(f"{split}: {label_file.name}:{line_number} exceeds image bounds")
                        continue
                    counts[split][CLASS_NAMES[class_id]] += 1

    leakage: list[str] = []
    manifest_path = args.dataset_root / "source_manifest.csv"
    if not manifest_path.is_file():
        quality_errors.append("missing source_manifest.csv; document-level split integrity cannot be verified")
    else:
        document_splits: dict[str, set[str]] = {}
        with manifest_path.open(encoding="utf-8", newline="") as file:
            for row in csv.DictReader(file):
                document_splits.setdefault(row.get("document_id", ""), set()).add(row.get("split", ""))
        leakage = sorted(document_id for document_id, document_split_set in document_splits.items() if document_id and len(document_split_set) > 1)

    train_counts = counts["train"]
    missing = [name for name in CLASS_NAMES if train_counts[name] == 0]
    below_minimum = [name for name in CLASS_NAMES if 0 < train_counts[name] < args.minimum_train_instances]
    report = {
        "dataset_root": str(args.dataset_root),
        "minimum_train_instances": args.minimum_train_instances,
        "counts": {split: {name: counts[split][name] for name in CLASS_NAMES} for split in SPLITS},
        "missing_train_classes": missing,
        "below_minimum_train_classes": below_minimum,
        "quality_errors": quality_errors,
        "document_split_leakage": leakage,
        "ready_for_multiclass_training": not missing and not below_minimum and not quality_errors and not leakage,
    }
    print(json.dumps(report, ensure_ascii=False, indent=2))
    if not report["ready_for_multiclass_training"]:
        raise SystemExit(2)


if __name__ == "__main__":
    main()
