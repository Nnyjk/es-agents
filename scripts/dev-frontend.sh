#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"

cd "$ROOT_DIR/frontend"

if [ ! -d node_modules ]; then
  npm ci --legacy-peer-deps
fi

exec npm run dev -- --host 0.0.0.0
