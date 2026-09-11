#!/usr/bin/env python3
"""Safely restore one missing split in the derived signature YOLO dataset."""

from __future__ import annotations

import argparse
import importlib.util
import shutil
from collections import Counter
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
PREPARATION_SCRIPT = PROJECT_ROOT / "scripts" / "prepare_signature_yolo_dataset.py"


def load_preparation_module():
    specification = importlib.util.spec_from_file_location("signature_preparation", PREPARATION_SCRIPT)
    if specification is None or specification.loader is None:
        raise RuntimeError(f"Cannot load preparation script: {PREPARATION_SCRIPT}")
    module = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(module)
    return module


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--split", choices=("train", "valid", "test"), required=True)
    parser.add_argument("--raw-root", type=Path, default=PROJECT_ROOT / "datasets" / "raw")
    parser.add_argument("--output-root", type=Path, default=PROJECT_ROOT / "datasets" / "processed" / "signature_yolo_v1")
    args = parser.parse_args()

    preparation = load_preparation_module()
    images_target = args.output_root / args.split / "images"
    labels_target = args.output_root / args.split / "labels"
    images_target.mkdir(parents=True, exist_ok=True)
    labels_target.mkdir(parents=True, exist_ok=True)
    report = Counter()

    for source_id, source_directory in preparation.SOURCES:
        images_source = args.raw_root / source_directory / args.split / "images"
        labels_source = args.raw_root / source_directory / args.split / "labels"
        if not images_source.is_dir() or not labels_source.is_dir():
            raise FileNotFoundError(f"Missing source split: {source_id}/{args.split}")
        for image_path in sorted(images_source.iterdir()):
            if image_path.suffix.lower() not in preparation.IMAGE_EXTENSIONS:
                continue
            report["source_images"] += 1
            label_path = labels_source / f"{image_path.stem}.txt"
            image_ok, _ = preparation.valid_image(image_path)
            if not image_ok or not label_path.is_file():
                report["skipped_images"] += 1
                continue
            normalized_lines: list[str] = []
            for raw_line in label_path.read_text(encoding="utf-8").splitlines():
                if not raw_line.strip():
                    continue
                valid, _ = preparation.validate_label_line(raw_line)
                if valid:
                    _, x_center, y_center, width, height = raw_line.split()
                    normalized_lines.append(f"0 {x_center} {y_center} {width} {height}")
            if not normalized_lines:
                report["skipped_images"] += 1
                continue
            target_stem = f"{source_id}__{image_path.stem}"
            shutil.copy2(image_path, images_target / f"{target_stem}{image_path.suffix.lower()}")
            (labels_target / f"{target_stem}.txt").write_text("\n".join(normalized_lines) + "\n", encoding="utf-8")
            report["restored_images"] += 1
            report["restored_boxes"] += len(normalized_lines)

    print(dict(report))


if __name__ == "__main__":
    main()
