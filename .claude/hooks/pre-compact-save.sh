#!/bin/bash
# PreCompact Hook: Save session state before compaction
# This allows context to be recovered after /compact or auto-compaction

STATE_FILE="${CLAUDE_PROJECT_DIR:-.}/.claude/session-state.md"

cat > "$STATE_FILE" << EOF
# Session State (auto-saved before compaction)
- **Branch:** $(git branch --show-current 2>/dev/null || echo "unknown")
- **Worktrees:** $(git worktree list 2>/dev/null | grep -v "^$" || echo "none")
- **Modified files:** $(git diff --name-only 2>/dev/null | head -20 || echo "none")
- **Staged files:** $(git diff --cached --name-only 2>/dev/null | head -20 || echo "none")
- **Active steering:** $(ls -d .steering/2026* 2>/dev/null | tail -1 || echo "none")
- **Timestamp:** $(date '+%Y-%m-%d %H:%M:%S')
EOF

exit 0
