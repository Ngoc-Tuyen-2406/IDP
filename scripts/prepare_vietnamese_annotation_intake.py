#!/usr/bin/env python3
"""Copy user-supplied Vietnamese contract/table pages into a manual annotation queue."""

from __future__ import annotations

import argparse
import csv
import json
import shutil
from pathlib import Path


def main() -> None:
    project_root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--source-root",
        type=Path,
        default=project_root / "datasets/raw/vietnamese_ocr_archive/IMG_OCR_VIE_CN",
    )
    parser.add_argument(
        "--output-root",
        type=Path,
        default=project_root / "datasets/annotations/contract_regions/unassigned",
    )
    parser.add_argument("--overwrite", action="store_true")
    args = parser.parse_args()

    images_root = args.output_root / "images"
    json_root = args.output_root / "ocr_json"
    manifest_path = args.output_root / "annotation_manifest.csv"
    if args.output_root.exists() and not args.overwrite:
        raise SystemExit(f"Annotation queue already exists: {args.output_root}. Use --overwrite to replace it.")
    if args.output_root.exists():
        shutil.rmtree(args.output_root)
    images_root.mkdir(parents=True)
    json_root.mkdir(parents=True)

    tasks: list[dict[str, str]] = []
    groups = (("CONTRACTS", "contract_page"), ("FORMS", "table_page"))
    for group_name, task_type in groups:
        source_group = args.source_root / group_name
        for image_path in sorted(source_group.glob("*")):
            if image_path.suffix.lower() not in {".jpg", ".jpeg", ".png"}:
                continue
            if task_type == "table_page" and "TABLE" not in image_path.stem.upper():
                continue
            json_path = image_path.with_suffix(".json")
            target_stem = f"vietnamese_ocr__{group_name.lower()}__{image_path.stem}"
            target_image = images_root / f"{target_stem}{image_path.suffix.lower()}"
            shutil.copy2(image_path, target_image)
            if json_path.is_file():
                shutil.copy2(json_path, json_root / f"{target_stem}.json")
            tasks.append(
                {
                    "image": target_image.name,
                    "task_type": task_type,
                    "required_classes": "signature,seal,table,signature_block",
                    "ocr_json": f"ocr_json/{target_stem}.json" if json_path.is_file() else "",
                    "status": "pending_manual_annotation",
                }
            )

    with manifest_path.open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=("image", "task_type", "required_classes", "ocr_json", "status"))
        writer.writeheader()
        writer.writerows(tasks)
    report = {"source_dataset": "vietnamese_ocr_archive", "license_status": "quarantined_pending_verification", "queued_images": len(tasks), "manifest": str(manifest_path)}
    (args.output_root / "intake_report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
