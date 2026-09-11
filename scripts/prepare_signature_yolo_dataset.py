#!/usr/bin/env python3
"""Create a validated, project-owned YOLO dataset from approved signature sources."""

from __future__ import annotations

import argparse
import json
import shutil
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

try:
    from PIL import Image
except ImportError as error:
    raise SystemExit("Pillow is required. Install ai-server requirements before running this script.") from error


SPLITS = ("train", "valid", "test")
IMAGE_EXTENSIONS = {".bmp", ".jpeg", ".jpg", ".png", ".tif", ".tiff", ".webp"}
SOURCES = (
    ("signature_rf100", "signature_rf100"),
    ("signature_roboflow_amruth", "signature_roboflow_amruth"),
)


def parse_arguments() -> argparse.Namespace:
    project_root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--raw-root",
        type=Path,
        default=project_root / "datasets" / "raw",
        help="Directory containing staged source datasets.",
    )
    parser.add_argument(
        "--output-root",
        type=Path,
        default=project_root / "datasets" / "processed" / "signature_yolo_v1",
        help="Destination for the normalized YOLO dataset.",
    )
    parser.add_argument(
        "--overwrite",
        action="store_true",
        help="Create a timestamped rebuild next to an existing output instead of deleting it.",
    )
    return parser.parse_args()


def validate_label_line(line: str) -> tuple[bool, str | None]:
    values = line.split()
    if len(values) != 5:
        return False, "expected five YOLO values"
    try:
        _, x_center, y_center, width, height = (float(value) for value in values)
    except ValueError:
        return False, "non-numeric YOLO value"
    if not all(0.0 <= value <= 1.0 for value in (x_center, y_center, width, height)):
        return False, "coordinates outside [0, 1]"
    if width <= 0.0 or height <= 0.0:
        return False, "non-positive bounding box"
    if x_center - width / 2 < 0.0 or x_center + width / 2 > 1.0:
        return False, "bounding box exceeds horizontal image bounds"
    if y_center - height / 2 < 0.0 or y_center + height / 2 > 1.0:
        return False, "bounding box exceeds vertical image bounds"
    return True, None


def valid_image(path: Path) -> tuple[bool, str | None]:
    try:
        with Image.open(path) as image:
            image.verify()
        return True, None
    except (OSError, ValueError) as error:
        return False, str(error)


def prepare_dataset(raw_root: Path, output_root: Path, overwrite: bool) -> dict[str, object]:
    if output_root.exists():
        if not overwrite:
            raise FileExistsError(f"Output already exists: {output_root}. Use --overwrite to create a safe rebuild.")
        timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        output_root = output_root.parent / f"{output_root.name}__rebuild_{timestamp}"

    report: dict[str, object] = {
        "dataset_id": "signature_yolo_v1",
        "output_root": str(output_root),
        "created_at": datetime.now(timezone.utc).isoformat(),
        "class_names": ["signature"],
        "sources": [source_id for source_id, _ in SOURCES],
        "splits": {split: Counter() for split in SPLITS},
        "rejected": [],
    }

    for split in SPLITS:
        (output_root / split / "images").mkdir(parents=True, exist_ok=True)
        (output_root / split / "labels").mkdir(parents=True, exist_ok=True)

    for source_id, source_directory in SOURCES:
        source_root = raw_root / source_directory
        if not source_root.is_dir():
            raise FileNotFoundError(f"Missing staged source: {source_root}")

        for split in SPLITS:
            images_directory = source_root / split / "images"
            labels_directory = source_root / split / "labels"
            if not images_directory.is_dir() or not labels_directory.is_dir():
                raise FileNotFoundError(f"Missing YOLO split directories for {source_id}/{split}")

            image_files = sorted(path for path in images_directory.iterdir() if path.suffix.lower() in IMAGE_EXTENSIONS)
            for image_path in image_files:
                split_report: Counter = report["splits"][split]  # type: ignore[index]
                split_report["source_images"] += 1
                label_path = labels_directory / f"{image_path.stem}.txt"
                image_ok, image_error = valid_image(image_path)
                if not image_ok:
                    report["rejected"].append({"source": source_id, "file": str(image_path), "reason": f"invalid image: {image_error}"})  # type: ignore[index]
                    split_report["rejected_images"] += 1
                    continue
                if not label_path.is_file():
                    report["rejected"].append({"source": source_id, "file": str(image_path), "reason": "missing label"})  # type: ignore[index]
                    split_report["missing_labels"] += 1
                    continue

                normalized_lines: list[str] = []
                invalid_lines = 0
                for line_number, raw_line in enumerate(label_path.read_text(encoding="utf-8").splitlines(), start=1):
                    if not raw_line.strip():
                        continue
                    is_valid, reason = validate_label_line(raw_line)
                    if not is_valid:
                        report["rejected"].append({"source": source_id, "file": str(label_path), "reason": f"line {line_number}: {reason}"})  # type: ignore[index]
                        invalid_lines += 1
                        continue
                    _, x_center, y_center, width, height = raw_line.split()
                    normalized_lines.append(f"0 {x_center} {y_center} {width} {height}")

                if not normalized_lines:
                    split_report["empty_or_invalid_labels"] += 1
                    continue

                target_stem = f"{source_id}__{image_path.stem}"
                target_image = output_root / split / "images" / f"{target_stem}{image_path.suffix.lower()}"
                target_label = output_root / split / "labels" / f"{target_stem}.txt"
                shutil.copy2(image_path, target_image)
                target_label.write_text("\n".join(normalized_lines) + "\n", encoding="utf-8")
                split_report["accepted_images"] += 1
                split_report["accepted_boxes"] += len(normalized_lines)
                split_report["rejected_label_lines"] += invalid_lines

    yaml_content = (
        f"path: {output_root.as_posix()}\n"
        "train: train/images\n"
        "val: valid/images\n"
        "test: test/images\n\n"
        "nc: 1\n"
        "names:\n"
        "  0: signature\n"
    )
    (output_root / "data.yaml").write_text(yaml_content, encoding="utf-8")
    report["splits"] = {split: dict(counter) for split, counter in report["splits"].items()}  # type: ignore[union-attr]
    (output_root / "validation_report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    return report


def main() -> None:
    args = parse_arguments()
    try:
        report = prepare_dataset(args.raw_root.resolve(), args.output_root.resolve(), args.overwrite)
    except (FileNotFoundError, FileExistsError) as error:
        raise SystemExit(f"Dataset preparation failed: {error}") from error

    print(f"Prepared dataset: {report['output_root']}")
    for split, counters in report["splits"].items():
        print(f"{split}: {counters}")
    print(f"Rejected records: {len(report['rejected'])}")


if __name__ == "__main__":
    main()
