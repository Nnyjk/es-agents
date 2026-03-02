#!/usr/bin/env bash
set -euo pipefail

echo "=== Local Test Script for es-agents ==="
echo "This script runs all local checks before submitting a PR"
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}✗ $1 is not installed${NC}"
        return 1
    else
        echo -e "${GREEN}✓ $1 is available${NC}"
        return 0
    fi
}

echo "=== Checking Dependencies ==="
DEPS_OK=true

check_command node || DEPS_OK=false
check_command npm || DEPS_OK=false
check_command java || DEPS_OK=false
check_command mvn || DEPS_OK=false
check_command go || DEPS_OK=false

if [ "$DEPS_OK" = false ]; then
    echo
    echo -e "${YELLOW}Some dependencies are missing. Please install them first.${NC}"
    echo "See docs/07-development/LOCAL-DEV-ENV.md for installation instructions."
    exit 1
fi

echo
echo "=== Node.js Version ==="
node --version
npm --version

echo
echo "=== Java Version ==="
java --version | head -1

echo
echo "=== Go Version ==="
go version

echo
echo "=== Running Frontend Checks ==="
cd frontend
echo "Installing dependencies..."
npm ci --legacy-peer-deps

echo "Building..."
npm run build

echo "Running tests..."
npm test

echo "Type checking..."
npx tsc --noEmit
cd ..

echo
echo "=== Running Server Checks ==="
mvn -B test --file server/pom.xml
mvn -B -DskipTests package --file server/pom.xml

echo
echo "=== Running Agent Checks ==="
cd agent
echo "Running tests..."
go test -v ./...

echo "Running vet..."
go vet ./...

echo "Building Linux binary..."
CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -trimpath -tags "netgo osusergo" -o bin/host-agent ./cmd/host-agent

echo "Verifying Linux compatibility..."
bash scripts/verify-linux-compat.sh bin/host-agent
cd ..

echo
echo "=== Repository Lint ==="
npx --yes prettier@3.5.3 --check ".github/**/*.{yml,yaml,md}" "docs/**/*.md" "frontend/src/**/*.{ts,tsx}"

echo "Checking for merge markers..."
if grep -R -nE --exclude-dir=.git "^(<<<<<<<|=======|>>>>>>>)" .; then
    echo -e "${RED}✗ Unresolved merge markers detected${NC}"
    exit 1
fi

echo
echo -e "${GREEN}=== All Checks Passed! ===${NC}"
echo "You can now commit and push your changes."
