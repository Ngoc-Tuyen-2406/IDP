from __future__ import annotations

import re
import unicodedata


def normalize_text(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()


def fold_text(text: str) -> str:
    """Make Vietnamese and English keyword matching accent-insensitive."""
    decomposed = unicodedata.normalize("NFD", text.lower())
    without_marks = "".join(character for character in decomposed if unicodedata.category(character) != "Mn")
    return without_marks.replace("\u0111", "d")


def split_lines(text: str) -> list[str]:
    return [line.strip() for line in text.splitlines() if line.strip()]


def chunk_text(text: str, chunk_size: int = 700) -> list[str]:
    normalized = text.strip()
    if not normalized:
        return []
    lines = split_lines(normalized)
    chunks: list[str] = []
    current = ""
    for line in lines:
        if len(current) + len(line) + 1 > chunk_size and current:
            chunks.append(current.strip())
            current = line
        else:
            current = f"{current}\n{line}".strip()
    if current:
        chunks.append(current.strip())
    return chunks


def keyword_score(text: str, question: str) -> int:
    score = 0
    question_terms = {term for term in re.findall(r"[a-zA-Z0-9_]+", question.lower()) if len(term) > 2}
    body = fold_text(text)
    for term in question_terms:
        if term in body:
            score += 1
    return score
