#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
CONFIG_PATH="${1:-$ROOT_DIR/agent/.dev/config.yaml}"

if [ ! -f "$CONFIG_PATH" ]; then
  "$ROOT_DIR/scripts/dev-host-agent-config.sh" "$CONFIG_PATH"
fi

cd "$ROOT_DIR/agent"
exec go run ./cmd/host-agent --config "$CONFIG_PATH"
