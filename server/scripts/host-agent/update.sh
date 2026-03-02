#!/usr/bin/env bash
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
SOURCE_DIR="${1:-$SCRIPT_DIR}"
BINARY_NAME="host-agent"

if [ -x "$SCRIPT_DIR/stop.sh" ]; then
  "$SCRIPT_DIR/stop.sh" || true
fi

for FILE in "$BINARY_NAME" install.sh start.sh stop.sh update.sh; do
  if [ ! -f "$SOURCE_DIR/$FILE" ]; then
    continue
  fi
  if [ "$SOURCE_DIR/$FILE" = "$SCRIPT_DIR/$FILE" ]; then
    continue
  fi
  cp "$SOURCE_DIR/$FILE" "$SCRIPT_DIR/$FILE"
done

chmod +x "$SCRIPT_DIR/$BINARY_NAME" "$SCRIPT_DIR/install.sh" "$SCRIPT_DIR/start.sh" "$SCRIPT_DIR/stop.sh" "$SCRIPT_DIR/update.sh"
echo "HostAgent binaries and scripts updated."
echo "config.yaml preserved at $SCRIPT_DIR/config.yaml"
echo "Run ./start.sh to start the new version."
