# Mermaid Syntax Highlighting — タスクリスト

## セクション A: 共通 colorizer + Variant Flow Source モード

- [x] `flow/MermaidSourceColorizer.kt` を新規作成 (renderLine + render + hexFor + escapeHtml)
- [x] `flow/MermaidSourceColorizerTest.kt` を新規作成 (renderLine の 7 ケース)
- [x] `RescriptVariantFlowPanel.kt` の `JTextArea` を `JEditorPane` に置換、`MermaidSourceColorizer.render()` 経由
- [x] Copy Mermaid action は `RescriptVariantFlowMermaidExporter.toMermaid()` を直接呼ぶため raw text のまま (既存ロジック維持)
- [x] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.flow.*"` が緑
- [x] コミット: `✨ Add Mermaid colorizer and apply to Variant Flow source mode`

## セクション B: Module Dependency Source モード

- [ ] `RescriptDependencyDiagramPanel.kt` の `JTextArea` を `JEditorPane` に置換
- [ ] Copy Mermaid action が生ソースを保持していることを確認
- [ ] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.diagram.*"` が緑
- [ ] コミット: `✨ Apply Mermaid colorizer to Module Dependency source mode`

## セクション C: ドキュメント同期

- [ ] `CLAUDE.md` `flow/` `diagram/` 段落に Mermaid colorizer 言及追加
- [ ] `README.md` Features の該当機能行
- [ ] `docs/repository-structure.md` の `flow/` 行に `MermaidSourceColorizer` を追加
- [ ] `sphinx-docs/user/features/advanced.md` の Variant Flow / Module Dependency セクションに Source mode 色付けの説明
- [ ] `cd sphinx-docs && make gettext && make update-po && make build-ja` 実行
- [ ] 新規/変更 `msgid` の日本語 `msgstr` を埋める
- [ ] コミット: `📝 Document Mermaid source mode colourisation`

## セクション D: 仕上げとマージ

- [ ] `./gradlew ktlintCheck buildPlugin test koverHtmlReport koverVerify verifyPluginStructure` が緑
- [ ] DoD Phase 3 自己検証
- [ ] 本ファイルの全チェックボックスを `[x]` に更新してコミット
- [ ] マージ実行

## テスト省略の理由

- `RescriptVariantFlowPanel` / `RescriptDependencyDiagramPanel` 自体は Swing UI コンポーネントで UI 免除
- `MermaidSourceColorizer.render` は `EditorColorsManager` 依存だが、`renderLine(line, hex, hex, hex, hex)` を internal で抽出すれば ColorScheme 非依存のテストが可能
