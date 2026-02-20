# Design: 残りの Claude Code 設定強化

## 1. Path-specific Rules

各ルールファイルの先頭に YAML フロントマターを追加:

### `testing.md`
```yaml
---
paths:
  - "src/**/*.kt"
---
```

### `code-comments.md`
```yaml
---
paths:
  - "src/main/**/*.kt"
---
```

### `documentation.md`
```yaml
---
paths:
  - "**/*.md"
  - "docs/**"
  - ".steering/**"
---
```

### `git-conventions.md` / `steering-workflow.md`
パス制限なし（フロントマター追加しない）。

## 2. PostToolUse Hook

`.claude/hooks/check-kotlin-build.sh`:

```sh
#!/bin/sh
INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // ""' 2>/dev/null) || exit 0

# .kt ファイル以外はスキップ
case "$FILE_PATH" in
  *.kt) ;;
  *) exit 0 ;;
esac

echo "Kotlin file changed: $FILE_PATH — running compile check..." >&2
./gradlew compileKotlin --quiet 2>&1 | tail -20
exit 0
```

`settings.json` の `hooks` に追加:

```json
"PostToolUse": [
  {
    "matcher": "Write|Edit",
    "hooks": [
      {
        "type": "command",
        "command": ".claude/hooks/check-kotlin-build.sh",
        "timeout": 120
      }
    ]
  }
]
```

## 3. Stop Hook

`.claude/hooks/check-tasklist.sh`:

```sh
#!/bin/sh
# 最新の tasklist.md を検索
LATEST=$(find .steering -name "tasklist.md" -type f 2>/dev/null | sort -r | head -1)
if [ -z "$LATEST" ]; then
  exit 0
fi

INCOMPLETE=$(grep -c '^\- \[ \]' "$LATEST" 2>/dev/null || echo "0")
if [ "$INCOMPLETE" -gt 0 ]; then
  echo "⚠️ $LATEST に未完了タスクが $INCOMPLETE 件あります" >&2
fi
exit 0
```

`settings.json` の `hooks` に追加:

```json
"Stop": [
  {
    "matcher": ".*",
    "hooks": [
      {
        "type": "command",
        "command": ".claude/hooks/check-tasklist.sh",
        "timeout": 10
      }
    ]
  }
]
```

## 4. SessionStart Hook

`.claude/hooks/session-info.sh`:

```sh
#!/bin/sh
echo "=== ReScript IntelliJ Plugin Dev Environment ==="
echo "JDK:    $(java -version 2>&1 | head -1)"
echo "Node:   $(node --version 2>/dev/null || echo 'not found')"
echo "Branch: $(git branch --show-current 2>/dev/null || echo 'unknown')"
echo "================================================"
exit 0
```

`settings.json` の `hooks` に追加:

```json
"SessionStart": [
  {
    "matcher": ".*",
    "hooks": [
      {
        "type": "command",
        "command": ".claude/hooks/session-info.sh",
        "timeout": 10
      }
    ]
  }
]
```

## 5. MCP サーバー設定

Context7 MCP はユーザーレベルで構成済み。プロジェクト共有用の `.mcp.json` を作成し、IDE MCP サーバー設定を記録する。

## 影響範囲

- `.claude/rules/testing.md` — フロントマター追加
- `.claude/rules/code-comments.md` — フロントマター追加
- `.claude/rules/documentation.md` — フロントマター追加
- `.claude/hooks/check-kotlin-build.sh` — 新規作成
- `.claude/hooks/check-tasklist.sh` — 新規作成
- `.claude/hooks/session-info.sh` — 新規作成
- `.claude/settings.json` — PostToolUse, Stop, SessionStart hooks 追加
- `.mcp.json` — 新規作成
