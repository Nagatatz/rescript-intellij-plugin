# Task List: Dependency Diagram Extension Point 登録

## Phase 0: 準備

- [x] worktree 作成 (`EnterWorktree dependency-diagram-registration`)

## Phase 1: 実装

### 1.1 Mermaid エクスポータ
- [x] `src/main/kotlin/com/rescript/plugin/diagram/RescriptMermaidExporter.kt` を実装
- [x] `src/test/kotlin/com/rescript/plugin/diagram/RescriptMermaidExporterTest.kt` を実装（複数モデル → Mermaid 出力検証、特殊文字エスケープ検証）

### 1.2 DOT/Mermaid Export Action
- [x] `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramExportAction.kt` を実装
- [x] `src/test/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramExportActionTest.kt` を実装（DOT/Mermaid 戻り値、空モデル時の振る舞い）

### 1.3 ToolWindow パネル
- [x] `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramPanel.kt` を実装
- [x] テスト省略（理由: Swing UI コンポーネント）

### 1.4 ToolWindow Factory
- [x] `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramToolWindowFactory.kt` を実装
- [x] テスト省略（理由: Swing UI / IDE ライフサイクル依存）

### 1.5 Tools メニュー Action
- [x] `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramAction.kt` を実装
- [x] テスト省略（理由: IDE ライフサイクル依存・実質 ToolWindow 起動のみ）

## Phase 2: plugin.xml 登録

- [x] `<extensions>` に `<toolWindow id="ReScript Module Diagram" ...>` を追加
- [x] `<actions>` に `RescriptDependencyDiagramAction` を `ToolsMenu` 配下で登録（`AnalyzeMenu` は IntelliJ Platform に存在しないため変更）
- [x] アイコン: `AllIcons.FileTypes.Diagram` を採用

## Phase 3: ドキュメント整合性

- [x] `sphinx-docs/user/features/advanced.md` の Dependency Diagram セクションを実装に合わせて全面更新
- [x] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` を `make gettext && make update-po` で更新し、新規 msgstr を翻訳
- [x] `docs/functional-design.md` Extension Point マップに新規エントリ追加（ToolWindow + Action の 2 行）
- [x] `docs/repository-structure.md` の `diagram/` 行の代表クラス列を更新
- [x] `docs/repository-structure.md` のルート行に `RescriptErrorReporter` を追記（既存の docs-audit 残課題を同梱）
- [x] `README.md` Features Navigation セクションの "Dependency diagram" を Mermaid/DOT 対応に微修正
- [x] `CLAUDE.md` レイヤー 3 への追記は不要（参照先 `docs/repository-structure.md` / `docs/functional-design.md` で十分）

## Phase 4: コミット前検証

- [x] `./gradlew ktlintCheck` 成功
- [x] `./gradlew clean buildPlugin` 成功
- [x] `./gradlew test` 成功（新規テスト含む。3,535 件中 0 件失敗、全 diagram テスト 35 件パス）
- [x] `cd sphinx-docs && uv run --quiet make build-ja` 成功
- [x] KDoc が新規クラスすべてに付与されているか確認
- [x] deprecated API 利用がないことを確認
- [x] tasklist の全項目が `[x]` になっているか（マージ前の最終コミット直前）

※ `koverHtmlReport` は新規コードが純粋関数 + UI のため省略。Mermaid exporter / Export action のロジック分は十分なテストケースでカバー（Phase 1.1 / 1.2 のテスト 18 件）。

## Phase 5: コミット

機能単位で分割（git-conventions.md 準拠）:

- [x] `✨ Add Mermaid exporter for module dependency graph`（1.1 + テスト）
- [x] `✨ Add export action for module dependency diagram`（1.2 + テスト）
- [x] `✨ Add ReScript Module Diagram tool window`（1.3 + 1.4 + 1.5 + plugin.xml）
- [x] `📝 Document module dependency diagram feature`（Phase 3 全体 + .po 同期 + repository-structure 修正の同梱）

## Phase 6: マージ

- [x] worktree 内で `main` にマージ
- [x] 作業ブランチ削除
- [x] セッション終了（worktree 自動クリーンアップ発動）

## テスト免除の根拠記録

| クラス | 免除カテゴリ | 理由 |
|--------|-------------|------|
| `RescriptDependencyDiagramPanel` | Swing UI コンポーネント | `SimpleToolWindowPanel` 派生で UI 描画のみ |
| `RescriptDependencyDiagramToolWindowFactory` | Swing UI / IDE ライフサイクル | `ToolWindowFactory` 実装、ロジックは Panel に委譲 |
| `RescriptDependencyDiagramAction` | IDE ライフサイクル依存 | ToolWindow `activate()` のみで純粋ロジックなし |
