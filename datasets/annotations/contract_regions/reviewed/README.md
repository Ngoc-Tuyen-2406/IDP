# Reviewed Contract Region Labels

This directory only contains human-reviewed ground truth. Do not copy
`review_queue_v1/prelabels/` here without checking every bounding box.

Required input layout:

```text
reviewed/
  images/
  labels/
  review_manifest.csv
```

Start from `review_queue_v1/review_manifest.csv`. For each completed page, set
`review_status` to `approved` and `annotation_complete` to `true`, keep its
`document_id`, and add the reviewer name. A reviewed page with no target object
must still have an empty `.txt` label file, proving it was exhaustively checked.

Run `python scripts/prepare_contract_regions_yolo_dataset.py` only after the
corresponding image and label files are present here.
