#!/usr/bin/env bash
set -euo pipefail

echo "[frontend] installing dependencies"
npm --prefix frontend ci --legacy-peer-deps

echo "[frontend] build"
npm --prefix frontend run build

echo "[frontend] test"
npm --prefix frontend run test

echo "[frontend] type check"
npm --prefix frontend exec -- tsc --noEmit --project frontend/tsconfig.json
