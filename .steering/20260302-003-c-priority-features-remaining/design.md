# Design: C 優先度機能 残り 16 件

## 実装順序

依存関係と難易度に基づいて、以下の順序で実装する。

## 各機能の設計

---

### #57 Scratch File

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/scratch/RescriptScratchFileType.kt`
- `src/main/kotlin/com/rescript/plugin/scratch/RescriptScratchCreationHelper.kt`
- `src/test/kotlin/com/rescript/plugin/scratch/RescriptScratchCreationHelperTest.kt`

**実装:**
- `Language` を `RescriptLanguage.INSTANCE` に紐付けた Scratch ファイルサポート
- `ScratchFileCreationHelper` を継承し、`prepareText()` で初期テンプレートを提供
- `RootType` を登録して Scratch メニューに "ReScript" を追加
- 初期テンプレート: `// ReScript Scratch File\nlet result = "Hello"\nJs.log(result)\n`
- 実行は既存の `RescriptRunConfiguration` を拡張（スクラッチファイルパス対応）

**登録:** `plugin.xml` に `<scratch.creationHelper>`, `<scratch.rootType>`

---

### #58 REPL

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/repl/RescriptReplToolWindowFactory.kt`
- `src/main/kotlin/com/rescript/plugin/repl/RescriptReplPanel.kt`
- `src/main/kotlin/com/rescript/plugin/repl/RescriptReplExecutor.kt`
- `src/test/kotlin/com/rescript/plugin/repl/RescriptReplExecutorTest.kt`

**実装:**
- `ToolWindowFactory` + `DumbAware` で REPL ツールウィンドウを作成
- `RescriptReplPanel`: 入力エリア + 出力エリア + ツールバー（Clear, Run, Stop）
- `RescriptReplExecutor`: `node -e` で ReScript コードをコンパイル→実行するパイプライン
  - 入力 `.res` をテンポラリファイルに書き出し
  - `npx rescript` でコンパイル → 生成された `.js` を `node` で実行
  - stdout/stderr をキャプチャして出力エリアに表示
- companion object に `buildCommand()` と `parseOutput()` を配置（テスト可能）

**登録:** `plugin.xml` に `<toolWindow id="ReScript REPL">`

---

### #66 Suggested Refactoring

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/inspection/RescriptSuggestedRefactoringInspection.kt`
- `src/test/kotlin/com/rescript/plugin/inspection/RescriptSuggestedRefactoringInspectionTest.kt`

**実装:** `LocalInspectionTool` を継承。3 ルール:
1. **長すぎる関数**: let 束縛の本体が一定行数（20行）を超える場合に「Extract Function を検討」を提案
2. **同一パターンの繰り返し**: 同一ファイル内で同じ `->Array.map(...)->Array.filter(...)` 等のパイプチェーンが 2 回以上出現する場合に「共通関数への抽出を検討」を提案
3. **ネストが深い switch**: switch の中に switch がある場合に「パターンの平坦化を検討」を提案

各ルールは `INFORMATION` レベル（弱いヒント）として `holder.registerProblem()` で登録。Quick Fix は不要（提案のみ）。

**検出:** ファイルテキストを行単位でスキャンし、正規表現でパターンを検出。companion object にロジックを集約。

**登録:** `plugin.xml` に `<localInspection>` (INFORMATION)

---

