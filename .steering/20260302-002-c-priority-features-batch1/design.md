# C 優先度機能バッチ1 — 設計

## 各機能の設計

### #53 Strip Trailing Spaces

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/editor/RescriptStripTrailingSpacesFilterFactory.kt`
- `src/test/kotlin/com/rescript/plugin/editor/RescriptStripTrailingSpacesFilterFactoryTest.kt`

**実装:** `StripTrailingSpacesFilterFactory` を継承。`createFilter()` で ReScript ファイルかを判定し、文字列リテラル内のみ除去をスキップする `StripTrailingSpacesFilter` を返す。実際には `ENFORCED_REMOVAL` を返して行末空白の積極的除去を有効化する。文字列リテラル内の空白保護はレクサーベースのオフセット判定で行う。

**登録:** `plugin.xml` に `<stripTrailingSpacesFilterFactory>`

---

### #55 Formatting for Injected

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/formatter/RescriptInjectedFormattingModelBuilder.kt`
- `src/test/kotlin/com/rescript/plugin/formatter/RescriptInjectedFormattingModelBuilderTest.kt`

**実装:** `FormattingModelBuilder` を実装。`createModel()` で `DelegatingFormattingModel` を返し、インジェクトされた言語フラグメント（`%raw()` 内の JS）をその言語のフォーマッタに委譲する。既存の `RescriptFormattingService` は外部 CLI フォーマッタなので、これとは独立。`InjectedLanguageManager` を使用してインジェクションホストとゲスト言語のマッピングを取得する。

**登録:** `plugin.xml` に `<lang.formatter language="ReScript">`

---

### #59 Grazie Text Extractor

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/grazie/RescriptGrazieTextExtractor.kt`
- `src/main/resources/META-INF/rescript-grazie.xml`
- `src/test/kotlin/com/rescript/plugin/grazie/RescriptGrazieTextExtractorTest.kt`

**実装:** `TextExtractor` を実装。`buildTextContent()` で以下のトークンからテキストを抽出:
- `SINGLE_COMMENT` → `//` プレフィックスを除外してテキスト部分を抽出
- `MULTI_COMMENT` → `/*` `*/` を除外して内部テキストを抽出
- `STRING_VALUE` → 文字列リテラルの内容を抽出

**登録:** `plugin.xml` に `<depends optional="true" config-file="rescript-grazie.xml">tanvd.grazi</depends>`、`rescript-grazie.xml` に `<grazie.textExtractor>`

**パターン:** `RescriptSpellcheckingStrategy` と同じトークンタイプ判定パターン。

---

### #60 Element Signature Provider

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptElementSignatureProvider.kt`
- `src/test/kotlin/com/rescript/plugin/navigation/RescriptElementSignatureProviderTest.kt`

**実装:** `ElementSignatureProvider` を実装。`getSignature()` で宣言要素に一意シグネチャを返す。フォーマット: `<type>#<name>#<offset>` (例: `LET_DECLARATION#myFunc#42`)。`restoreBySignature()` でシグネチャからファイル内の対応要素を復元。

**参照:** `RescriptQualifiedNameProvider` の宣言検出パターン + `RescriptPsiUtils.extractName()` を活用。

**登録:** `plugin.xml` に `<elementSignatureProvider>`

---

### #61 Index Pattern Builder

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/indexing/RescriptIndexPatternBuilder.kt`
- `src/test/kotlin/com/rescript/plugin/indexing/RescriptIndexPatternBuilderTest.kt`

**実装:** `IndexPatternBuilder` を実装。`getIndexingLexer()` で `RescriptLexer` を返す。`getCommentTokenSet()` で `RescriptTokenTypes.COMMENTS` を返す。`getCommentStartDelta()` / `getCommentEndDelta()` でコメントデリミタの長さを返す。

**参照:** 既存の `RescriptTodoIndexer` と同じレクサーを使用。

**登録:** `plugin.xml` に `<indexPatternBuilder>`

---

### #68 File Include Provider

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptFileIncludeProvider.kt`
- `src/test/kotlin/com/rescript/plugin/navigation/RescriptFileIncludeProviderTest.kt`

**実装:** `FileIncludeProvider` を継承。`registerFileTypesUsedForIndexing()` で `.res` / `.resi` を登録。`getIncludeInfos()` でファイルテキストから `open <ModulePath>` パターンを正規表現で抽出し、モジュール名を `FileIncludeInfo` に変換。モジュール名 → ファイルパスの変換（例: `Belt.Array` → `Belt/Array.res` or `Array.res`）。

**登録:** `plugin.xml` に `<include.provider>`

---

### #69 Editor Floating Toolbar

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/editor/RescriptFloatingToolbarProvider.kt`
- `src/test/kotlin/com/rescript/plugin/editor/RescriptFloatingToolbarProviderTest.kt`

**実装:** `FloatingToolbarProvider` を実装。`isApplicable()` で現在のファイルが ReScript ファイルかを判定。`getActionGroup()` で以下のアクションを含む `DefaultActionGroup` を返す:
1. **Format** — `ReformatCodeAction`
2. **Build** — `RescriptRunAction`（既存）
3. **Open Compiled JS** — `RescriptOpenCompiledJsAction`（既存）
4. **Create Interface** — `RescriptCreateInterfaceAction`（既存）

`autoHideable = true` で自動非表示。

**登録:** `plugin.xml` に `<editorFloatingToolbarProvider>`

---

## plugin.xml 登録まとめ

| 機能 | 登録先 | extension point |
|------|--------|----------------|
| #53 | `plugin.xml` | `<stripTrailingSpacesFilterFactory>` |
| #55 | `plugin.xml` | `<lang.formatter language="ReScript">` |
| #59 | `rescript-grazie.xml` | `<grazie.textExtractor>` |
| #60 | `plugin.xml` | `<elementSignatureProvider>` |
| #61 | `plugin.xml` | `<indexPatternBuilder>` |
| #68 | `plugin.xml` | `<include.provider>` |
| #69 | `plugin.xml` | `<editorFloatingToolbarProvider>` |

## ドキュメント更新

- `CLAUDE.md` — レイヤー 3 に 7 機能追加
- `README.md` — Features セクションに 7 機能追加
- `sphinx-docs/user/features/` — 該当ページに説明追加
- `docs/product-requirements.md` — 7 件を「実装済み」に移動、残り 16 件に更新
