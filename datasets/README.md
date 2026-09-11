# IDP Dataset Workspace

This directory is the local workspace for training and evaluation data. Raw files,
derived images, annotations, and checkpoints are ignored by Git.

## Directory layout

```text
datasets/
  registry/       Tracked source, license, and usage records.
  raw/            Original downloaded datasets. Never edit in place.
  processed/      Normalized pages, OCR inputs, and split manifests.
  annotations/    Project-owned annotations for Vietnamese contracts.
  exports/        YOLO, RAG, and evaluation exports.
```

## Rules

1. Read `registry/DATA_REGISTER.csv` before using a dataset.
2. Preserve each dataset's LICENSE, TERMS, README, and source URL.
3. Do not publish raw contracts, signatures, seals, PII, or source archives.
4. Derived datasets must record the source dataset ids and transformation date.
5. Vietnamese contracts must be anonymized and approved before being copied to
   `raw/vn_contracts_authorized/`.

## Staging downloaded archives

The current downloaded archives can be staged with:

```powershell
cd C:\Users\DELL\OneDrive\Desktop\IDP-System
.\scripts\Stage-Datasets.ps1 -SourceRoot C:\Users\DELL\Downloads
```

The command never overwrites a completed destination. If staging is interrupted,
review the exact partial directory and run the script with `-Force` for that dataset:

```powershell
.\scripts\Stage-Datasets.ps1 -SourceRoot C:\Users\DELL\Downloads -DatasetIds contract_nli -Force
```
