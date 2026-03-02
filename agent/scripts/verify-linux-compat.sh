#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
OUTPUT_PATH="${1:-$ROOT_DIR/bin/host-agent-linux-amd64}"
TARGET_GLIBC_VERSION="${TARGET_GLIBC_VERSION:-2.34}"

mkdir -p "$(dirname "$OUTPUT_PATH")"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

version_gt() {
  [ "$1" != "$2" ] && [ "$(printf '%s\n%s\n' "$1" "$2" | sort -V | tail -n 1)" = "$1" ]
}

collect_glibc_versions() {
  local binary_path="$1"
  {
    strings "$binary_path" 2>/dev/null || true
    readelf --version-info "$binary_path" 2>/dev/null || true
    objdump -T "$binary_path" 2>/dev/null || true
  } | grep -oE 'GLIBC_[0-9]+\.[0-9]+' | sort -Vu || true
}

verify_binary() {
  local binary_path="$1"
  local file_output ldd_output readelf_program_headers readelf_dynamic objdump_headers glibc_versions max_glibc_version

  file_output="$(file "$binary_path")"
  if [[ "$file_output" != *"ELF 64-bit"* ]] || [[ "$file_output" != *"x86-64"* ]]; then
    echo "Unexpected binary format: $file_output" >&2
    return 1
  fi

  ldd_output="$(ldd "$binary_path" 2>&1 || true)"
  if [[ "$ldd_output" == *"not found"* ]]; then
    echo "Dynamic dependency resolution failed:" >&2
    echo "$ldd_output" >&2
    return 1
  fi

  readelf_program_headers="$(readelf -l "$binary_path" 2>/dev/null || true)"
  readelf_dynamic="$(readelf -d "$binary_path" 2>/dev/null || true)"
  objdump_headers="$(objdump -p "$binary_path" 2>/dev/null || true)"
  glibc_versions="$(collect_glibc_versions "$binary_path")"

  max_glibc_version=""
  if [ -n "$glibc_versions" ]; then
    max_glibc_version="$(printf '%s\n' "$glibc_versions" | sed 's/^GLIBC_//' | sort -V | tail -n 1)"
  fi

  if [ -n "$max_glibc_version" ] && version_gt "$max_glibc_version" "$TARGET_GLIBC_VERSION"; then
    echo "Detected GLIBC symbol version ${max_glibc_version}, which exceeds target ${TARGET_GLIBC_VERSION}." >&2
    echo "ldd output:" >&2
    echo "$ldd_output" >&2
    echo "readelf --version-info output:" >&2
    readelf --version-info "$binary_path" >&2 || true
    echo "objdump -T output:" >&2
    objdump -T "$binary_path" >&2 || true
    return 1
  fi

  if [[ "$ldd_output" == *"not a dynamic executable"* ]] || [[ "$ldd_output" == *"statically linked"* ]]; then
    echo "Verified static linux host-agent binary: $binary_path"
    return 0
  fi

  if [[ "$readelf_program_headers" != *"INTERP"* ]]; then
    echo "Dynamic binary is missing INTERP program header." >&2
    return 1
  fi

  if [[ "$readelf_dynamic" != *"(NEEDED)"* ]] && [[ "$objdump_headers" != *"NEEDED"* ]]; then
    echo "Dynamic binary is missing NEEDED entries in readelf/objdump output." >&2
    return 1
  fi

  echo "Verified linux host-agent binary with GLIBC baseline <= ${TARGET_GLIBC_VERSION}: $binary_path"
  return 0
}

build_host_agent() {
  local build_mode="$1"
  local build_log

  build_log="$(mktemp)"
  case "$build_mode" in
    preferred)
      echo "Building host-agent with current toolchain settings..." >&2
      if (
        cd "$ROOT_DIR" &&
        GOOS=linux GOARCH=amd64 go build -trimpath -o "$OUTPUT_PATH" ./cmd/host-agent
      ) >"$build_log" 2>&1; then
        rm -f "$build_log"
        return 0
      fi
      echo "Primary build failed." >&2
      cat "$build_log" >&2
      rm -f "$build_log"
      return 1
      ;;
    static-fallback)
      echo "Rebuilding host-agent with static compatibility fallback (CGO_ENABLED=0)..." >&2
      if (
        cd "$ROOT_DIR" &&
        CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
          go build -trimpath -tags 'netgo osusergo' -o "$OUTPUT_PATH" ./cmd/host-agent
      ) >"$build_log" 2>&1; then
        rm -f "$build_log"
        return 0
      fi
      echo "Static compatibility fallback build failed." >&2
      cat "$build_log" >&2
      echo "Unable to produce a linux/amd64 binary compatible with GLIBC <= ${TARGET_GLIBC_VERSION}." >&2
      echo "If CGO is required, build inside an older glibc sysroot or ship a musl/static variant explicitly." >&2
      rm -f "$build_log"
      return 1
      ;;
    *)
      echo "Unknown build mode: $build_mode" >&2
      return 1
      ;;
  esac
}

require_command go
require_command ldd
require_command readelf
require_command objdump
require_command strings
require_command file

if build_host_agent preferred && verify_binary "$OUTPUT_PATH"; then
  exit 0
fi

echo "Falling back to a static linux build because the primary artifact did not meet the GLIBC baseline." >&2
build_host_agent static-fallback
verify_binary "$OUTPUT_PATH"
