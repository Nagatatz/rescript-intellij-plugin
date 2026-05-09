#!/bin/sh
# SessionStart hook: displays development environment info and worktree freshness.
# Informational only (exit 0 always; never blocks the session).

echo "=== ReScript IntelliJ Plugin Dev Environment ==="
echo "JDK:    $(java -version 2>&1 | head -1)"
echo "Node:   $(node --version 2>/dev/null || echo 'not found')"

BRANCH=$(git branch --show-current 2>/dev/null || echo 'unknown')
echo "Branch: $BRANCH"

# Worktree freshness vs. origin/main.
# Avoid network calls (no `git fetch` here) — surface the *currently known* gap.
# If the user wants the truly latest comparison, run `git fetch origin` manually first.
if git rev-parse --verify origin/main >/dev/null 2>&1; then
  AHEAD=$(git rev-list --count origin/main..HEAD 2>/dev/null || echo '?')
  BEHIND=$(git rev-list --count HEAD..origin/main 2>/dev/null || echo '?')
  echo "Sync:   ${AHEAD} ahead / ${BEHIND} behind origin/main (last known; run 'git fetch origin' to refresh)"
else
  echo "Sync:   origin/main not tracked locally"
fi

# Most recent .steering/ directory — surfaces the next free number to avoid collisions.
if [ -d .steering ]; then
  LATEST_STEERING=$(ls -1 .steering 2>/dev/null | grep -E '^[0-9]{8}-[0-9]{3}-' | sort | tail -1)
  if [ -n "$LATEST_STEERING" ]; then
    echo "Steering: latest = $LATEST_STEERING"
  fi
fi

# Active worktrees (excluding the main one) — flag potential parallel sessions.
WORKTREE_COUNT=$(git worktree list --porcelain 2>/dev/null | grep -c '^worktree ' || echo 0)
if [ "$WORKTREE_COUNT" -gt 1 ]; then
  echo "Worktrees: $WORKTREE_COUNT active (run 'git worktree list' to inspect)"
fi

echo "================================================"
exit 0
