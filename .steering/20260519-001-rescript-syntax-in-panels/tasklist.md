# ReScript Syntax-Based Coloring — タスクリスト

各セクションは「実装 + テスト + (該当する) ドキュメント = マージ可能 1 単位」の粒度。

## セクション A: Hoogle 検索結果のシグネチャ色付け

- [x] `navigation/RescriptSignatureTokenColorizer.kt` を新規作成 (tokenize + 属性解決)
- [x] `RescriptTypeSignatureCellRenderer` を tokenize 呼出に変更
- [x] `navigation/RescriptSignatureTokenColorizerTest.kt` を新規作成 (5 ケース)
- [x] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.navigation.*"` が緑
- [x] コミット: `✨ Tokenise Hoogle signature cells via RescriptSyntaxHighlighter`

## セクション B: Type Info panel の `EditorTextField` 化

- [x] `RescriptTypeInfoPanel.kt` の `JBLabel` を `EditorTextField` (viewer) に置換
- [x] `RescriptFileType` + Project 接続、設定 (`isLineNumbersShown` 等) 調整
- [x] `showMessage` を `editorField.text = text` に変更
- [x] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.typeinfo.*"` が緑
- [x] コミット: `✨ Apply ReScript syntax highlighting to Type Info panel`

## セクション C: PPX View panel の `@annotation` 色付け

- [ ] `RescriptPpxViewPanel.kt` の `JTextArea` を `JEditorPane` (HTML) に置換
- [ ] `renderHtml(annotations, color): String` を internal 抽出
- [ ] `annotationColorHex()` で `RescriptSyntaxHighlighter.ANNOTATION` の前景色を取得
- [ ] `RescriptPpxViewPanelTest` (既存があれば) に `renderHtml` の構造テスト追加
- [ ] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.ppx.*"` が緑
- [ ] コミット: `✨ Highlight @annotation tokens in PPX View panel`

## セクション D: Notebook cell 入力の `EditorTextField` 化

- [ ] `RescriptNotebookCellPanel` のコンストラクタに `project: Project` を追加
- [ ] `RescriptNotebookPanel` 側の生成箇所を更新
- [ ] `JTextArea` (codeArea) を `EditorTextField` に置換 (REPL input パターン踏襲)
- [ ] DocumentListener を `EditorTextField.addDocumentListener` に差し替え
- [ ] `./gradlew ktlintCheck buildPlugin test --tests "com.rescript.plugin.notebook.*"` が緑
- [ ] コミット: `✨ Apply ReScript syntax highlighting to Notebook cell input`

## セクション E: ドキュメント同期

- [ ] `CLAUDE.md` レイヤー 3 — `navigation/`、`typeinfo/`、`ppx/`、`notebook/` 段落にハイライト言及追加
- [ ] `README.md` Features の該当機能行に追記
- [ ] `docs/repository-structure.md` に `RescriptSignatureTokenColorizer` を追加
- [ ] `sphinx-docs/user/features/` の該当ページに syntax highlighting の説明
- [ ] `cd sphinx-docs && make gettext && make update-po && make build-ja` 実行
- [ ] 新規/変更 `msgid` の日本語 `msgstr` を埋める
- [ ] コミット: `📝 Document syntax highlighting in Hoogle / Type Info / PPX / Notebook panels`

## セクション F: 仕上げとマージ

- [ ] `./gradlew ktlintCheck buildPlugin test koverHtmlReport verifyPluginStructure` が緑
- [ ] DoD Phase 3 自己検証 (KDoc、deprecated API、セキュリティ)
- [ ] 本ファイルの全チェックボックスを `[x]` に更新してコミット
- [ ] `AskUserQuestion` でマージ可否確認
- [ ] 承認後: `git merge worktree-20260519-001-rescript-syntax-in-panels` (main 側で)
- [ ] Steering C 着手準備

## テスト省略の理由

- `RescriptTypeInfoPanel` / `RescriptPpxViewPanel` / `RescriptNotebookCellPanel` 自体は Swing UI コンポーネントで UI 免除。トークン化ロジックは別ヘルパーに抽出してユニットテスト可能にする
