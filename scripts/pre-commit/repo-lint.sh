#!/usr/bin/env bash
set -euo pipefail

echo "[lint] prettier check"
npx --yes prettier@3.5.3 --check \
  ".github/**/*.{yml,yaml,md}" \
  "docs/**/*.md" \
  "frontend/src/**/*.{ts,tsx}"

echo "[lint] merge markers"
if rg -n --hidden --glob '!.git' --glob '!**/node_modules/**' '^(<<<<<<<|=======|>>>>>>>)' .; then
  echo "Unresolved merge markers detected."
  exit 1
fi
