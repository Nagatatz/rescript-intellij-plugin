# 要求内容

## 背景

2026-05-11 のリポジトリ audit で「11 個の `*ToolWindowFactory` が `createToolWindowContent` を重複実装している」と指摘された。詳細を読み直すと、実際に同型なのは 6 個（Pattern A: panel 自身が Disposable）であり、残り 5 個（Pattern B: panel を `toolWindow.disposable` で受け取り、`panel.component` を content として登録）は構造が異なる。Pattern A の重複だけを共通化し、Pattern B はそのまま保持する。

## スコープ

### in-scope

- `util/` に Pattern A 用のヘルパー `RescriptToolWindowContent` を新規作成
- 以下 6 個の Pattern A factory を helper 経由に書き換える:
  1. `impact/RescriptTypeImpactToolWindowFactory`
  2. `flow/RescriptVariantFlowToolWindowFactory`
  3. `interop/RescriptInteropRiskToolWindowFactory`
  4. `migration/RescriptMigrationToolWindowFactory`
  5. `diagram/RescriptDependencyDiagramToolWindowFactory`
  6. `coverage/RescriptTypeCoverageToolWindowFactory`

### out-of-scope

- Pattern B (`ppx`, `repl`, `dependencies`, `preview`, `typeinfo`) の factory は触らない
  - 理由: panel の構築が `Panel(project, disposable)` で `panel.component` を返す型で、Disposer の取り回しが異なる。共通化するなら panel 側の API を揃える必要があり、本作業のスコープを超える
- 既存 panel クラスは変更しない
- `plugin.xml` の Extension Point 登録は変更しない（factory のクラス名・package は維持）
- ToolWindow ID 定数や `shouldBeAvailable` のリファクタは対象外

## 受け入れ条件

- 新規ヘルパー `util/RescriptToolWindowContent` が KDoc 付きで導入されている
- 6 個の factory が helper 経由に書き換わり、bytecode 上の挙動は等価
- `./gradlew ktlintCheck` グリーン
- `./gradlew clean buildPlugin` グリーン
- `./gradlew test` グリーン
- 既存テストの追加修正は不要（factory には既存テストがない — testing.md の UI 例外）
- helper 自体には `ContentFactory.getInstance()` を要するため単体テストを書かない（testing.md の「IDE ライフサイクル依存」相当）。tasklist にその理由を明記する

## 参照

- `.claude/rules/testing.md` — UI コンポーネント / IDE ライフサイクル依存の免除条項
- `.claude/rules/code-comments.md` — 新規 helper の KDoc 必須
- 2026-05-11 audit の "🟡 Medium 4." 項目
