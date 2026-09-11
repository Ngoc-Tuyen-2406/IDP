from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import json

from core.config import settings


@dataclass(slots=True)
class ModelDescriptor:
    model_name: str
    model_type: str | None
    version: str | None
    description: str | None
    path: Path


class ModelRegistry:
    def __init__(self, root: Path) -> None:
        self.root = root
        self.root.mkdir(parents=True, exist_ok=True)

    def discover(self) -> list[ModelDescriptor]:
        descriptors: list[ModelDescriptor] = []
        for manifest in self.root.rglob("*.json"):
            try:
                data = json.loads(manifest.read_text(encoding="utf-8"))
            except Exception:
                continue
            descriptors.append(
                ModelDescriptor(
                    model_name=data.get("model_name", manifest.stem),
                    model_type=data.get("model_type"),
                    version=data.get("version"),
                    description=data.get("description"),
                    path=manifest.parent,
                )
            )
        return descriptors

    def get_first_by_type(self, model_type: str) -> ModelDescriptor | None:
        for descriptor in self.discover():
            if descriptor.model_type == model_type:
                return descriptor
        return None


model_registry = ModelRegistry(settings.model_root)
