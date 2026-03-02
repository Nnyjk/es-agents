#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "usage: $0 <output-binary-path>" >&2
  exit 1
fi

output_path="$1"
target_glibc_version="${TARGET_GLIBC_VERSION:-2.34}"

mkdir -p "$(dirname "$output_path")"

CGO_ENABLED="${CGO_ENABLED:-0}" \
GOOS="${GOOS:-linux}" \
GOARCH="${GOARCH:-amd64}" \
go build -trimpath -tags "${GO_BUILD_TAGS:-netgo osusergo}" -o "$output_path" ./cmd/host-agent

if command -v file >/dev/null 2>&1; then
  file_output="$(file "$output_path")"
  case "$file_output" in
    *"ELF 64-bit"*x86-64*) ;;
    *)
      echo "unexpected file metadata: $file_output" >&2
      exit 1
      ;;
  esac
fi

if command -v ldd >/dev/null 2>&1; then
  ldd_output="$(ldd "$output_path" 2>&1 || true)"
  case "$ldd_output" in
    *"not a dynamic executable"*|*"statically linked"*) ;;
    *)
      echo "host-agent must be statically linked, got: $ldd_output" >&2
      exit 1
      ;;
  esac
fi

if command -v readelf >/dev/null 2>&1; then
  program_headers="$(readelf -l "$output_path" 2>/dev/null || true)"
  if [[ "$program_headers" == *"INTERP"* ]]; then
    echo "static host-agent unexpectedly contains INTERP program header" >&2
    exit 1
  fi

  dynamic_section="$(readelf -d "$output_path" 2>/dev/null || true)"
  if [[ "$dynamic_section" == *"(NEEDED)"* ]]; then
    echo "static host-agent unexpectedly contains NEEDED entries" >&2
    exit 1
  fi
fi

if command -v objdump >/dev/null 2>&1; then
  objdump_headers="$(objdump -p "$output_path" 2>/dev/null || true)"
  if [[ "$objdump_headers" == *"NEEDED"* ]]; then
    echo "static host-agent unexpectedly contains dynamic loader metadata" >&2
    exit 1
  fi
fi

if command -v strings >/dev/null 2>&1; then
  glibc_symbols="$(strings "$output_path" | grep -o 'GLIBC_[0-9]\+\.[0-9]\+' || true)"
  if [[ -n "$glibc_symbols" ]]; then
    echo "detected unexpected GLIBC symbol references above target ${target_glibc_version}: $glibc_symbols" >&2
    exit 1
  fi
fi
