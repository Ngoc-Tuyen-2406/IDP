#!/usr/bin/env python3
"""Generate reviewable pseudo-label candidates for every unlabeled document image in datasets/raw."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import sys
from collections import Counter
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(PROJECT_ROOT / "ai-server"))

from services.auto_label_service import auto_label_service  # noqa: E402


IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff"}
SKIPPED_SOURCE_DIRECTORIES = {"contract_nli", "doclaynet_reference", "pubtables_reference", "signature_rf100", "signature_roboflow_amruth"}
CONTRACT_CATEGORIES = {"CONTRACTS", "FORMS", "TRADE DOCUMENTS"}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input-root", type=Path, default=PROJECT_ROOT / "datasets/raw")
    parser.add_argument("--output-root", type=Path, default=PROJECT_ROOT / "datasets/annotations/auto_label_candidates_contract_v1")
    parser.add_argument("--min-confidence", type=float, default=0.55)
    parser.add_argument(
        "--include-layout-warmup",
        action="store_true",
        help="Also scan non-contract NDL layout samples. Disabled to protect contract-domain quality.",
    )
    parser.add_argument("--overwrite", action="store_true")
    args = parser.parse_args()

    output_has_generated_content = args.output_root.exists() and any(
        item.name != "README.md" for item in args.output_root.iterdir()
    )
    if output_has_generated_content and not args.overwrite:
        raise SystemExit(f"Output exists: {args.output_root}. Use --overwrite to replace it.")
    if output_has_generated_content and args.output_root.exists():
        import shutil

        shutil.rmtree(args.output_root)
    labels_root = args.output_root / "labels"
    details_root = args.output_root / "details"
    labels_root.mkdir(parents=True)
    details_root.mkdir(parents=True)

    report: dict[str, object] = {
        "input_root": str(args.input_root),
        "minimum_confidence": args.min_confidence,
        "eligible_sources": ["vietnamese_ocr_archive/IMG_OCR_VIE_CN/CONTRACTS", "vietnamese_ocr_archive/IMG_OCR_VIE_CN/FORMS", "vietnamese_ocr_archive/IMG_OCR_VIE_CN/TRADE DOCUMENTS"],
        "scanned_images": 0,
        "skipped_non_contract_images": 0,
        "candidate_images": 0,
        "class_counts": Counter(),
        "errors": [],
    }
    manifest_rows: list[dict[str, str]] = []
    for image_path in args.input_root.rglob("*"):
        if not image_path.is_file() or image_path.suffix.lower() not in IMAGE_EXTENSIONS:
            continue
        relative_path = image_path.relative_to(args.input_root)
        if relative_path.parts and relative_path.parts[0] in SKIPPED_SOURCE_DIRECTORIES:
            continue
        is_vietnamese_contract_source = (
            len(relative_path.parts) >= 3
            and relative_path.parts[0] == "vietnamese_ocr_archive"
            and relative_path.parts[1] == "IMG_OCR_VIE_CN"
            and relative_path.parts[2] in CONTRACT_CATEGORIES
        )
        is_layout_warmup = args.include_layout_warmup and relative_path.parts and relative_path.parts[0] == "ndl_docl_sample"
        if not is_vietnamese_contract_source and not is_layout_warmup:
            report["skipped_non_contract_images"] += 1
            continue
        report["scanned_images"] += 1
        candidate_id = hashlib.sha1(relative_path.as_posix().encode("utf-8")).hexdigest()[:16]
        try:
            candidates = auto_label_service.detect(image_path, args.min_confidence)
        except ValueError as error:
            report["errors"].append({"image": str(relative_path), "error": str(error)})
            continue
        if not candidates:
            continue
        (labels_root / f"{candidate_id}.txt").write_text("\n".join(item.to_yolo() for item in candidates) + "\n", encoding="utf-8")
        (details_root / f"{candidate_id}.json").write_text(json.dumps({"image": str(image_path), "relative_path": str(relative_path), "candidates": [item.to_dict() for item in candidates]}, ensure_ascii=False, indent=2), encoding="utf-8")
        report["candidate_images"] += 1
        for candidate in candidates:
            report["class_counts"][candidate.label] += 1
        manifest_rows.append({"candidate_id": candidate_id, "source_image": str(image_path), "label_file": f"labels/{candidate_id}.txt", "detail_file": f"details/{candidate_id}.json", "status": "pending_review"})

    report["class_counts"] = dict(report["class_counts"])
    with (args.output_root / "manifest.csv").open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=("candidate_id", "source_image", "label_file", "detail_file", "status"))
        writer.writeheader()
        writer.writerows(manifest_rows)
    (args.output_root / "report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
