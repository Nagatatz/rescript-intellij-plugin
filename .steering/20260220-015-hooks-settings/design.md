# Design: Hooks + settings.json

## 設計概要

Claude Code の設定システムを活用し、チーム共有のセキュリティ設定とフックスクリプトを導入する。

## ファイル構成

```
.claude/
├── settings.json              # チーム共有設定（新規作成）
└── hooks/                     # フックスクリプトディレクトリ（新規作成）
    ├── validate-bash.sh       # Bash コマンド検証フック
    └── validate-file-edit.sh  # ファイル編集検証フック
```

## 設計詳細

### 1. settings.json

```json
{
  "permissions": {
    "deny": [
      "Read(.env)",
      "Read(.env.*)",
      "Bash(rm -rf *)"
    ]
  },
  "env": {
    "BASH_DEFAULT_TIMEOUT_MS": "120000"
  },
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": ".claude/hooks/validate-bash.sh"
          }
        ]
      },
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": ".claude/hooks/validate-file-edit.sh"
          }
        ]
      }
    ]
  }
}
```

### 2. validate-bash.sh フロー

```
stdin (JSON) → jq で command 抽出 → パターンマッチ → exit 0 or exit 2
```

- `jq -r '.tool_input.command // ""'` で command フィールドを取得
- `grep -qE` でブロックパターンをマッチ:
  - `git add \.$` — `git add .` の完全一致
  - `git add -A` — `-A` フラグ検出
  - `git push --force` / `git push -f` — 強制プッシュ検出
  - `rm -rf` — 再帰的強制削除検出

### 3. validate-file-edit.sh フロー

```
stdin (JSON) → jq で file_path 抽出 → パターンマッチ → exit 0 or exit 2
```

- `jq -r '.tool_input.file_path // ""'` で file_path フィールドを取得
- `RescriptFlexLexer.java` を含むパスをブロック

## エラーハンドリング

- `jq` が利用不可の場合: スクリプトは exit 0 で許可（フェイルオープン）
- JSON パースに失敗した場合: exit 0 で許可（フェイルオープン）

## 影響範囲

- 新規ファイルの追加のみ。既存ファイルへの変更なし
- `.claude/settings.json` は Git にコミットされ、チームメンバー全員に共有される