### #104 JS→ReScript 変換

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/paste/RescriptPasteAsRescriptProcessor.kt`
- `src/test/kotlin/com/rescript/plugin/paste/RescriptPasteAsRescriptProcessorTest.kt`

**実装:** `CopyPastePostProcessor<TextBlockTransferableData>` を継承。
- `processTransferableData()` でペーストされたテキストが JavaScript かどうかを検出
  - JS 検出: `const `, `let `, `function `, `var `, `import `, `export ` で始まる行が含まれる
- 変換ルール (companion object `convertJsToRescript()`):
  - `const x = ` / `var x = ` → `let x = `
  - `function foo(a, b) {` → `let foo = (a, b) => {`
  - `===` → `==`, `!==` → `!=`
  - `null` → `None`, `undefined` → `None`
  - `console.log(` → `Js.log(`
  - `// comment` はそのまま
  - `{...}` オブジェクトリテラルはそのまま（ReScript レコードと構文が近い）
- ユーザーに変換の確認ダイアログを表示してから適用

**登録:** `plugin.xml` に `<copyPastePostProcessor>`

---

### #63 Inline Variable/Function

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/refactor/RescriptInlineHandler.kt`
- `src/test/kotlin/com/rescript/plugin/refactor/RescriptInlineHandlerTest.kt`

**実装:** `InlineActionHandler` を継承。
- `canInlineElement()`: カーソル位置が `let name = expr` の `name` 上にあることを確認
- `inlineElement()`:
  1. let 宣言の右辺の式を取得
  2. 同一ファイル内で `name` の使用箇所をテキスト検索
  3. 各使用箇所を右辺の式で置換（必要に応じて括弧で囲む）
  4. 元の let 宣言を削除
- companion object に `findUsages()`, `needsParentheses()`, `performInline()` を配置

**登録:** `plugin.xml` に `<inlineActionHandler>`

---

### #65 Introduce Constant

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/refactor/RescriptIntroduceConstantHandler.kt`
- `src/test/kotlin/com/rescript/plugin/refactor/RescriptIntroduceConstantHandlerTest.kt`

**実装:** `RefactoringActionHandler` を継承。
- 選択されたリテラル値（数値、文字列）を検出
- トップレベルに `let <name> = <literal>` 束縛を挿入
- 元のリテラルを `<name>` で置換
- 同一ファイル内の同一リテラルもオプションで置換
- companion object に `extractLiteral()`, `generateBinding()` を配置

**パターン:** 既存の `RescriptExtractVariableHandler` と同じアプローチ。

**登録:** `RescriptRefactoringSupportProvider` に追加

---

### #67 Dependency Diagram

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramProvider.kt`
- `src/main/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramModel.kt`
- `src/test/kotlin/com/rescript/plugin/diagram/RescriptDependencyDiagramModelTest.kt`

**実装:**
- `DiagramProvider` を継承してモジュール依存関係図を生成
- 既存の `RescriptDependencyAnalyzer` を再利用してモジュール間の依存関係を解析
- `DiagramNode` / `DiagramEdge` でグラフ構造を表現
- UML プラグインの `DiagramProvider` API を使用（`com.intellij.diagram`）
- `open` 文と `include` 文から依存関係を抽出
- companion object に `buildDependencyGraph()` を配置

**登録:** `META-INF/rescript-diagram.xml` に `<diagramProvider>` + `plugin.xml` に optional dependency

---

### #86 React コンポーネント抽出

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/refactor/RescriptExtractComponentHandler.kt`
- `src/test/kotlin/com/rescript/plugin/refactor/RescriptExtractComponentHandlerTest.kt`

**実装:** `RefactoringActionHandler` を継承。
- 選択された JSX コードを検出（`<` で始まる選択範囲）
- 新しいコンポーネントを生成:
  ```rescript
  module NewComponent = {
    @react.component
    let make = () => {
      <選択されたJSX>
    }
  }
  ```
- 元の選択範囲を `<NewComponent />` に置換
- 選択された JSX 内の変数参照を検出し、props として追加
- companion object に `extractProps()`, `generateComponent()`, `generateUsage()` を配置

**登録:** `RescriptRefactoringSupportProvider` に追加、`<refactoring.helper>` に登録

---

### #87 PPX 展開ビュー

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/ppx/RescriptPpxViewToolWindowFactory.kt`
- `src/main/kotlin/com/rescript/plugin/ppx/RescriptPpxViewPanel.kt`
- `src/test/kotlin/com/rescript/plugin/ppx/RescriptPpxViewPanelTest.kt`

**実装:**
- `ToolWindowFactory` + `DumbAware` で PPX 展開ビューツールウィンドウを作成
- `RescriptPpxViewPanel`: ファイル内の PPX アノテーション一覧を表示
  - `@react.component` → make 関数のシグネチャ + `React.createElement` 呼び出しパターン
  - `@deriving(json)` → `toJson`/`fromJson` 関数シグネチャ
  - `@genType` → `.gen.tsx` ファイルの生成内容の説明
- カーソル位置の変更をリッスンし、最も近い PPX アノテーションの情報を表示
- 静的マッピングで PPX → 展開説明を提供
- companion object に `getPpxExpansionInfo()`, `findPpxAnnotations()` を配置

**登録:** `plugin.xml` に `<toolWindow id="ReScript PPX">`

---

### #88 モジュールタイプ実装生成

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/generate/RescriptGenerateModuleImplAction.kt`
- `src/test/kotlin/com/rescript/plugin/generate/RescriptGenerateModuleImplActionTest.kt`

**実装:** `AnAction` を継承。
- カーソル位置のモジュールタイプ宣言を検出
  - `module type Foo = { ... }` のブロックを解析
- タイプ定義内の宣言を解析:
  - `let name: type` → `let name = (params) => todo("implement")`
  - `type t` → `type t = unit`
  - `module M: S` → `module M: S = { ... }`
- スケルトンモジュール実装を生成して挿入
- companion object に `parseModuleType()`, `generateSkeleton()` を配置

**登録:** `plugin.xml` の `RescriptGenerateGroup` に `<action>`

---

### #105 型ホール支援

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/quickfix/RescriptTypeHoleQuickFix.kt`
- `src/test/kotlin/com/rescript/plugin/quickfix/RescriptTypeHoleQuickFixTest.kt`

**実装:** `LocalInspectionTool` + `LocalQuickFix` で型ホールを検出・支援。
- `_` が型位置に出現した場合を検出（`let x: _ = ...` パターン）
- LSP hover で推論された型を取得可能な場合は Quick Fix として「推論された型を挿入」を提供
- 推論できない場合は一般的な型候補（`string`, `int`, `float`, `bool`, `unit`, `array<'a>`, `option<'a>`）をリストで提供
- companion object に `detectTypeHole()`, `suggestTypes()` を配置

**登録:** `plugin.xml` に `<localInspection>` + `<intentionAction>`

---

### #106 コメント内コード評価

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/editor/RescriptCommentEvalProvider.kt`
- `src/test/kotlin/com/rescript/plugin/editor/RescriptCommentEvalProviderTest.kt`

**実装:** `InlayHintsProvider` を使ってコメント内のコード例にインレイヒントを表示。
- `/** ... */` ドキュメントコメント内の `@example` ブロックまたはコードフェンスを検出
- コード例の構文が ReScript として有効かをレクサーで簡易検証
- 有効な場合: 緑のチェックマークのインレイヒント `✓ valid syntax`
- 無効な場合: 赤の警告インレイヒント `⚠ syntax error`
- companion object に `findCodeExamples()`, `validateSyntax()` を配置

**登録:** `plugin.xml` に `<codeInsight.inlayHintsProvider>`

---

### #107 Worksheet モード

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/worksheet/RescriptWorksheetFileType.kt`
- `src/main/kotlin/com/rescript/plugin/worksheet/RescriptWorksheetRunner.kt`
- `src/test/kotlin/com/rescript/plugin/worksheet/RescriptWorksheetRunnerTest.kt`

**実装:**
- `LanguageFileType` で `.resw` 拡張子のワークシートファイルタイプを定義
- `RescriptWorksheetRunner`: ファイル内の各トップレベル式を個別にコンパイル・実行
  - テンポラリ `.res` ファイルに各式をラップして書き出し
  - `npx rescript` でコンパイル → `node` で実行
  - 結果をコメントとして各式の右側に表示
- ガターアイコン（Run）で実行をトリガー
- companion object に `extractExpressions()`, `buildEvalScript()`, `formatResult()` を配置

**登録:** `plugin.xml` に `<fileType>`, `<lang.syntaxHighlighter>` (RescriptSyntaxHighlighter を再利用)

---

### #62 Extract Function

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/refactor/RescriptExtractFunctionHandler.kt`
- `src/test/kotlin/com/rescript/plugin/refactor/RescriptExtractFunctionHandlerTest.kt`

**実装:** `RefactoringActionHandler` を継承。
- 選択されたコードブロックの自由変数（外部参照）を検出
- 自由変数をパラメータとした新しい関数を生成:
  ```rescript
  let extractedFunction = (param1, param2) => {
    <選択されたコード>
  }
  ```
- 元の選択範囲を `extractedFunction(arg1, arg2)` で置換
- カーソルを関数名に配置してリネーム可能に
- companion object に `findFreeVariables()`, `generateFunction()`, `generateCallSite()` を配置

**パターン:** 既存の `RescriptExtractVariableHandler` を拡張したアプローチ。

**登録:** `RescriptRefactoringSupportProvider` に追加

---

### #64 Change Signature

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/refactor/RescriptChangeSignatureHandler.kt`
- `src/main/kotlin/com/rescript/plugin/refactor/RescriptChangeSignatureDialog.kt`
- `src/test/kotlin/com/rescript/plugin/refactor/RescriptChangeSignatureHandlerTest.kt`

**実装:**
- `ChangeSignatureHandler` を継承
- カーソル位置の関数宣言を検出し、パラメータ一覧を解析
- ダイアログで以下の変更を許可:
  - パラメータの追加・削除・並び替え
  - パラメータ名の変更
  - デフォルト値の指定
- LSP rename を活用して呼び出し側を更新
- companion object に `parseParameters()`, `applySignatureChange()` を配置

**登録:** `plugin.xml` に `<refactoring.changeSignatureHandler>`

---

### #108 型シグネチャ検索

**ファイル:**
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptTypeSignatureSearchContributor.kt`
- `src/test/kotlin/com/rescript/plugin/navigation/RescriptTypeSignatureSearchContributorTest.kt`

**実装:** `WeightedSearchEverywhereContributor` を継承。
- Search Everywhere に "Types" タブを追加
- スタブインデックスから型宣言を収集
- 型シグネチャのパターンマッチング:
  - `string -> int` → パラメータ型が `string`、戻り値が `int` の関数を検索
  - `option<'a> -> 'a` → Option.getExn 等にマッチ
- トークン化した型シグネチャの部分一致で検索
- companion object に `tokenizeSignature()`, `matchSignature()`, `rankMatch()` を配置

**登録:** `plugin.xml` に `<searchEverywhereContributorFactory>`

---

## plugin.xml 登録まとめ

| 機能 | 登録先 | extension point |
|------|--------|----------------|
| #57 | `plugin.xml` | `<scratch.creationHelper>`, `<scratch.rootType>` |
| #58 | `plugin.xml` | `<toolWindow>` |
| #66 | `plugin.xml` | `<localInspection>` |
| #104 | `plugin.xml` | `<copyPastePostProcessor>` |
| #63 | `plugin.xml` | `<inlineActionHandler>` |
| #65 | `plugin.xml` | 既存 `RescriptRefactoringSupportProvider` に追加 |
| #67 | `rescript-diagram.xml` | `<diagramProvider>` (optional: UML plugin) |
| #86 | `plugin.xml` | 既存 `RescriptRefactoringSupportProvider` に追加 |
| #87 | `plugin.xml` | `<toolWindow>` |
| #88 | `plugin.xml` | `RescriptGenerateGroup` に `<action>` |
| #105 | `plugin.xml` | `<localInspection>`, `<intentionAction>` |
| #106 | `plugin.xml` | `<codeInsight.inlayHintsProvider>` |
| #107 | `plugin.xml` | `<fileType>`, `<lang.syntaxHighlighter>` |
| #62 | `plugin.xml` | 既存 `RescriptRefactoringSupportProvider` に追加 |
| #64 | `plugin.xml` | `<refactoring.changeSignatureHandler>` |
| #108 | `plugin.xml` | `<searchEverywhereContributorFactory>` |

## テスト方針

各機能の companion object メソッドをユニットテストする。以下は UI/IDE 依存のためテスト免除:

| 機能 | 免除理由 |
|------|---------|
| #57 Scratch File | `ScratchFileCreationHelper` は IDE ライフサイクル依存 |
| #58 REPL Panel | Swing UI コンポーネント |
| #87 PPX View Panel | Swing UI コンポーネント |
| #107 Worksheet FileType | `LanguageFileType` は IDE 依存（Runner はテスト対象） |

上記以外はすべて companion object のロジックをユニットテストする。
