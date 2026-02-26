#!/usr/bin/env python3
"""Validate frontend service API calls against backend JAX-RS resources.

Usage:
  python3 scripts/validate_api_contract.py
  python3 scripts/validate_api_contract.py --repo /path/to/repo --strict

Exit code:
  0: no missing mappings
  1: missing mappings found (only when --strict is enabled)
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

FE_METHOD_RE = re.compile(r"request\.(get|post|put|delete)\(\s*([`\"][^`\"]*[`\"])")
BE_METHOD_RE = re.compile(r"@(GET|POST|PUT|DELETE)\b")
PATH_RE = re.compile(r'@Path\("([^"]+)"\)')


def normalize_path(raw: str, placeholder_pattern: str) -> str:
    normalized = raw.strip()
    normalized = re.sub(placeholder_pattern, "{var}", normalized)
    if not normalized.startswith("/"):
        normalized = f"/{normalized}"
    normalized = re.sub(r"//+", "/", normalized)
    return normalized


def parse_frontend_endpoints(repo: Path) -> set[tuple[str, str, str]]:
    endpoints: set[tuple[str, str, str]] = set()
    for file_path in (repo / "frontend/src/services").glob("*.ts"):
        content = file_path.read_text(encoding="utf-8")
        for match in FE_METHOD_RE.finditer(content):
            method = match.group(1).upper()
            raw_path = match.group(2)[1:-1]
            path = normalize_path(raw_path, r"\$\{[^}]+\}")
            endpoints.add((method, path, file_path.relative_to(repo).as_posix()))
    return endpoints


def parse_backend_endpoints(repo: Path) -> set[tuple[str, str, str]]:
    endpoints: set[tuple[str, str, str]] = set()
    for file_path in (repo / "server/src/main/java/com/easystation").glob("**/resource/*.java"):
        lines = file_path.read_text(encoding="utf-8").splitlines()

        base_path = ""
        for line in lines:
            base_match = PATH_RE.search(line)
            if base_match:
                base_path = base_match.group(1)
                break

        i = 0
        while i < len(lines):
            method_match = BE_METHOD_RE.search(lines[i])
            if not method_match:
                i += 1
                continue

            method = method_match.group(1)
            sub_path = ""
            j = i + 1
            while j < len(lines) and lines[j].strip().startswith("@"):
                path_match = PATH_RE.search(lines[j])
                if path_match:
                    sub_path = path_match.group(1)
                j += 1

            full_path = f"{base_path.rstrip('/')}/{sub_path.lstrip('/')}" if sub_path else base_path
            path = normalize_path(full_path, r"\{[^}]+\}")
            endpoints.add((method, path, file_path.relative_to(repo).as_posix()))
            i = j

    return endpoints


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo", type=Path, default=Path(__file__).resolve().parents[1], help="Repository root")
    parser.add_argument("--strict", action="store_true", help="Exit with non-zero status when mappings are missing")
    args = parser.parse_args()

    repo = args.repo.resolve()
    frontend_endpoints = parse_frontend_endpoints(repo)
    backend_endpoints = parse_backend_endpoints(repo)

    backend_method_path = {(method, path) for method, path, _ in backend_endpoints}
    missing = [
        (method, path, src)
        for method, path, src in sorted(frontend_endpoints)
        if (method, path) not in backend_method_path
    ]

    print(f"Frontend endpoints: {len(frontend_endpoints)}")
    print(f"Backend endpoints: {len(backend_endpoints)}")
    print(f"Missing mappings: {len(missing)}")

    for method, path, src in missing:
        print(f"MISSING {method} {path} from {src}")

    if args.strict and missing:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
