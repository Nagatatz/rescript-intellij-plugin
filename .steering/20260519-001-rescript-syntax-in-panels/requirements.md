# ReScript Syntax-Based Coloring in Panels — 要求内容

## 背景

機能発掘調査 `.steering/20260514-001-feature-discovery/` のバケット B (ReScript syntax-based 色付け 4 件) を本ステアリングで実装する。Steering A (`20260514-002-visual-color-brushup`) で意味別色付けは完了したが、Hoogle 検索結果や Type Info / PPX View / Notebook 入力など、ReScript ソース・型シグネチャ・アノテーションを **plain text** で表示しているパネルが残っている。これらに syntax-based 色付けを適用してエディタ本体と同じ視覚体験を提供する。

## スコープ

4 件の色付け + ドキュメント同期 = 5 コミット。

### 機能 1: Hoogle 検索結果のシグネチャ色付け

`navigation/RescriptTypeSignatureCellRenderer.kt`

- 現状: signature 全体を `GRAY_ITALIC_ATTRIBUTES` で 1 行 append
- 変更: 新規 `RescriptSignatureTokenColorizer` を `navigation/` に追加し、`RescriptLexer` でトークン分解 → `RescriptSyntaxHighlighter.getTokenHighlights()` で `TextAttributesKey` を取得 → `EditorColorsManager.getInstance().globalScheme.getAttributes(key)` で `TextAttributes` 解決 → `SimpleTextAttributes.fromTextAttributes()` で `SimpleTextAttributes` 化 → `ColoredListCellRenderer.append()` でトークンごとに描画
- `LIDENT` (highlighter 未登録) は fallback で `REGULAR_ATTRIBUTES`
- 空キー (whitespace, parens 等で `EMPTY_ARRAY` が返るケース) は `REGULAR_ATTRIBUTES`

### 機能 2: Type Info panel の `EditorTextField` 化

`typeinfo/RescriptTypeInfoPanel.kt`

- 現状: `JBLabel` で hover 由来の型シグネチャを plain text 表示
- 変更: `JBLabel` を `EditorTextField` (viewer モード) に置換し、`RescriptFileType` を渡してエディタ標準のシンタックスハイライトを適用
- `setOneLineMode(true)` で 1 行表示を維持
- REPL output 領域 (`RescriptReplPanel`) のパターンを参考にする (`EditorTextField` + Project + RescriptFileType)

### 機能 3: PPX View panel の `@annotation` 色付け

`ppx/RescriptPpxViewPanel.kt`

- 現状: `JTextArea` で `Line N: @annotation\n  → 説明文` 形式の plain text 表示
- 変更: `JTextArea` のまま、`DefaultHighlighter` の `HighlightPainter` で `@annotation` 部分のみ着色 (前景は `RescriptSyntaxHighlighter.ANNOTATION` の `TextAttributesKey` から解決)
- 残り (`Line N:` プレフィックスや `→` 矢印、英語説明文) は plain のまま — 内容がほぼ英語のため full ReScript highlighting は意図に反する
- 代替案: `EditorTextField` + `PlainTextFileType` への切替も検討したが、`Line 5:` や `→` が ReScript として解釈されると不自然になるため不採用

### 機能 4: Notebook cell 入力の `EditorTextField` 化

`notebook/RescriptNotebookCellPanel.kt`

- 現状: `JTextArea` で ReScript ソースを monospace のみ表示
- 変更: `JTextArea` を `EditorTextField` (編集可能) に置換、`RescriptFileType` でフルハイライト
- REPL input (`RescriptReplPanel.kt:75-90`) のパターンを完全に踏襲: `EditorFactory.getInstance().createDocument(...)` + `RescriptFileType` + `addSettingsProvider`
- 既存の `DocumentListener` ロジックを `EditorTextField.addDocumentListener` に差し替え
- `toCell()` / Run のための text アクセスは `editorTextField.text` で取得

### 機能 5: ドキュメント同期

- `CLAUDE.md` レイヤー 3 — `navigation/`、`typeinfo/`、`ppx/`、`notebook/` 段落にハイライト言及を追加
- `README.md` Features セクションの該当機能行
- `docs/repository-structure.md` の該当パッケージ行に新 helper (`RescriptSignatureTokenColorizer`) を追加
- `sphinx-docs/user/features/` の該当ページに ScRipt 構文ハイライトの説明
- `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` を同期

## 受け入れ条件

- Hoogle 検索結果のトークンが種別ごとに色分けされる (キーワード / 型構築子 / 演算子 / 識別子 / 数値 / 記号)
- Type Info パネルの型シグネチャがエディタと同じカラースキームで色付けされる
- PPX View パネルの `@annotation` トークンが目立つ色で描画される (例: メタデータ色)
- Notebook 入力セルが REPL 入力と同等のシンタックスハイライトを得る
- 既存テスト全件緑、新規テストでトークン化ロジックの単体検証を行う
- `./gradlew ktlintCheck buildPlugin test koverHtmlReport verifyPluginStructure` 全緑

## 制約

- ColorScheme 依存の実際の色値は assert しない (テストは TextAttributesKey 解決経路の構造的検証のみ)
- 既存パネルの API (公開メソッド・コンストラクタ引数) は変えない
- LSP 機能には触れない (型情報取得経路はそのまま)
