# 要求定義: A 優先度機能一括実装

## 概要

他の JetBrains 言語プラグインとの機能ギャップ調査（`.steering/20260221-001-feature-research/research.md`）で特定された **A 優先度（高優先）** の 13 機能を一括実装する。いずれもパーサー変更不要で、高インパクトまたは中程度の労力で実装可能な機能。

## 実装対象

### 9. Extend/Shrink Word Selection（編集操作）
- **Extension Point:** `com.intellij.extendWordSelectionHandler`
- **概要:** Ctrl+W / Ctrl+Shift+W で言語構造に沿った選択拡大/縮小
- **受け入れ条件:**
  - 文字列リテラル内容 → 文字列全体（引用符含む）の段階的選択
  - 括弧内の内容 → 括弧を含む全体の段階的選択（`()`, `{}`, `[]`）
  - パイプチェーン（`->`）の引数 → パイプ式全体
  - JSX 属性値 → JSX 属性 → JSX 要素全体
  - let 束縛の右辺 → let 宣言全体
  - コメント内容 → コメント全体（`//` と `/* */` の両方）

### 10. Enter Handler — ドキュメントコメント継続（編集操作）
- **Extension Point:** `com.intellij.enterHandlerDelegate`
- **概要:** Enter キー押下時にドキュメントコメントの継続や文脈に応じた自動挿入
- **受け入れ条件:**
  - `/** */` ドキュメントコメント内で Enter → `* ` を自動挿入して継続
  - `/** ` の直後で Enter → `* ` を挿入し、次の行に ` */` を追加（3行コメントへ展開）
  - 既存の `* ` 行の後で Enter → `* ` を自動挿入
  - 通常コメント (`//`) の後で Enter → `// ` を自動挿入

### 11. Expression Type Info（コード分析）
- **Extension Point:** `com.intellij.codeInsight.typeInfo` / `ExpressionTypeProvider`
- **概要:** Ctrl+Shift+P でカーソル位置の式の型をポップアップ表示
- **受け入れ条件:**
  - カーソル位置の式の型が LSP hover レスポンスから取得・表示される
  - 型情報がない場合は「No type information available」と表示
  - ReScript の型構文でフォーマットされた表示（`string`, `option<int>`, `array<User.t>` 等）

### 12. Highlight Usages — セマンティックハイライト（コード分析）
- **Extension Point:** `com.intellij.highlightUsagesHandlerFactory`
- **概要:** カーソル位置のキーワードに応じた関連箇所のハイライト
- **受け入れ条件:**
  - `switch` キーワード → 全パターンアーム（`|`）をハイライト
  - `try` キーワード → `catch` ブランチをハイライト
  - `if` キーワード → `else if` / `else` ブランチをハイライト
  - `|` パターンアーム → 対応する `switch` と全 `|` をハイライト

### 13. Join Lines — スマート行結合（編集操作）
- **Extension Point:** `com.intellij.joinLinesHandler`
- **概要:** Ctrl+Shift+J で言語構造を考慮した行結合
- **受け入れ条件:**
  - `let x =` + 次行の値 → `let x = value` に結合
  - パイプチェーンの行 → `expr->fn1->fn2` に結合
  - `if cond {` + `body` + `}` → 可能なら 1 行に結合
  - 複数行文字列 → 連結

### 14. Completion Confidence（コード補完）
- **Extension Point:** `com.intellij.completion.confidence`
- **概要:** 自動補完ポップアップの表示制御
- **受け入れ条件:**
  - コメント内（`//`, `/* */`）で自動補完が表示されない
  - 文字列リテラル内で自動補完が表示されない
  - `%raw()` ブロック内で自動補完が表示されない（JS インジェクションが担当）
  - 通常のコードコンテキストでは正常に自動補完が動作する

### 15. Live Template Context（コード補完）
- **Extension Point:** `com.intellij.liveTemplateContext`
- **概要:** Live Template の有効コンテキストを定義
- **受け入れ条件:**
  - `RescriptTemplateContextType` が定義され、ReScript ファイル内でのみ Live Templates が展開される
  - コメント内では Live Templates が無効
  - 文字列内では Live Templates が無効
  - 既存の 15 テンプレートが `OTHER` から新しいコンテキストに移行

### 16. Live Template Macros（コード補完）
- **Extension Point:** `com.intellij.liveTemplateMacro`
- **概要:** Live Template 変数で使えるカスタム関数
- **受け入れ条件:**
  - `rescriptModuleName()` — 現在のファイル名からモジュール名を導出（`MyModule.res` → `MyModule`）
  - `rescriptComponentName()` — React コンポーネント名（= モジュール名）を導出
  - Live Template 内の `$MODULE_NAME$` 等の変数でこれらのマクロが使用可能

