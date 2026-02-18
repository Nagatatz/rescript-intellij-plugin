# Design: P3 Batch Tier 2

## ブランチ戦略

```
main
 └── feature/p3-batch-tier2          ← バッチブランチ
      ├── feature/statement-mover     ← worktree
      ├── feature/qualified-name      ← worktree
      └── feature/smart-enter         ← worktree
```

## Feature 1: Statement Up/Down Mover

### API
- `com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover`
- Extension point: `<statementUpDownMover>`

### 実装ファイル
- `src/main/kotlin/com/rescript/plugin/editor/RescriptStatementUpDownMover.kt`
- `src/test/kotlin/com/rescript/plugin/editor/RescriptStatementUpDownMoverTest.kt`

### 設計
- `checkAvailable()` で caret 位置の PSI 要素からトップレベル宣言を特定
- `PsiTreeUtil.findFirstParent` で NAVIGABLE_TYPES + OPEN_STATEMENT + INCLUDE_STATEMENT + ANNOTATION を含む宣言ノードを探す
- 先行する ANNOTATION ノードも宣言に含める（`@genType let foo = ...` をまとめて移動）
- 隣接する宣言を探し、`info.toMove` / `info.toMove2` に行範囲を設定

## Feature 2: Qualified Name Copy

### API
- `com.intellij.ide.actions.QualifiedNameProvider`
- Extension point: `<qualifiedNameProvider>`

### 実装ファイル
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptQualifiedNameProvider.kt`
- `src/test/kotlin/com/rescript/plugin/navigation/RescriptQualifiedNameProviderTest.kt`

### 設計
- `getQualifiedName(element)`:
  - ファイル名（拡張子なし）をルートモジュール名に変換（先頭大文字化）
  - 親 MODULE_DECLARATION を辿り、`RescriptPsiUtils.extractName()` で各レベルの名前を取得
  - パス結合: `FileName.Module.SubModule.functionName`
- `qualifiedNameToElement()`: null（LSP が担当）

## Feature 3: Smart Enter

### API
- `com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor`
- Extension point: `<lang.smartEnterProcessor>`

### 実装ファイル
- `src/main/kotlin/com/rescript/plugin/editor/RescriptSmartEnterProcessor.kt`
- `src/test/kotlin/com/rescript/plugin/editor/RescriptSmartEnterProcessorTest.kt`

### 設計
- `process()`:
  1. caret 位置の行テキストをレキサーで解析
  2. 未閉じ括弧の補完: `{` → `}`, `(` → `)`, `[` → `]`
  3. `switch` 文のブレース補完
  4. パイプ行の `=>` 補完
  5. 上記以外: 通常改行 + インデント
- `RescriptLexer` を使ったトークン解析

## plugin.xml への追加

```xml
<statementUpDownMover
    implementation="com.rescript.plugin.editor.RescriptStatementUpDownMover"/>
<qualifiedNameProvider
    implementation="com.rescript.plugin.navigation.RescriptQualifiedNameProvider"/>
<lang.smartEnterProcessor language="ReScript"
    implementationClass="com.rescript.plugin.editor.RescriptSmartEnterProcessor"/>
```
