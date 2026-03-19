#!/usr/bin/env bash
set -euo pipefail

echo "[agent] tests"
(
  cd agent
  go test -v ./...
)

echo "[agent] vet"
(
  cd agent
  go vet ./...
)

echo "[agent] linux build"
(
  cd agent
  CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -trimpath -tags "netgo osusergo" -o bin/host-agent ./cmd/host-agent
)

echo "[agent] compatibility verification"
(
  cd agent
  bash scripts/verify-linux-compat.sh bin/host-agent
)
