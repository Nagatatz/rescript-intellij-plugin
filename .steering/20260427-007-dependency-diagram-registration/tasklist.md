# Task List: Dependency Diagram Extension Point 登録

## Phase 0: 準備

- [ ] worktree 作成 (`EnterWorktree dependency-diagram-registration`)

## Phase 1: 実装

### 1.1 Mermaid エクスポータ
- [ ] `src/main/kotlin/com/rescript/plugin/diagram/RescriptMermaidExporter.kt` を実装
- [ ] `src/test/kotlin/com/rescript/plugin/diagram/RescriptMermaidExporterTest.kt` を実装（複数モデル → Mermaid 出力検証、特殊文字エスケープ検証）

### 1.2 DOT/Mermaid Export Action
- [ ] `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramExportAction.kt` を実装
- [ ] `src/test/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramExportActionTest.kt` を実装（DOT/Mermaid 戻り値、空モデル時の振る舞い）

### 1.3 ToolWindow パネル
- [ ] `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramPanel.kt` を実装
- [ ] テスト省略（理由: Swing UI コンポーネント）

### 1.4 ToolWindow Factory
- [ ] `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramToolWindowFactory.kt` を実装
- [ ] テスト省略（理由: Swing UI / IDE ライフサイクル依存）

### 1.5 Analyze メニュー Action
- [ ] `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramAction.kt` を実装
- [ ] テスト省略（理由: IDE ライフサイクル依存・実質 ToolWindow 起動のみ）

## Phase 2: plugin.xml 登録

- [ ] `<extensions>` に `<toolWindow id="ReScript Module Diagram" ...>` を追加
- [ ] `<actions>` に `RescriptDependencyDiagramAction` を `AnalyzeMenu` 配下で登録
- [ ] アイコン: `AllIcons.FileTypes.Diagram`（既存リソースを優先、必要なら専用 SVG を追加）

## Phase 3: ドキュメント整合性

- [ ] `sphinx-docs/user/features/advanced.md:927-952` を実装に合わせて全面更新
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po` を `make gettext && make update-po` で更新し、新規 msgstr を翻訳
- [ ] `docs/functional-design.md` Extension Point マップに新規エントリ追加
- [ ] `docs/repository-structure.md:78` の代表クラス列を更新
- [ ] `README.md:74` の説明を Mermaid/DOT 対応へ微修正
- [ ] `CLAUDE.md` レイヤー 3 該当箇所があれば更新（要確認）

## Phase 4: コミット前検証

- [ ] `./gradlew ktlintCheck` 成功
- [ ] `./gradlew clean buildPlugin` 成功
- [ ] `./gradlew test` 成功（新規テスト含む）
- [ ] `./gradlew koverHtmlReport` で新規コードのカバレッジ確認
- [ ] `cd sphinx-docs && uv run --quiet make build-ja` 成功
- [ ] KDoc が新規クラスすべてに付与されているか
- [ ] deprecated API 利用がないか
- [ ] tasklist の全項目が `[x]` になっているか（このマージ前の最終コミット直前）

## Phase 5: コミット

機能単位で分割（git-conventions.md 準拠）:

- [ ] `✨ Add Mermaid exporter for module dependency graph`（1.1 + テスト）
- [ ] `✨ Add export action for module dependency diagram`（1.2 + テスト）
- [ ] `✨ Add ReScript Module Diagram tool window`（1.3 + 1.4 + 1.5 + plugin.xml）
- [ ] `📝 Document module dependency diagram feature`（Phase 3 全体 + .po 同期 + repository-structure 修正の同梱）

※ 既に staged だった `docs/repository-structure.md:36`（`RescriptErrorReporter` 追記）はドキュメント整合性コミットに含める

## Phase 6: マージ

- [ ] worktree 内で `main` にマージ
- [ ] 作業ブランチ削除
- [ ] セッション終了（worktree 自動クリーンアップ発動）

## テスト免除の根拠記録

| クラス | 免除カテゴリ | 理由 |
|--------|-------------|------|
| `RescriptDependencyDiagramPanel` | Swing UI コンポーネント | `SimpleToolWindowPanel` 派生で UI 描画のみ |
| `RescriptDependencyDiagramToolWindowFactory` | Swing UI / IDE ライフサイクル | `ToolWindowFactory` 実装、ロジックは Panel に委譲 |
| `RescriptDependencyDiagramAction` | IDE ライフサイクル依存 | ToolWindow `activate()` のみで純粋ロジックなし |
