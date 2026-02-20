# Git 規約

## コミットメッセージ

コミットメッセージには以下の絵文字プレフィックスを付与すること:

| 絵文字 | 用途 | 判定条件 |
|-------|------|---------|
| ✨ | 新機能追加 | 新しいファイル追加、または新しい関数/コンポーネントを追加 |
| 🐛 | バグ修正 | 条件分岐の修正、例外処理の追加、既存ロジックの修正 |
| ♻️ | リファクタリング | 関数の抽出・統合、名前変更、構造変更（機能変更なし） |
| 📝 | ドキュメント更新 | `.md` ファイルのみの変更、またはコメントのみの追加・修正 |
| 🎨 | UI やスタイルの改善 | スタイル変更、CSS/レイアウト関連の変更 |
| ⚡ | パフォーマンス改善 | クエリ最適化、キャッシュ追加、アルゴリズム改善 |
| 🔧 | 設定ファイルの変更 | `build.gradle.kts`、`gradle.properties`、設定ファイルの変更 |
| ✅ | テスト追加・修正 | テストファイルの追加・修正 |
| 🗑️ | 不要コード削除 | ファイル削除、不要コードの除去（コード量が純減） |

**判定優先順位**: 複数の絵文字が該当する場合、上の表の優先順位に従う（✨ が最優先）。

**フォーマット**: `<絵文字> <動詞で始まる簡潔な英語説明>`

**例**:
- `✨ Add JSX token support to lexer`
- `🐛 Fix PDF parsing error for edge cases`
- `🔧 Configure ktlint and plugin verification`

## コミット粒度

**以下は強制的な行動指示であり、例外なく従うこと。**

コミットは**最低でも機能単位**で分割すること。複数の独立した機能を1つのコミットにまとめることは禁止する。

**原則:**
- 1つのコミットには1つの論理的な変更のみを含める
- 機能の実装コード + 対応するテスト + plugin.xml 登録は同一コミットに含めてよい
- ドキュメント更新（CLAUDE.md, README.md, docs/ 等）は、該当機能のコミットに含めるか、全機能実装後に1つのドキュメント更新コミットとしてまとめる
- tasklist.md の更新は各コミットに含めること

**禁止事項:**
- 複数の独立した機能を1つの巨大コミットにまとめること（例: 13個の新機能を1コミットにする）
- 「一括実装」のような粒度の粗いコミット

**例（良い例）:**
```
✨ Add completion confidence for ReScript
✨ Add ReScript-specific live template context
✨ Add enter handler for comment continuation
✨ Add smart join lines handler
📝 Update docs for A-priority features
```

**例（悪い例）:**
```
✨ Add 13 A-priority IDE features  ← 粒度が粗すぎる
```

## ブランチ運用ルール

**以下は強制的な行動指示であり、例外なく従うこと。**

機能追加・変更・バグ修正・リファクタリング・テスト追加など、コードの変更を伴う作業は**必ず `main` から新しいブランチを作成して行うこと**。`main` ブランチに直接コミットすることは禁止する。

**手順:**
1. `main` ブランチから作業用ブランチを作成する（ブランチ命名規則に従う）
2. 作業用ブランチで実装・コミットを行う
3. 完了後、`tasklist.md` のマージタスクを含む全タスクを `[x]` に更新してコミットする
4. ユーザーに `main` へのマージ可否を確認する
5. 承認後、`main` にマージしてブランチを削除する

**重要:** マージ前に `tasklist.md` の全タスク（マージタスク自体を含む）が `[x]` になっていることを確認すること。マージ後に tasklist を更新する運用は禁止する。

```bash
# ブランチ作成
git checkout main
git checkout -b feature/<機能名>

# 実装・コミット後、main にマージ
git checkout main
git merge feature/<機能名>
git branch -d feature/<機能名>
```

**例外:** 以下のケースでは `main` への直接コミットを許可する:
- タイポ修正、1行の設定変更など明らかに軽微な修正
- ステアリングドキュメント（`.steering/`）のみの変更
- `CLAUDE.md` や `docs/` のみのドキュメント更新

## ブランチ命名規則

| プレフィックス | 用途 | 例 |
|--------------|------|-----|
| `feature/` | 新機能追加 | `feature/jsx-highlighting` |
| `fix/` | バグ修正 | `fix/lexer-state-reset` |
| `refactor/` | リファクタリング | `refactor/token-types` |
| `docs/` | ドキュメント更新 | `docs/update-architecture` |
| `test/` | テスト追加・修正 | `test/lexer-edge-cases` |
| `chore/` | 設定・依存関係等 | `chore/update-dependencies` |
