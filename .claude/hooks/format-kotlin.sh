#!/bin/sh
# PostToolUse hook for Write/Edit tools.
# Auto-formats .kt files using ktlint after modification.
# Exit 0 always (informational only, never blocks).

# Read stdin
INPUT=$(cat)

# Extract file_path field (fail-open if jq is unavailable or parse fails)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // ""' 2>/dev/null) || exit 0

# Only format .kt files
case "$FILE_PATH" in
  *.kt) ;;
  *) exit 0 ;;
esac

# Skip if file doesn't exist
[ -f "$FILE_PATH" ] || exit 0

# Run ktlint format on the project (fast when nothing needs formatting)
./gradlew ktlintFormat --quiet 2>&1 | tail -5

exit 0
