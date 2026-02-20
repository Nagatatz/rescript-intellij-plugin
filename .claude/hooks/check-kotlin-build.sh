#!/bin/sh
# PostToolUse hook for Write/Edit tools.
# Runs async Kotlin compile check when a .kt file is modified.
# Exit 0 always (informational only, never blocks).

# Read stdin
INPUT=$(cat)

# Extract file_path field (fail-open if jq is unavailable or parse fails)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // ""' 2>/dev/null) || exit 0

# Only check .kt files
case "$FILE_PATH" in
  *.kt) ;;
  *) exit 0 ;;
esac

echo "Kotlin file changed: $FILE_PATH — running compile check..." >&2
./gradlew compileKotlin --quiet 2>&1 | tail -20
exit 0
