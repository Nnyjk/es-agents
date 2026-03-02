#!/usr/bin/env bash
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
SOURCE_DIR="${1:-$SCRIPT_DIR}"
BINARY_NAME="host-agent"
CONFIG_FILE="config.yaml"

echo "Updating HostAgent from $SOURCE_DIR..."

# Validate source directory
if [ ! -d "$SOURCE_DIR" ]; then
  echo "Error: Source directory not found: $SOURCE_DIR"
  exit 1
fi

# Validate source has required files
if [ ! -f "$SOURCE_DIR/$BINARY_NAME" ]; then
  echo "Error: $BINARY_NAME not found in source directory"
  exit 1
fi

# Stop existing process
echo "Stopping existing HostAgent..."
if [ -x "$SCRIPT_DIR/stop.sh" ]; then
  "$SCRIPT_DIR/stop.sh" || true
else
  PID_FILE="$SCRIPT_DIR/host-agent.pid"
  if [ -f "$PID_FILE" ]; then
    PID="$(cat "$PID_FILE")"
    if kill -0 "$PID" 2>/dev/null; then
      kill "$PID" || true
      sleep 1
    fi
    rm -f "$PID_FILE"
  fi
fi

# Backup config if exists
CONFIG_BACKUP=""
if [ -f "$SCRIPT_DIR/$CONFIG_FILE" ]; then
  echo "Backing up $CONFIG_FILE..."
  cp "$SCRIPT_DIR/$CONFIG_FILE" "$SCRIPT_DIR/${CONFIG_FILE}.bak"
  CONFIG_BACKUP="$SCRIPT_DIR/${CONFIG_FILE}.bak"
fi

# Replace files (preserve config.yaml)
echo "Replacing files..."
for FILE in "$BINARY_NAME" install.sh start.sh stop.sh update.sh; do
  if [ ! -f "$SOURCE_DIR/$FILE" ]; then
    echo "  Skipping $FILE (not in source)"
    continue
  fi
  if [ "$SOURCE_DIR/$FILE" = "$SCRIPT_DIR/$FILE" ]; then
    echo "  Skipping $FILE (same location)"
    continue
  fi
  cp "$SOURCE_DIR/$FILE" "$SCRIPT_DIR/$FILE"
  echo "  Updated $FILE"
done

# Restore config
if [ -n "$CONFIG_BACKUP" ] && [ -f "$CONFIG_BACKUP" ]; then
  echo "Restoring $CONFIG_FILE..."
  mv "$CONFIG_BACKUP" "$SCRIPT_DIR/$CONFIG_FILE"
fi

# Make scripts executable
chmod +x "$SCRIPT_DIR/$BINARY_NAME" "$SCRIPT_DIR/install.sh" "$SCRIPT_DIR/start.sh" "$SCRIPT_DIR/stop.sh" "$SCRIPT_DIR/update.sh" 2>/dev/null || true

echo ""
echo "HostAgent updated successfully."
echo "Run ./start.sh to start the new version."
