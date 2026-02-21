#!/bin/sh
# PreToolUse hook for Edit/Write tools.
# Blocks prohibition language (禁止表現) in .claude/rules/*.md files.
# Enforces positive/affirmative phrasing in project rules.
# Reads JSON from stdin, checks content against prohibition patterns.
# Exit 0 = allow, Exit 2 = block.

# Read stdin
INPUT=$(cat)

# Extract file_path (fail-open if jq is unavailable or parse fails)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // ""' 2>/dev/null) || exit 0

if [ -z "$FILE_PATH" ]; then
  exit 0
fi

# Only check .claude/rules/*.md files
case "$FILE_PATH" in
  */.claude/rules/*.md) ;;
  *) exit 0 ;;
esac

# Extract content to check: new_string (Edit) or content (Write)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // ""' 2>/dev/null) || exit 0

if [ "$TOOL_NAME" = "Edit" ]; then
  CONTENT=$(echo "$INPUT" | jq -r '.tool_input.new_string // ""' 2>/dev/null) || exit 0
elif [ "$TOOL_NAME" = "Write" ]; then
  CONTENT=$(echo "$INPUT" | jq -r '.tool_input.content // ""' 2>/dev/null) || exit 0
else
  exit 0
fi

if [ -z "$CONTENT" ]; then
  exit 0
fi

# Check for prohibition patterns
if echo "$CONTENT" | grep -qE '禁止する|禁止とする|してはならない|してはいけない|することは禁止'; then
  cat >&2 <<'MSG'
BLOCKED: 禁止表現が検出されました。肯定的な表現に書き換えてください。

  ✗ "〜は禁止する" → ✓ "〜すること"
  ✗ "〜してはならない" → ✓ "〜しないこと" / "〜ではなく〜を使うこと"
  ✗ "〜してはいけない" → ✓ "〜する代わりに〜すること"

理由: 肯定的な表現は何をすべきかが明確になり、遵守率が向上します。
MSG
  exit 2
fi

exit 0
