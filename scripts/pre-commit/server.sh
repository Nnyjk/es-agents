#!/usr/bin/env bash
set -euo pipefail

echo "[server] unit tests"
mvn -B test --file server/pom.xml

echo "[server] package build"
mvn -B -DskipTests package --file server/pom.xml
