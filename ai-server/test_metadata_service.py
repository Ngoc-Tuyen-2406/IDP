import unittest

from services.metadata_service import metadata_service


class MetadataServiceTest(unittest.TestCase):
    def test_extracts_core_vietnamese_contract_fields(self) -> None:
        text = (
            "H\u1ee2P \u0110\u1ed2NG S\u1ed0: HD-2026/ABC-01\n"
            "Ng\u00e0y k\u00fd: 01/05/2026\n"
            "Ng\u00e0y hi\u1ec7u l\u1ef1c: 02/05/2026\n"
            "Ng\u00e0y h\u1ebft h\u1ea1n: 01/05/2027\n"
            "B\u00ean A: C\u00d4NG TY TNHH ALPHA\n"
            "B\u00ean B: C\u00d4NG TY C\u1ed4 PH\u1ea6N BETA\n"
            "M\u00e3 s\u1ed1 thu\u1ebf: 0312345678\n"
            "T\u1ed5ng gi\u00e1 tr\u1ecb h\u1ee3p \u0111\u1ed3ng: 1.250.000.000 VND"
        )

        result = metadata_service.extract_from_text(text)
        values = {field.field_name: field.current_value for field in result.fields}

        self.assertEqual(values["contract_number"], "HD-2026/ABC-01")
        self.assertEqual(values["signed_date"], "01/05/2026")
        self.assertEqual(values["effective_date"], "02/05/2026")
        self.assertEqual(values["expired_date"], "01/05/2027")
        self.assertEqual(values["party_a"], "C\u00d4NG TY TNHH ALPHA")
        self.assertEqual(values["party_b"], "C\u00d4NG TY C\u1ed4 PH\u1ea6N BETA")
        self.assertEqual(values["tax_code"], "0312345678")
        self.assertEqual(values["total_value"], "1.250.000.000 VND")


if __name__ == "__main__":
    unittest.main()
