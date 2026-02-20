#!/bin/sh
# PreToolUse hook for Bash tool.
# Blocks dangerous commands: git add ., git add -A, git push --force, rm -rf.
# Reads JSON from stdin, extracts tool_input.command, and checks against block patterns.
# Exit 0 = allow, Exit 2 = block.

# Read stdin
INPUT=$(cat)

# Extract command field (fail-open if jq is unavailable or parse fails)
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // ""' 2>/dev/null) || exit 0

if [ -z "$COMMAND" ]; then
  exit 0
fi

# Check for blocked patterns
# git add . (exact match at end of command or with trailing options)
if echo "$COMMAND" | grep -qE '(^|&&\s*|;\s*)git\s+add\s+\.$'; then
  echo "BLOCKED: 'git add .' is not allowed. Stage specific files instead." >&2
  exit 2
fi

# git add -A
if echo "$COMMAND" | grep -qE '(^|&&\s*|;\s*)git\s+add\s+(-\S*A|-A)'; then
  echo "BLOCKED: 'git add -A' is not allowed. Stage specific files instead." >&2
  exit 2
fi

# git push --force or git push -f
if echo "$COMMAND" | grep -qE '(^|&&\s*|;\s*)git\s+push\s+.*(-f|--force)'; then
  echo "BLOCKED: 'git push --force' is not allowed. Use regular push." >&2
  exit 2
fi

# rm -rf
if echo "$COMMAND" | grep -qE '(^|&&\s*|;\s*)rm\s+(-\S*r\S*f|(-\S*f\S*r)|-rf)'; then
  echo "BLOCKED: 'rm -rf' is not allowed. Remove files individually." >&2
  exit 2
fi

exit 0
