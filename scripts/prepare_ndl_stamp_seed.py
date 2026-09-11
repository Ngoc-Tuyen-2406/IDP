#!/usr/bin/env python3
"""Export the NDL-DocL sample's real stamp boxes as a traceable seal seed set."""

from __future__ import annotations

import argparse
import json
import shutil
import xml.etree.ElementTree as element_tree
from pathlib import Path


IMAGE_EXTENSIONS = (".jpg", ".jpeg", ".png", ".tif", ".tiff")


def find_image(xml_path: Path) -> Path | None:
    for extension in IMAGE_EXTENSIONS:
        candidate = xml_path.with_suffix(extension)
        if candidate.is_file():
            return candidate
    return None


def main() -> None:
    project_root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-root", type=Path, default=project_root / "datasets/raw/ndl_docl_sample/layout-dataset-master/sample")
    parser.add_argument("--output-root", type=Path, default=project_root / "datasets/processed/ndl_stamp_seed_v1")
    parser.add_argument("--overwrite", action="store_true")
    args = parser.parse_args()

    if args.output_root.exists():
        if not args.overwrite:
            raise SystemExit(f"Output already exists: {args.output_root}. Use --overwrite to replace it.")
        shutil.rmtree(args.output_root)
    (args.output_root / "images").mkdir(parents=True)
    (args.output_root / "labels").mkdir(parents=True)

    report = {"source_dataset": "ndl_docl_sample", "source_label": "5_stamp", "target_class_id": 1, "accepted_images": 0, "accepted_boxes": 0, "skipped_xml_without_image": []}
    for xml_path in args.source_root.rglob("*.xml"):
        image_path = find_image(xml_path)
        if image_path is None:
            report["skipped_xml_without_image"].append(str(xml_path))
            continue
        root = element_tree.parse(xml_path).getroot()
        size = root.find("size")
        if size is None:
            continue
        width = float(size.findtext("width", "0"))
        height = float(size.findtext("height", "0"))
        if width <= 0 or height <= 0:
            continue
        labels = []
        for item in root.findall("object"):
            if item.findtext("name") != "5_stamp":
                continue
            box = item.find("bndbox")
            if box is None:
                continue
            xmin, ymin = float(box.findtext("xmin", "0")), float(box.findtext("ymin", "0"))
            xmax, ymax = float(box.findtext("xmax", "0")), float(box.findtext("ymax", "0"))
            box_width, box_height = (xmax - xmin) / width, (ymax - ymin) / height
            if box_width <= 0 or box_height <= 0:
                continue
            x_center, y_center = ((xmin + xmax) / 2) / width, ((ymin + ymax) / 2) / height
            labels.append(f"1 {x_center:.6f} {y_center:.6f} {box_width:.6f} {box_height:.6f}")
        if not labels:
            continue
        output_stem = "ndl_docl__" + xml_path.stem
        shutil.copy2(image_path, args.output_root / "images" / f"{output_stem}{image_path.suffix.lower()}")
        (args.output_root / "labels" / f"{output_stem}.txt").write_text("\n".join(labels) + "\n", encoding="utf-8")
        report["accepted_images"] += 1
        report["accepted_boxes"] += len(labels)
    (args.output_root / "validation_report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
