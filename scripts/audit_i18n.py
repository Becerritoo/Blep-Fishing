#!/usr/bin/env python3
from __future__ import annotations

import re
import sys
from pathlib import Path
from typing import Any

import yaml


REPO = Path(__file__).resolve().parents[1]
JAVA_DIR = REPO / "src" / "main" / "java"
LANG_DIR = REPO / "src" / "main" / "resources" / "lang"
ENGLISH = LANG_DIR / "en_US.yml"
ES_MX = LANG_DIR / "es_MX.yml"

GET_LANGUAGE_RE = re.compile(r'GetLanguageString\("([^"]+)"\)')
GET_FORMATTED_RE = re.compile(r'GetFormattedMessage\("([^"]+)"\)')
HARD_LITERAL_RE = re.compile(
    r'sendMessage\("([^"]+)"\)|thatExcludesNonPlayersWithMessage\("([^"]+)"\)|new Text\("([^"]+)"\)'
)


def load_yaml(path: Path) -> dict[str, Any]:
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    if data is None:
        return {}
    if not isinstance(data, dict):
        raise ValueError(f"{path} is not a YAML mapping at root")
    return data


def flatten_keys(data: Any, prefix: str = "") -> set[str]:
    if isinstance(data, dict):
        out: set[str] = set()
        for key, value in data.items():
            key_str = str(key)
            path = f"{prefix}.{key_str}" if prefix else key_str
            out |= flatten_keys(value, path)
        return out
    return {prefix}


def main() -> int:
    english = load_yaml(ENGLISH)
    es_mx = load_yaml(ES_MX)
    english_keys = flatten_keys(english)
    es_mx_keys = flatten_keys(es_mx)

    used_keys: set[str] = set()
    hard_literal_hits: list[tuple[Path, int, str]] = []

    for java_file in JAVA_DIR.rglob("*.java"):
        text = java_file.read_text(encoding="utf-8", errors="ignore")
        used_keys.update(GET_LANGUAGE_RE.findall(text))
        used_keys.update(GET_FORMATTED_RE.findall(text))

        for lineno, line in enumerate(text.splitlines(), 1):
            if HARD_LITERAL_RE.search(line):
                hard_literal_hits.append((java_file, lineno, line.strip()))

    missing_in_english = sorted(k for k in used_keys if k not in english_keys)
    missing_in_es = sorted(k for k in english_keys if k not in es_mx_keys)

    print("== BlepFishing i18n audit ==")
    print(f"Used language keys in code: {len(used_keys)}")
    print(f"English keys: {len(english_keys)}")
    print(f"es_MX keys: {len(es_mx_keys)}")
    print(f"Hardcoded direct message literals: {len(hard_literal_hits)}")

    if missing_in_english:
        print("\n[FAIL] Missing keys in en_US.yml:")
        for key in missing_in_english:
            print(f"- {key}")

    if missing_in_es:
        print("\n[FAIL] Missing keys in es_MX.yml (vs en_US.yml):")
        for key in missing_in_es[:200]:
            print(f"- {key}")
        if len(missing_in_es) > 200:
            print(f"... and {len(missing_in_es) - 200} more")

    if hard_literal_hits:
        print("\n[FAIL] Direct hardcoded message literals detected:")
        for file_path, line_no, snippet in hard_literal_hits:
            rel = file_path.relative_to(REPO)
            print(f"- {rel}:{line_no} :: {snippet}")

    if not (missing_in_english or missing_in_es or hard_literal_hits):
        print("\n[PASS] i18n audit completed with no blocking issues.")
        return 0
    return 1


if __name__ == "__main__":
    sys.exit(main())
