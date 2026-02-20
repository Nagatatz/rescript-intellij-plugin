#!/bin/sh
# SessionStart hook: displays development environment info.
# Informational only (exit 0 always).

echo "=== ReScript IntelliJ Plugin Dev Environment ==="
echo "JDK:    $(java -version 2>&1 | head -1)"
echo "Node:   $(node --version 2>/dev/null || echo 'not found')"
echo "Branch: $(git branch --show-current 2>/dev/null || echo 'unknown')"
echo "================================================"
exit 0
