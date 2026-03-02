#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"

export DB_URL="${DB_URL:-jdbc:postgresql://127.0.0.1:5432/easy_station}"
export DB_USER="${DB_USER:-postgres}"
export DB_PASSWORD="${DB_PASSWORD:-postgres}"

cd "$ROOT_DIR/server"
exec mvn quarkus:dev --file pom.xml
