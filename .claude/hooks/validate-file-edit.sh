#!/bin/sh
# PreToolUse hook for Edit/Write tools.
# Blocks direct editing of auto-generated files (RescriptFlexLexer.java).
# Reads JSON from stdin, extracts tool_input.file_path, and checks against block patterns.
# Exit 0 = allow, Exit 2 = block.

# Read stdin
INPUT=$(cat)

# Extract file_path field (fail-open if jq is unavailable or parse fails)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // ""' 2>/dev/null) || exit 0

if [ -z "$FILE_PATH" ]; then
  exit 0
fi

# Block editing of auto-generated RescriptFlexLexer.java
if echo "$FILE_PATH" | grep -q 'RescriptFlexLexer\.java$'; then
  echo "BLOCKED: RescriptFlexLexer.java is auto-generated. Edit Rescript.flex instead." >&2
  exit 2
fi

exit 0
