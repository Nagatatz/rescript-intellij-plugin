# Refactor UI Helpers — 要求内容

## 背景

直近 3 ステアリング (visual color brushup / ReScript syntax in panels / Mermaid highlighting) で同種のヘルパーパターンが複数箇所に出現した。リファクタ監査でトップ 3 件が高優先と判定されたため、本ステアリングで共通基盤に集約する。

## スコープ

3 件のリファクタ + ドキュメント = 4 コミット。

### 機能 1: `util/RescriptColorUtils.kt` 新規 — 色 hex 変換の集約

- `String.format("#%02X%02X%02X", color.red, color.green, color.blue)` パターンが 2 箇所で重複:
  - `ppx/RescriptPpxViewPanel.kt:96` (annotationColorHex 内)
  - `flow/MermaidSourceColorizer.kt:136` (hexFor 内)
- 新規 `util/RescriptColorUtils.kt` に `internal fun colorToHexString(color: Color): String` を抽出
- 両呼出箇所を `RescriptColorUtils.colorToHexString(color)` に差し替え

### 機能 2: local `escapeHtml` を `RescriptSecurityUtils.escapeHtml` に統一

- local 実装が 2 箇所:
  - `ppx/RescriptPpxViewPanel.kt:146-151` (private fun escapeHtml)
  - `flow/MermaidSourceColorizer.kt:140-145` (private fun escapeHtml)
- 両方とも素朴な `replace("&", "&amp;")...` チェーン
- 既存 `util/RescriptSecurityUtils.escapeHtml` (`StringUtil.escapeXmlEntities` ラッパー) に統一
- IntelliJ Platform の `StringUtil` は包括的 XML エンティティエスケープを行うので、`"` を `&quot;` にすることも保証される

### 機能 3: `util/HtmlEditorPaneFactory.kt` 新規 — JEditorPane (HTML) 初期化の集約

- 同一の HTML ペイン初期化が 3 箇所で重複:
  - `ppx/RescriptPpxViewPanel.kt:38-43`
  - `flow/RescriptVariantFlowPanel.kt:50-66` (textArea = JEditorPane)
  - `diagram/RescriptDependencyDiagramPanel.kt:48-56` (textArea = JEditorPane)
- 共通設定: `contentType = "text/html"`, `isEditable = false`, `putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)`, `font = Font(Font.MONOSPACED, PLAIN, size)`
- 新規 `util/HtmlEditorPaneFactory.kt` に `fun createReadOnlyHtmlPane(fontSize: Int = 13, borderInset: Int? = null): JEditorPane` を抽出
- 各呼出箇所を factory 呼出に差し替え

### 機能 4: ドキュメント同期

- `docs/repository-structure.md` の `util/` 行に `RescriptColorUtils`, `HtmlEditorPaneFactory` を追加
- 該当機能の段落の本文修正は不要 (実装詳細の変更のみ、ユーザー視点で変化なし)
- `CLAUDE.md` / `README.md` / `sphinx-docs` は更新不要

## 受け入れ条件

- 各機能ごとにコミットを分割し、独立にビルド・テスト緑
- `RescriptColorUtils.colorToHexString` と `HtmlEditorPaneFactory.createReadOnlyHtmlPane` にユニットテストを追加
- `MermaidSourceColorizer` / `RescriptPpxViewPanel` の既存テストは緑のまま
- `./gradlew ktlintCheck buildPlugin test koverHtmlReport koverVerify verifyPluginStructure` 全緑
- 既存 panel の UI 挙動は変わらない (factory への置換のみ)

## 制約

- `util/RescriptColorUtils` と `util/HtmlEditorPaneFactory` は `internal` で公開し、テスト可能性を確保
- factory のデフォルト引数は既存パネルの設定を踏襲 (フォントサイズ 13、border は呼出側で設定)
- `Color` を扱う関数は `java.awt.Color` を引数に取り、`JBColor` のラップは呼出側責務
