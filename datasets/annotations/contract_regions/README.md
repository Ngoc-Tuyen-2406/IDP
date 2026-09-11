# Contract Region Annotation Standard

This is the project-owned annotation intake area for authorized and anonymized
Vietnamese contract pages. Do not place raw contracts here until their data-use
record is completed in `datasets/registry/VN_CONTRACT_INTAKE_TEMPLATE.csv`.

Use CVAT, Roboflow Annotate, Label Studio, or another annotation tool to export
YOLO detection labels with the following fixed class ids. LabelImg is not
required.

| Id | Class | Annotation rule |
| --- | --- | --- |
| 0 | `signature` | Inked handwritten signature only. |
| 1 | `seal` | Physical/company stamp or official seal impression. |
| 2 | `table` | Complete tabular region, including its border when visible. |
| 3 | `signature_block` | Entire signing area: signer title/name/signature/seal area. |

`signature` may overlap `signature_block`. `seal` may overlap `signature_block`.
Do not use `signature_block` to label every footer; it must be a genuine signing
area. Annotate every instance of an enabled class on a page.

## Required layout

```text
contract_regions/
  review_queue_v1/images/  review_queue_v1/prelabels/  review_queue_v1/previews/
  reviewed/images/  reviewed/labels/  reviewed/review_manifest.csv
```

Each label file has the same basename as its image. A row is:

```text
class_id x_center y_center width height
```

All coordinates must be normalized to `[0, 1]`. Keep document pages from the
same contract in one split only, otherwise evaluation leaks near-duplicate pages.

`review_queue_v1/` and `unassigned/` are annotation queues only. The
`prelabels/` folder contains suggestions, not ground truth. No file from either
queue may be used for training or evaluation.

## Review And Build

Generate the queue with `python scripts/prepare_contract_regions_review_queue.py`.
Review every object in CVAT, Roboflow Annotate, or Label Studio. Place the
reviewed image/label pairs in `reviewed/images/` and `reviewed/labels/`, then
set `review_status=approved` and `annotation_complete=true` in
`reviewed/review_manifest.csv`. Build the deterministic document-level split
with `python scripts/prepare_contract_regions_yolo_dataset.py`.
