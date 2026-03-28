#!/bin/bash

# Agent Test Coverage Script
# Generates detailed coverage report for all Go packages

set -e

echo "Running tests with coverage..."

# Create coverage output directory
COVERAGE_DIR="coverage"
mkdir -p "$COVERAGE_DIR"

# Collect coverage for all packages
go test ./... -coverprofile="$COVERAGE_DIR/coverage.out" -covermode=atomic

# Show summary
echo ""
echo "=== Coverage Summary ==="
go tool cover -func="$COVERAGE_DIR/coverage.out"

# Generate HTML report
echo ""
echo "Generating HTML coverage report..."
go tool cover -html="$COVERAGE_DIR/coverage.out" -o="$COVERAGE_DIR/coverage.html"

echo ""
echo "Coverage report generated at: $COVERAGE_DIR/coverage.html"
echo "Open in browser: firefox $COVERAGE_DIR/coverage.html"