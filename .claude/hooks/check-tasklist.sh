#!/bin/sh
# Stop hook: checks for incomplete tasks in the latest tasklist.md.
# Warns if there are unchecked items. Never blocks (exit 0 always).

# Find the most recent tasklist.md in .steering/
LATEST=$(find .steering -name "tasklist.md" -type f 2>/dev/null | sort -r | head -1)

if [ -z "$LATEST" ]; then
  exit 0
fi

INCOMPLETE=$(grep -c '^\- \[ \]' "$LATEST" 2>/dev/null || echo "0")

if [ "$INCOMPLETE" -gt 0 ]; then
  echo "Warning: $LATEST has $INCOMPLETE incomplete task(s)" >&2
fi

exit 0
