# Refactor UI Helpers — タスクリスト

## セクション A: 色 hex 変換の集約

- [x] `util/RescriptColorUtils.kt` を新規作成 (colorToHexString)
- [x] `util/RescriptColorUtilsTest.kt` を新規作成 (5 ケース)
- [x] `ppx/RescriptPpxViewPanel.kt:96` の inline String.format を `RescriptColorUtils.colorToHexString` 呼出に差し替え
- [x] `flow/MermaidSourceColorizer.kt:136` の同実装を差し替え
- [x] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.util.*" --tests "com.rescript.plugin.ppx.*" --tests "com.rescript.plugin.flow.*"` が緑
- [x] コミット: `♻️ Extract colorToHexString into RescriptColorUtils`

## セクション B: escapeHtml の統一

- [x] `ppx/RescriptPpxViewPanel.kt` の private `escapeHtml` を削除し `RescriptSecurityUtils.escapeHtml` に差し替え
- [x] `flow/MermaidSourceColorizer.kt` の private `escapeHtml` を削除し同様に差し替え
- [x] 既存テスト (`RescriptPpxViewPanelTest`, `MermaidSourceColorizerTest`) が緑のまま
- [x] コミット: `♻️ Replace local escapeHtml fallbacks with RescriptSecurityUtils.escapeHtml`

## セクション C: JEditorPane factory

- [x] `util/HtmlEditorPaneFactory.kt` を新規作成 (createReadOnlyHtmlPane)
- [x] `util/HtmlEditorPaneFactoryTest.kt` を新規作成 (6 ケース)
- [x] `ppx/RescriptPpxViewPanel.kt` を factory 呼出に差し替え
- [x] `flow/RescriptVariantFlowPanel.kt` を factory 呼出に差し替え
- [x] `diagram/RescriptDependencyDiagramPanel.kt` を factory 呼出に差し替え
- [x] `./gradlew ktlintCheck buildPlugin test` が緑
- [x] コミット: `♻️ Extract HtmlEditorPaneFactory and use across PPX / flow / diagram panels`

## セクション D: ドキュメント同期

- [ ] `docs/repository-structure.md` の `util/` 行に `RescriptColorUtils`, `HtmlEditorPaneFactory` を追加
- [ ] CLAUDE.md / README.md / sphinx-docs は更新不要 (実装詳細のみの変更、ユーザー視点で挙動変化なし)
- [ ] コミット: `📝 List RescriptColorUtils and HtmlEditorPaneFactory in repository-structure`

## セクション E: 仕上げとマージ

- [ ] `./gradlew ktlintCheck buildPlugin test koverHtmlReport koverVerify verifyPluginStructure` 全緑
- [ ] DoD Phase 3 自己検証 (KDoc 完備、deprecated API 不使用)
- [ ] 本ファイルの全チェックボックスを `[x]` に更新してコミット
- [ ] `AskUserQuestion` でマージ可否確認、承認後 main にマージ

## テスト省略の理由

- `RescriptPpxViewPanel` / `RescriptVariantFlowPanel` / `RescriptDependencyDiagramPanel` 本体は Swing UI コンポーネントで UI 免除。リファクタは factory への置換のみで、本体側に新規ロジックは無い
- `MermaidSourceColorizer` の既存テストが escapeHtml の挙動を assert しているので、`StringUtil.escapeXmlEntities` への差し替えはそのテストで検証される
