#!/usr/bin/env python3
"""Install a trained four-class YOLO artifact into the AI Server model registry."""

from __future__ import annotations

import argparse
import json
import shutil
from datetime import datetime, timezone
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--weights", type=Path, required=True, help="Colab-generated best.pt file.")
    parser.add_argument("--version", default="v1-bootstrap")
    parser.add_argument(
        "--description",
        default="Four-class contract-region YOLO bootstrap model. Validate on reviewed contract labels before production use.",
    )
    parser.add_argument(
        "--model-root",
        type=Path,
        default=PROJECT_ROOT / "ai-server" / "trained_models" / "contract_regions_yolo_v1" / "baseline",
    )
    parser.add_argument("--replace", action="store_true", help="Replace an existing installed best.pt after explicitly retraining the model.")
    args = parser.parse_args()

    weights = args.weights.resolve()
    if not weights.is_file() or weights.suffix.lower() != ".pt":
        raise SystemExit(f"Weights file not found or not a .pt file: {weights}")

    model_root = args.model_root.resolve()
    destination = model_root / "weights" / "best.pt"
    if destination.exists() and not args.replace:
        raise SystemExit(f"Installed model exists: {destination}. Use --replace only for a deliberate replacement.")
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(weights, destination)
    manifest = {
        "model_name": "idp-contract-regions-yolo11n",
        "model_type": "yolo",
        "version": args.version,
        "description": args.description,
        "classes": ["signature", "seal", "table", "signature_block"],
        "weights": "weights/best.pt",
        "input_size": 1024,
        "installed_at": datetime.now(timezone.utc).isoformat(),
    }
    (model_root / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Installed model: {destination}")
    print(f"Manifest: {model_root / 'manifest.json'}")


if __name__ == "__main__":
    main()
