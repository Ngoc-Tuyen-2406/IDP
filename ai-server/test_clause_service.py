import unittest

from services.clause_service import clause_service
from services.risk_service import risk_service
from schemas.pipeline import RiskRequest


class ClauseServiceTest(unittest.TestCase):
    def test_extracts_reviewable_vietnamese_clauses(self) -> None:
        text = (
            "\u0110i\u1ec1u 1. Thanh to\u00e1n\n"
            "B\u00ean B thanh to\u00e1n trong 30 ng\u00e0y.\n"
            "\u0110i\u1ec1u 2. B\u1ea3o m\u1eadt\n"
            "Hai b\u00ean c\u00f3 ngh\u0129a v\u1ee5 b\u1ea3o m\u1eadt th\u00f4ng tin.\n"
            "\u0110i\u1ec1u 3. Ch\u1ea5m d\u1ee9t h\u1ee3p \u0111\u1ed3ng\n"
            "M\u1ed7i b\u00ean c\u00f3 quy\u1ec1n ch\u1ea5m d\u1ee9t khi vi ph\u1ea1m nghi\u00eam tr\u1ecdng."
        )

        result = clause_service.extract_from_text(text)
        clause_types = {clause.clause_type for clause in result.clauses}

        self.assertEqual({"payment", "confidentiality", "termination"}, clause_types)
        self.assertTrue(all(clause.text for clause in result.clauses))

    def test_risk_uses_detected_clauses(self) -> None:
        result = risk_service.analyze(
            RiskRequest(
                text="Thanh to\u00e1n. B\u1ea3o m\u1eadt th\u00f4ng tin. Ch\u1ea5m d\u1ee9t h\u1ee3p \u0111\u1ed3ng.",
                metadata=[],
            )
        )

        self.assertNotIn("Payment clause is not clearly detected.", result.risk_summary)
        self.assertNotIn("Confidentiality clause is not clearly detected.", result.risk_summary)
        self.assertNotIn("Termination condition is not clearly detected.", result.risk_summary)


if __name__ == "__main__":
    unittest.main()