### 17. Problem Highlight Filter（コード分析）
- **Extension Point:** `com.intellij.problemHighlightFilter`
- **概要:** 特定ディレクトリ内のエラーハイライトを抑制
- **受け入れ条件:**
  - `node_modules/` 内の `.res` / `.resi` ファイルでエラーハイライトが抑制される
  - `lib/` (コンパイル出力) 内のファイルでエラーハイライトが抑制される
  - 通常のプロジェクトソースファイルでは正常にハイライトが動作する

### 18. External Documentation（ドキュメント）
- **Extension Point:** `com.intellij.lang.documentationProvider` (`getUrlFor()`)
- **概要:** Shift+F1 でブラウザの外部ドキュメントを開く
- **受け入れ条件:**
  - 標準ライブラリモジュール（`Belt.Array`, `Js.String2` 等）→ 対応する rescript-lang.org ドキュメントページ
  - `Belt.*` → `https://rescript-lang.org/docs/manual/api/belt/*`
  - `Js.*` → `https://rescript-lang.org/docs/manual/api/js/*`
  - 標準モジュール外ではフォールバックなし（URL を返さない）

### 19. Run Anything Provider（テスト・実行）
- **Extension Point:** `com.intellij.runAnythingProvider`
- **概要:** Ctrl+Ctrl の Run Anything ダイアログに ReScript コマンドを追加
- **受け入れ条件:**
  - `rescript build` — ReScript プロジェクトをビルド
  - `rescript build -w` — ウォッチモードでビルド
  - `rescript clean` — ビルドキャッシュをクリーン
  - `rescript format <file>` — ファイルをフォーマット
  - `rescript` プレフィックスで補完候補が表示される
  - ReScript プロジェクト（`rescript.json` が存在）でのみ有効

### 20. Goto Super — .res → .resi ジャンプ（ナビゲーション）
- **Extension Point:** `com.intellij.codeInsight.gotoSuper`
- **概要:** Ctrl+U で `.res` の関数実装から `.resi` の対応するインターフェース宣言にジャンプ
- **受け入れ条件:**
  - `.res` ファイルの `let` 宣言にカーソル → `.resi` の同名 `let` 宣言にジャンプ
  - `.res` ファイルの `type` 宣言にカーソル → `.resi` の同名 `type` 宣言にジャンプ
  - `.res` ファイルの `module` 宣言にカーソル → `.resi` の同名 `module` 宣言にジャンプ
  - `.resi` が存在しない場合は何も起こらない
  - `.resi` に対応する宣言が見つからない場合はファイル先頭にジャンプ

### 21. Additional Snippets — VSCode パリティ（コード補完）
- **Extension Point:** Live Templates XML / Postfix Completion
- **概要:** VSCode ReScript 拡張にあるスニペットを追加
- **受け入れ条件:**
  - **Postfix Completion 追加:**
    - `.promise` — `expr` → `expr->Promise.then(result => { ... })`
    - `.await` — `expr` → `await expr`
    - `.some` — `expr` → `Some(expr)`
    - `.ok` — `expr` → `Ok(expr)`
    - `.error` — `expr` → `Error(expr)`
  - **Live Template 追加:**
    - `@module` — `@module("$MODULE$") external $NAME$: $TYPE$ = "$JS_NAME$"`
    - `@val` — `@val external $NAME$: $TYPE$ = "$JS_NAME$"`
    - `@send` — `@send external $NAME$: ($TYPE$, $ARGS$) => $RETURN$ = "$JS_NAME$"`
    - `@get` — `@get external $NAME$: $TYPE$ => $RETURN$ = "$JS_NAME$"`
    - `@set` — `@set external $NAME$: ($TYPE$, $VALUE$) => unit = "$JS_NAME$"`
    - `comp` — React コンポーネントテンプレート (`@react.component let make = (~$PROPS$) => { ... }`)

## 制約

- パーサー（`RescriptParser.kt`, `Rescript.flex`）の変更は行わない
- 既存の PSI 構造とトークンタイプを活用する
- 各機能は `plugin.xml`（またはオプション依存の `rescript-*.xml`）に Extension Point として登録する
- LSP 依存の機能（#11 Expression Type Info）は、LSP サーバーが利用不能な場合にグレースフルに無効化する
