#!/bin/bash
# Verify Linux binary compatibility (no GLIBC dependency required)
# Usage: ./verify-linux-compat.sh [binary_path]

set -e

BINARY="${1:-bin/host-agent-linux-amd64}"

echo "=== Verifying Linux Binary Compatibility ==="
echo "Binary: $BINARY"

if [ ! -f "$BINARY" ]; then
    echo "❌ Error: Binary not found: $BINARY"
    exit 1
fi

# Check if binary is ELF format
echo "Checking binary format..."
FILE_TYPE=$(file "$BINARY")
if [[ ! "$FILE_TYPE" =~ "ELF" ]]; then
    echo "❌ Error: Not a valid ELF binary"
    echo "File type: $FILE_TYPE"
    exit 1
fi
echo "✅ Binary format: ELF"

# Check for GLIBC dependencies
echo "Checking for GLIBC dependencies..."
if command -v ldd &> /dev/null; then
    LDD_OUTPUT=$(ldd "$BINARY" 2>&1 || true)
    if echo "$LDD_OUTPUT" | grep -qi "libc.so"; then
        echo "⚠️  Warning: Binary has libc.so dependency"
        echo "This may cause issues on systems with older GLIBC versions"
        echo "Consider recompiling with CGO_ENABLED=0"
        exit 1
    fi
    echo "✅ No GLIBC dependency detected"
else
    echo "⚠️  ldd not available, skipping dependency check"
fi

# Check architecture
echo "Checking architecture..."
ARCH=$(readelf -h "$BINARY" 2>/dev/null | grep "Machine:" | awk '{print $2}')
echo "Architecture: $ARCH"

if [[ "$ARCH" =~ "X86-64" ]] || [[ "$ARCH" =~ "ARM" ]]; then
    echo "✅ Architecture is supported"
else
    echo "⚠️  Unknown architecture: $ARCH"
fi

echo ""
echo "=== Verification Complete ==="
echo "✅ Binary is compatible with Linux systems"
