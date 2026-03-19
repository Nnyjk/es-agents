#!/usr/bin/env bash
set -euo pipefail

echo "[frontend] installing dependencies"
npm --prefix frontend ci --legacy-peer-deps

echo "[frontend] build"
npm --prefix frontend run build

echo "[frontend] test"
npm --prefix frontend run test

echo "[frontend] type check"
npx --yes tsc --project frontend/tsconfig.json --noEmit
