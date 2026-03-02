#!/usr/bin/env bash
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
BINARY_PATH="$SCRIPT_DIR/host-agent"
CONFIG_PATH="$SCRIPT_DIR/config.yaml"
LOG_DIR="$SCRIPT_DIR/logs"
LOG_FILE="$LOG_DIR/host-agent.log"
PID_FILE="$SCRIPT_DIR/host-agent.pid"

echo "Installing HostAgent..."

# Validate binary exists
if [ ! -f "$BINARY_PATH" ]; then
  echo "Error: host-agent binary not found at $BINARY_PATH"
  exit 1
fi

# Validate config exists
if [ ! -f "$CONFIG_PATH" ]; then
  echo "Error: config.yaml not found at $CONFIG_PATH"
  echo "Please create config.yaml with host_id and secret_key before installing."
  exit 1
fi

# Create log directory
mkdir -p "$LOG_DIR"

# Make binary executable
chmod +x "$BINARY_PATH"

# Check if already running
if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "HostAgent is already running with PID $(cat "$PID_FILE")"
  echo "Stopping existing process..."
  "$SCRIPT_DIR/stop.sh" || true
fi

# Start in background
echo "Starting HostAgent in background..."
nohup "$BINARY_PATH" --config "$CONFIG_PATH" >> "$LOG_FILE" 2>&1 &
printf '%s' "$!" > "$PID_FILE"

echo "HostAgent installed and started successfully."
echo "PID: $(cat "$PID_FILE")"
echo "Log file: $LOG_FILE"
echo ""
echo "Useful commands:"
echo "  Status: ps -p $(cat "$PID_FILE") || echo 'not running'"
echo "  Stop:   ./stop.sh"
echo "  Logs:   tail -f $LOG_FILE"
