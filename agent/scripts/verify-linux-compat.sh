#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
OUTPUT_PATH="${1:-$ROOT_DIR/bin/host-agent-linux-amd64}"

mkdir -p "$(dirname "$OUTPUT_PATH")"

cd "$ROOT_DIR"
CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -trimpath -o "$OUTPUT_PATH" ./cmd/host-agent

if strings "$OUTPUT_PATH" | grep -q 'GLIBC_2\.34'; then
  echo "GLIBC_2.34 dependency detected in $OUTPUT_PATH" >&2
  exit 1
fi

echo "Verified linux host-agent without GLIBC_2.34 dependency: $OUTPUT_PATH"
