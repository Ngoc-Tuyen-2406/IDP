# Contract Auto-Label Candidates v1

Generated output from `scripts/generate_auto_label_candidates.py` is stored
here. The script scans the contract-domain sources under `datasets/raw/` and
writes reviewable YOLO pseudo-labels without copying or changing source images.

Candidates from `pillow_grid`, `pillow_red_stamp`, and
`ocr_signature_keywords` are not ground truth. They must be reviewed before
promotion to the four-class training dataset.
