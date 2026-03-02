#!/usr/bin/env bash
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
PID_FILE="$SCRIPT_DIR/host-agent.pid"
LOG_DIR="$SCRIPT_DIR/logs"
LOG_FILE="$LOG_DIR/host-agent.log"
BINARY_PATH="$SCRIPT_DIR/host-agent"
CONFIG_PATH="$SCRIPT_DIR/config.yaml"

mkdir -p "$LOG_DIR"
chmod +x "$BINARY_PATH"

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "HostAgent already running with PID $(cat "$PID_FILE")"
  exit 0
fi

nohup "$BINARY_PATH" --config "$CONFIG_PATH" >> "$LOG_FILE" 2>&1 &
printf '%s' "$!" > "$PID_FILE"
echo "HostAgent started in background. PID: $(cat "$PID_FILE")"
echo "Log file: $LOG_FILE"
