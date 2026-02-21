# Requirements: Claude 設定強化（B + C1）

## 目的

ベストプラクティスレポートの知見に基づき、Hooks・Agent・Rules を強化する。

## 要件

### B1: PostToolUse 自動フォーマット Hook

ktlint 導入はプロジェクトの依存関係に影響するため見送り。代わりに IntelliJ の組み込みフォーマッタ連携や、既存の `check-kotlin-build.sh` の改善を検討する。→ **スキップ**（ktlint 未導入のため実現不可）

### B2: PreCompact 状態保存 Hook

コンパクション前にセッション状態（ブランチ、worktree、変更ファイル、steering パス）をファイルに保存する。

### B3: Notification Hook

macOS デスクトップ通知で Claude が注意を必要としていることを知らせる。

### B4: code-reviewer エージェント強化

レポートの失敗事例を反映したチェック項目を追加:
- テスト改ざん検出
- デッドコード検出
- エッジケース・条件分岐の確認

### B5: パス固有 Rules

- `flex-rules.md` (globs: `*.flex`) — JFlex 固有の注意事項
- `plugin-xml-rules.md` (globs: `**/plugin.xml`, `**/rescript-*.xml`) — extension point 登録規約

### C1: 禁止より代替指示への書き換え

Rules 内の「禁止する」表現を「〜すること」の肯定形に書き換える。

## 受け入れ条件

- B2/B3: Hook スクリプトが作成され settings.json に登録されている
- B4: code-reviewer.md にチェック項目が追加されている
- B5: 2つの新規 Rules ファイルが作成されている
- C1: Rules 内の主要な禁止表現が肯定形に書き換えられている
