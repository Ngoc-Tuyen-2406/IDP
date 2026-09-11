# Dataset Readiness - 2026-08-22

## Staged datasets

| Dataset id | Local path | Verified contents | Readiness | Intended task |
| --- | --- | --- | --- | --- |
| `contract_nli` | `datasets/raw/contract_nli/contract-nli` | 423 train, 61 dev, 123 test NDA documents | Ready | NLI, evidence retrieval, risk evaluation |
| `signature_rf100` | `datasets/raw/signature_rf100` | 368 document images, YOLOv11 labels, class `signature` | Ready | Signature detection warm-up |
| `signature_roboflow_amruth` | `datasets/raw/signature_roboflow_amruth` | 2234 document images, YOLOv11 labels, single signature class | Ready | Signature detection warm-up |
| `ndl_docl_sample` | `datasets/raw/ndl_docl_sample/layout-dataset-master` | Sample images and Pascal VOC XML labels | Sample only | Stamp and table layout exploration |
| `doclaynet_reference` | `datasets/raw/doclaynet_reference/DocLayNet-main` | Repository, license, and labeling guide only | Not data | Download official dataset separately |
| `pubtables_reference` | `datasets/raw/pubtables_reference/table-transformer-main` | Repository and training instructions only | Not data | Download official dataset separately |

## License obligations

- ContractNLI: retain its `LICENSE` and `TERMS`; use under CC BY 4.0.
- Both Roboflow signature exports: retain attribution to their source projects; use under CC BY 4.0.
- NDL-DocL: retain attribution, state modifications, and assess privacy, moral, and other rights before publishing derived data.
- Do not distribute any archive or derived sample unless its upstream terms permit that publication.

## Blocks before final model training

1. No ready dataset for Vietnamese `seal` or Vietnamese contract layout is available yet.
2. DocLayNet and PubTables-1M data files still need their official dataset download.
3. The current sources are not a valid final evaluation set for Vietnamese contracts.
4. `vn_contracts_authorized` remains pending until written permission and anonymization are recorded.

## Next data action

Create a Vietnamese validation/test set from authorized anonymized contracts. For every
document, add one row to `VN_CONTRACT_INTAKE_TEMPLATE.csv` before it enters the raw
dataset directory.
