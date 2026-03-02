#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
OUTPUT_PATH="${1:-$ROOT_DIR/agent/.dev/config.yaml}"

HOST_AGENT_ID="${HOST_AGENT_ID:-local-host}"
HOST_AGENT_SECRET="${HOST_AGENT_SECRET:-local-secret}"
HOST_AGENT_PORT="${HOST_AGENT_PORT:-9090}"
HOST_AGENT_HEARTBEAT="${HOST_AGENT_HEARTBEAT:-30s}"

mkdir -p "$(dirname "$OUTPUT_PATH")"

cat >"$OUTPUT_PATH" <<EOF
listen_port: ${HOST_AGENT_PORT}
host_id: ${HOST_AGENT_ID}
secret_key: ${HOST_AGENT_SECRET}
heartbeat_interval: ${HOST_AGENT_HEARTBEAT}
EOF

echo "Wrote host-agent config to $OUTPUT_PATH"
