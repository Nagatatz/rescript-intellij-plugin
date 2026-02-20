# 設計: A 優先度機能一括実装

## 全体方針

- パーサー（`RescriptParser.kt`, `Rescript.flex`）の変更は行わない
- 既存の PSI ノード（`RescriptElementTypes`）、トークン（`RescriptTokenTypes`）、ユーティリティ（`RescriptPsiUtils`）を活用
- 各機能は独立したパッケージ/ファイルに配置し、`plugin.xml` に Extension Point として登録
- 既存コードパターン（`RescriptSmartEnterProcessor`, `RescriptPostfixTemplateProvider`, `RescriptSwitchFileAction` 等）に倣う

---

## 9. Extend/Shrink Word Selection

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptWordSelectionHandler.kt`

### 設計

```
RescriptStringSelectionHandler : ExtendWordSelectionHandler
  └── select() — 文字列リテラルの引用符内→引用符含む全体

RescriptBracketSelectionHandler : ExtendWordSelectionHandler
  └── select() — 括弧内の内容→括弧含む全体 ((), {}, [])

RescriptCommentSelectionHandler : ExtendWordSelectionHandler
  └── select() — コメント内容→コメントマーカー含む全体 (//, /* */)
```

### 実装方針

- **文字列選択:** `STRING_VALUE` トークン上にカーソルがある場合、最初の選択で引用符を除いた内容を返し、次の選択で引用符含む全体を返す
- **括弧選択:** カーソル位置のトークンから前後にスキャンし、バランスのとれた括弧ペア（`LPAREN`/`RPAREN`, `LBRACE`/`RBRACE`, `LBRACKET`/`RBRACKET`）を検出。内容→ペア全体の 2 段階
- **コメント選択:** `SINGLE_COMMENT` は `//` を除いた内容→行全体、`MULTI_COMMENT` は `/*`/`*/` を除いた内容→マーカー含む全体

### plugin.xml 登録

```xml
<extendWordSelectionHandler
    implementation="com.rescript.plugin.editor.RescriptStringSelectionHandler"/>
<extendWordSelectionHandler
    implementation="com.rescript.plugin.editor.RescriptBracketSelectionHandler"/>
<extendWordSelectionHandler
    implementation="com.rescript.plugin.editor.RescriptCommentSelectionHandler"/>
```

---

## 10. Enter Handler — ドキュメントコメント継続

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptEnterHandler.kt`

### 設計

```
RescriptEnterHandler : EnterHandlerDelegate
  └── preprocessEnter()
      ├── MULTI_COMMENT 内 → "* " を挿入
      ├── "/**" の直後 → " * \n */" に展開
      └── SINGLE_COMMENT の行末 → "// " を挿入
```

### 実装方針

- `EnterHandlerDelegateAdapter` を継承
- `preprocessEnter()` でカーソル位置のコンテキストを判定:
  1. カーソルが `MULTI_COMMENT` トークン内（`/** ... */`）の場合:
     - コメント開始直後（`/**|`）→ 改行 + ` * ` + 改行 + ` */` を挿入（3行展開）
     - それ以外 → 改行 + ` * ` を挿入（行継続）
  2. カーソル行が `//` で始まる場合:
     - 改行 + 同じインデント + `// ` を挿入
- 行のインデントは `lineText.takeWhile { it.isWhitespace() }` で取得
- `Result.Default` を返して通常の改行処理と連携

### plugin.xml 登録

```xml
<enterHandlerDelegate
    implementation="com.rescript.plugin.editor.RescriptEnterHandler"/>
```

---

## 11. Expression Type Info

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/lsp/RescriptExpressionTypeProvider.kt`

### 設計

```
RescriptExpressionTypeProvider : ExpressionTypeProvider<PsiElement>
  ├── getInformationHint() — LSP hover から型情報を抽出
  ├── getExpressionsAt() — カーソル位置の PSI 要素リスト
  └── getErrorHint() — "No type information available"
```

### 実装方針

- `ExpressionTypeProvider<PsiElement>` を実装
- `getExpressionsAt()`: カーソル位置の `PsiElement` を `listOf(element)` で返す
- `getInformationHint()`:
  1. IntelliJ Platform の LSP API（`LspServerManager`）経由で現在の LSP サーバーインスタンスを取得
  2. `textDocument/hover` リクエストをカーソル位置で送信
  3. レスポンスの `contents`（Markdown）から型情報部分（コードフェンス内のテキスト）を抽出
  4. HTML エスケープして返す
- LSP サーバーが利用不能な場合は `getErrorHint()` を返す

### plugin.xml 登録

```xml
<codeInsight.expressionTypeProvider
    language="ReScript"
    implementationClass="com.rescript.plugin.lsp.RescriptExpressionTypeProvider"/>
```

---

## 12. Highlight Usages — セマンティックハイライト

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/highlight/RescriptHighlightUsagesHandlerFactory.kt`

### 設計

```
RescriptHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactory
  └── createHighlightUsagesHandler()
      └── RescriptKeywordHighlightUsagesHandler
          ├── switch → 全 | パターンアーム
          ├── try → catch ブランチ
          ├── if → else if / else ブランチ
          └── | → switch + 全 |
```

### 実装方針

- `HighlightUsagesHandlerFactory` を実装
- カーソル位置のトークンが `SWITCH`, `TRY`, `IF`, `PIPE` の場合にハンドラを返す
- `RescriptKeywordHighlightUsagesHandler`:
  - `switch` の場合: 同レベルの `LBRACE`/`RBRACE` 内の全 `PIPE` トークンを収集（ブレースバランスで判定）
  - `try` の場合: 対応する `CATCH` キーワードを収集
  - `if` の場合: 対応する `ELSE` + `IF` のチェーンを収集
  - `|` の場合: 囲む `switch` キーワード + 同レベルの全 `|` を収集
- トークンスキャンは `RescriptSmartEnterProcessor` のブレースバランスパターンを参考に実装

### plugin.xml 登録

```xml
<highlightUsagesHandlerFactory
    implementation="com.rescript.plugin.highlight.RescriptHighlightUsagesHandlerFactory"/>
```

---

## 13. Join Lines — スマート行結合

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/editor/RescriptJoinLinesHandler.kt`

### 設計

```
RescriptJoinLinesHandler : JoinLinesHandlerDelegate
  └── tryJoinLines()
      ├── "let x =" + 次行値 → "let x = value"
      ├── "expr->" + 次行関数 → "expr->fn"
      └── 通常結合（余分な空白を正規化）
```

### 実装方針

- `JoinLinesHandlerDelegate` を実装
- `tryJoinLines(document, file, start, end)` で結合位置の行テキストを分析:
  1. **let 束縛の結合:** 上の行が `= ` で終わる（トレーリング空白除く）→ 下の行の先頭空白を除去して結合
  2. **パイプチェーンの結合:** 上の行が `->` で終わるか、下の行が `->` で始まる → 空白なしで結合
  3. **通常の行結合:** デフォルト処理に委譲（`CANNOT_JOIN` を返す）
- `RescriptLexer` で行末/行頭のトークンを確認してパターンマッチ

### plugin.xml 登録

```xml
<joinLinesHandler
    implementation="com.rescript.plugin.editor.RescriptJoinLinesHandler"/>
```

---

## 14. Completion Confidence

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/completion/RescriptCompletionConfidence.kt`

### 設計

```
RescriptCompletionConfidence : CompletionConfidence
  └── shouldSkipAutopopup()
      ├── SINGLE_COMMENT, MULTI_COMMENT → ThreeState.YES (スキップ)
      ├── STRING_VALUE → ThreeState.YES
      ├── JS_STRING_OPEN, JS_STRING_CLOSE → ThreeState.YES
      └── else → ThreeState.UNSURE (デフォルト動作)
```

### 実装方針

- `CompletionConfidence` を実装
- `shouldSkipAutopopup()` でカーソル位置のトークンタイプをチェック
- `RescriptPostfixTemplateProvider` の `NON_APPLICABLE_TOKENS` と同じトークンセットを再利用（共通定数として `RescriptTokenTypes` に `NON_CODE_TOKENS` を追加検討 → 既存パターンに倣い各クラスに直接定義）
- ReScript ファイル以外では `ThreeState.UNSURE` を返す

### plugin.xml 登録

```xml
<completion.confidence language="ReScript"
    implementationClass="com.rescript.plugin.completion.RescriptCompletionConfidence"/>
```

---

## 15. Live Template Context

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/completion/RescriptTemplateContextType.kt`

### 設計

```
RescriptTemplateContextType : TemplateContextType("ReScript")
  └── isInContext() — ReScript ファイル内かつコメント/文字列外
```

### 実装方針

- `TemplateContextType` を継承し、`isInContext(file, offset)` でコンテキストを判定:
  1. ファイルが ReScript ファイルでなければ `false`
  2. カーソル位置のトークンが `SINGLE_COMMENT`, `MULTI_COMMENT`, `STRING_VALUE`, `JS_STRING_OPEN`, `JS_STRING_CLOSE` なら `false`
  3. それ以外は `true`
- `ReScript.xml` の全テンプレートのコンテキストを `OTHER` から `RESCRIPT` に更新

### plugin.xml 登録

```xml
<liveTemplateContext
    implementation="com.rescript.plugin.completion.RescriptTemplateContextType"/>
```

### ReScript.xml 更新

全テンプレートの `<context>` を変更:
```xml
<!-- Before -->
<context>
    <option name="OTHER" value="true"/>
</context>

<!-- After -->
<context>
    <option name="RESCRIPT" value="true"/>
</context>
```

---

## 16. Live Template Macros

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/completion/RescriptLiveTemplateMacros.kt`

### 設計

```
RescriptModuleNameMacro : Macro
  ├── getName() → "rescriptModuleName"
  └── calculateResult() → ファイル名 → モジュール名 (MyFile.res → MyFile)

RescriptComponentNameMacro : Macro
  ├── getName() → "rescriptComponentName"
  └── calculateResult() → ファイル名 → コンポーネント名 (= モジュール名)
```

### 実装方針

- `Macro` を継承
- `calculateResult(expressions, context)`:
  - `context.psiElementAtStartOffset?.containingFile?.virtualFile?.nameWithoutExtension` でモジュール名を取得
  - `RescriptComponentNameMacro` はモジュール名と同じ（ReScript ではコンポーネント = モジュール）

### plugin.xml 登録

```xml
<liveTemplateMacro
    implementation="com.rescript.plugin.completion.RescriptModuleNameMacro"/>
<liveTemplateMacro
    implementation="com.rescript.plugin.completion.RescriptComponentNameMacro"/>
```

---

## 17. Problem Highlight Filter

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/analysis/RescriptProblemHighlightFilter.kt`

### 設計

```
RescriptProblemHighlightFilter : ProblemHighlightFilter
  └── shouldHighlight()
      ├── node_modules/ 内 → false
      ├── lib/bs/ 内 → false
      └── else → true
```

### 実装方針

- `ProblemHighlightFilter` を実装
- `shouldHighlight(file)` でファイルパスをチェック:
  - `file.path` に `/node_modules/` が含まれる → `false`
  - `file.path` に `/lib/bs/` または `/lib/ocaml/` が含まれる → `false`
  - ファイル拡張子が `.res` / `.resi` でない → `true`（フィルタ対象外）
  - それ以外 → `true`
- `shouldProcessInBatch()` はデフォルト（`true`）

### plugin.xml 登録

```xml
<problemHighlightFilter
    implementation="com.rescript.plugin.analysis.RescriptProblemHighlightFilter"/>
```

---

## 18. External Documentation

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/documentation/RescriptDocumentationProvider.kt`

### 設計

```
RescriptDocumentationProvider : AbstractDocumentationProvider
  └── getUrlFor()
      ├── Belt.* → https://rescript-lang.org/docs/manual/api/belt/*
      ├── Js.* → https://rescript-lang.org/docs/manual/api/js/*
      └── 標準モジュール → 対応する URL
```

### URL マッピング

```kotlin
val MODULE_URL_MAP = mapOf(
    "Belt" to "belt",
    "Belt.Array" to "belt/array",
    "Belt.List" to "belt/list",
    "Belt.Map" to "belt/map",
    "Belt.Set" to "belt/set",
    "Belt.HashMap" to "belt/hash-map",
    "Belt.HashSet" to "belt/hash-set",
    "Belt.MutableMap" to "belt/mutable-map",
    "Belt.MutableSet" to "belt/mutable-set",
    "Belt.SortArray" to "belt/sort-array",
    "Belt.Int" to "belt/int",
    "Belt.Float" to "belt/float",
    "Belt.Option" to "belt/option",
    "Belt.Result" to "belt/result",
    "Belt.Range" to "belt/range",
    "Js" to "js",
    "Js.Array2" to "js/array-2",
    "Js.String2" to "js/string-2",
    "Js.Promise" to "js/promise",
    "Js.Json" to "js/json",
    "Js.Math" to "js/math",
    "Js.Date" to "js/date",
    "Js.Re" to "js/re",
    "Js.Dict" to "js/dict",
    "Js.Null" to "js/null",
    "Js.Nullable" to "js/nullable",
    "Js.Exn" to "js/exn",
    "Js.Console" to "js/console",
    "Js.TypedArray2" to "js/typed-array-2",
)
```

URL パターン: `https://rescript-lang.org/docs/manual/latest/api/$path`

### 実装方針

- `AbstractDocumentationProvider` を継承
- `getUrlFor(element, originalElement)`:
  1. カーソル位置の PSI 要素から識別子テキストを取得（`UIDENT` トークン）
  2. ドット区切りのモジュールパス（例: `Belt.Array`）を構築
  3. `MODULE_URL_MAP` で URL パスを検索
  4. ヒットすれば URL リストを返す
- `generateDoc()` は実装しない（LSP hover で対応済み）

### plugin.xml 登録

```xml
<lang.documentationProvider language="ReScript"
    implementationClass="com.rescript.plugin.documentation.RescriptDocumentationProvider"/>
```

---

## 19. Run Anything Provider

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/run/RescriptRunAnythingProvider.kt`

### 設計

```
RescriptRunAnythingProvider : RunAnythingProviderBase<String>
  ├── getCommand() → "rescript"
  ├── findMatchingValue() — "rescript" プレフィックスマッチ
  ├── getValues() — 候補コマンド一覧
  └── execute() — RescriptCliDetector で CLI パスを解決して実行
```

### 補完候補

```
rescript build
rescript build -w
rescript clean
rescript format <current-file>
```

### 実装方針

- `RunAnythingProviderBase<String>` を継承
- `getValues(dataContext)`:
  - ReScript プロジェクト（`rescript.json` が存在）でのみ候補を返す
  - 既存の `RescriptCliDetector` でプロジェクトの CLI パスを検出
- `execute(dataContext, value)`:
  - コマンドを解析し、`GeneralCommandLine` で実行
  - `RunContentManager` で実行結果を Run ツールウィンドウに表示
- `getHelpCommand()` → `"rescript"`
- `getHelpGroupTitle()` → `"ReScript"`

### plugin.xml 登録

```xml
<runAnythingProvider
    implementation="com.rescript.plugin.run.RescriptRunAnythingProvider"/>
```

---

## 20. Goto Super — .res → .resi ジャンプ

### ファイル構成

- `src/main/kotlin/com/rescript/plugin/navigation/RescriptGotoSuperHandler.kt`

### 設計

```
RescriptGotoSuperHandler : CodeInsightActionHandler
  ├── isValidFor() — .res / .resi ファイルのみ有効
  └── invoke()
      ├── 対象ファイル（.res→.resi / .resi→.res）を取得
      ├── カーソル位置の宣言名を取得
      ├── 対象ファイル内で同名の宣言を検索
      └── 見つかればジャンプ、なければファイル先頭にジャンプ
```

### 実装方針

- `CodeInsightActionHandler` を実装
- `invoke(project, editor, file)`:
  1. 現在のファイルの拡張子から対象ファイルを決定（`RescriptSwitchFileAction` と同じロジック）
  2. カーソル位置から `PsiTreeUtil.findFirstParent()` で宣言ノード（`LET_DECLARATION`, `TYPE_DECLARATION`, `MODULE_DECLARATION`, `EXTERNAL_DECLARATION`）を取得
  3. `RescriptPsiUtils.extractName()` で宣言名を取得
  4. 対象ファイルの PSI を走査し、同じ宣言種類 + 同名の要素を検索
  5. 見つかれば `editor.navigateTo()`、なければ対象ファイルの先頭にジャンプ
- `GotoSuperAction` 用のプロバイダーとしても登録

### plugin.xml 登録

```xml
<codeInsight.gotoSuper language="ReScript"
    implementationClass="com.rescript.plugin.navigation.RescriptGotoSuperHandler"/>
```

---

## 21. Additional Snippets

### ファイル変更

- `src/main/resources/liveTemplates/ReScript.xml` — 新テンプレート追加
- `src/main/kotlin/com/rescript/plugin/completion/RescriptPostfixTemplateProvider.kt` — 新 Postfix テンプレート追加

### 追加 Live Templates

| 名前 | テンプレート | 説明 |
|------|-------------|------|
| `@module` | `@module("$MODULE$") external $NAME$: $TYPE$ = "$JS_NAME$"` | FFI module binding |
| `@val` | `@val external $NAME$: $TYPE$ = "$JS_NAME$"` | FFI val binding |
| `@send` | `@send external $NAME$: ($TYPE$, $ARGS$) => $RETURN$ = "$JS_NAME$"` | FFI send binding |
| `@get` | `@get external $NAME$: $TYPE$ => $RETURN$ = "$JS_NAME$"` | FFI get binding |
| `@set` | `@set external $NAME$: ($TYPE$, $VALUE$) => unit = "$JS_NAME$"` | FFI set binding |
| `comp` | `@react.component\nlet make = (~$PROPS$) => {\n  $END$\n}` | React component |

### 追加 Postfix Completions

| 名前 | 変換 | 説明 |
|------|------|------|
| `.promise` | `expr` → `expr->Promise.then(result => {\n  \n})` | Promise chain |
| `.await` | `expr` → `await expr` | Await expression |

### 実装方針

- **Live Templates:** `ReScript.xml` に新しい `<template>` エントリを追加。`$MODULE_NAME$` 変数には `rescriptModuleName()` マクロを使用
- **Postfix:** `RescriptPostfixTemplateProvider` の `templates` セットに `PromisePostfixTemplate` と `AwaitPostfixTemplate` を追加（既存の `SomePostfixTemplate` 等と同パターン）

**注意:** `.some`, `.ok`, `.error` は既存の `RescriptPostfixTemplateProvider` に実装済みのため追加不要。

---

## テスト方針

各機能に対して `src/test/kotlin/com/rescript/plugin/` 配下にテストクラスを作成:

| 機能 | テストクラス | テスト内容 |
|------|-------------|-----------|
| Extend/Shrink Selection | `editor/RescriptWordSelectionHandlerTest.kt` | 文字列・括弧・コメントの選択範囲計算 |
| Enter Handler | `editor/RescriptEnterHandlerTest.kt` | コメント継続の自動挿入テスト |
| Expression Type Info | テスト省略（LSP 結合が必須） | — |
| Highlight Usages | `highlight/RescriptHighlightUsagesHandlerFactoryTest.kt` | キーワード関連箇所のトークン収集 |
| Join Lines | `editor/RescriptJoinLinesHandlerTest.kt` | 行結合パターンのテスト |
| Completion Confidence | `completion/RescriptCompletionConfidenceTest.kt` | コンテキスト判定ロジック |
| Live Template Context | `completion/RescriptTemplateContextTypeTest.kt` | コンテキスト判定ロジック |
| Live Template Macros | `completion/RescriptLiveTemplateMacrosTest.kt` | モジュール名導出ロジック |
| Problem Highlight Filter | `analysis/RescriptProblemHighlightFilterTest.kt` | パス判定ロジック |
| External Documentation | `documentation/RescriptDocumentationProviderTest.kt` | URL マッピングテスト |
| Run Anything | `run/RescriptRunAnythingProviderTest.kt` | コマンドマッチング・候補テスト |
| Goto Super | `navigation/RescriptGotoSuperHandlerTest.kt` | 宣言名マッチングのロジックテスト |
| Additional Snippets | `completion/RescriptPostfixTemplateProviderTest.kt` | 新 Postfix テンプレートのテスト |
