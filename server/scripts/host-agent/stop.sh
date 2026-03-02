#!/usr/bin/env bash
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
PID_FILE="$SCRIPT_DIR/host-agent.pid"

if [ ! -f "$PID_FILE" ]; then
  echo "HostAgent is not running."
  exit 0
fi

PID="$(cat "$PID_FILE")"
if kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  echo "HostAgent stopped."
else
  echo "HostAgent process not found, removing stale PID file."
fi
rm -f "$PID_FILE"
