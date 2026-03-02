#!/usr/bin/env bash
set -euo pipefail

echo "=== Local Test Script for es-agents ==="
echo "Run all local checks before submitting a PR"
echo

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Default: run all
RUN_FRONTEND=false
RUN_SERVER=false
RUN_AGENT=false
RUN_LINT=false
RUN_ALL=true

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --frontend|-f)
            RUN_FRONTEND=true
            RUN_ALL=false
            shift
            ;;
        --server|-s)
            RUN_SERVER=true
            RUN_ALL=false
            shift
            ;;
        --agent|-a)
            RUN_AGENT=true
            RUN_ALL=false
            shift
            ;;
        --lint|-l)
            RUN_LINT=true
            RUN_ALL=false
            shift
            ;;
        --all)
            RUN_ALL=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo
            echo "Options:"
            echo "  --frontend, -f   Run frontend checks only"
            echo "  --server, -s     Run server checks only"
            echo "  --agent, -a      Run agent checks only"
            echo "  --lint, -l       Run repository lint only"
            echo "  --all            Run all checks (default)"
            echo "  --help, -h       Show this help"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

# If all is set, enable all
if [ "$RUN_ALL" = true ]; then
    RUN_FRONTEND=true
    RUN_SERVER=true
    RUN_AGENT=true
    RUN_LINT=true
fi

check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo -e "${RED}✗ $1 is not installed${NC}"
        return 1
    else
        echo -e "${GREEN}✓ $1 is available${NC}"
        return 0
    fi
}

FAILED=0
PASSED=0
SKIPPED=0

run_check() {
    local name="$1"
    local cmd="$2"
    
    echo -e "${BLUE}→ $name${NC}"
    if eval "$cmd"; then
        echo -e "${GREEN}✓ $name passed${NC}"
        ((PASSED++))
    else
        echo -e "${RED}✗ $name failed${NC}"
        ((FAILED++))
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
echo "=== Environment ==="
echo "Node.js: $(node --version)"
echo "npm: $(npm --version)"
echo "Java: $(java --version | head -1)"
echo "Go: $(go version)"
echo

# Frontend checks
if [ "$RUN_FRONTEND" = true ]; then
    echo "=== Frontend Checks ==="
    cd frontend
    
    run_check "Installing dependencies" "npm ci --legacy-peer-deps"
    run_check "Building" "npm run build"
    run_check "Running tests" "npm test"
    run_check "Type checking" "npx tsc --noEmit"
    
    cd ..
    echo
fi

# Server checks
if [ "$RUN_SERVER" = true ]; then
    echo "=== Server Checks ==="
    run_check "Running tests" "mvn -B test --file server/pom.xml"
    run_check "Building package" "mvn -B -DskipTests package --file server/pom.xml"
    echo
fi

# Agent checks
if [ "$RUN_AGENT" = true ]; then
    echo "=== Agent Checks ==="
    cd agent
    
    run_check "Running tests" "go test -v ./..."
    run_check "Running vet" "go vet ./..."
    run_check "Building Linux binary" "CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -trimpath -tags \"netgo osusergo\" -o bin/host-agent ./cmd/host-agent"
    run_check "Verifying Linux compatibility" "bash scripts/verify-linux-compat.sh bin/host-agent"
    
    cd ..
    echo
fi

# Repository lint
if [ "$RUN_LINT" = true ]; then
    echo "=== Repository Lint ==="
    run_check "Checking code style" "npx --yes prettier@3.5.3 --check \".github/**/*.{yml,yaml,md}\" \"docs/**/*.md\" \"frontend/src/**/*.{ts,tsx}\""
    
    echo -e "${BLUE}→ Checking for merge markers${NC}"
    if grep -R -nE --exclude-dir=.git "^(<<<<<<<|=======|>>>>>>>)" .; then
        echo -e "${RED}✗ Unresolved merge markers detected${NC}"
        ((FAILED++))
    else
        echo -e "${GREEN}✓ No merge markers found${NC}"
        ((PASSED++))
    fi
    echo
fi

# Summary
echo "=== Summary ==="
echo -e "${GREEN}Passed: $PASSED${NC}"
if [ $FAILED -gt 0 ]; then
    echo -e "${RED}Failed: $FAILED${NC}"
    exit 1
else
    echo -e "${GREEN}=== All Checks Passed! ===${NC}"
    echo "You can now commit and push your changes."
fi
