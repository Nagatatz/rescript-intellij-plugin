# Window Instructions: P3 Batch Tier 2

## Window 1: Statement Up/Down Mover

```
cd /Users/ngtz/Documents/repos/rescript-wt-statement-mover

ブランチ `feature/statement-mover` で Statement Up/Down Mover を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-statement-mover/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### 機能概要
Alt+Shift+Up/Down でトップレベル宣言を上下移動する StatementUpDownMover。

### 実装内容
- **新規**: `src/main/kotlin/com/rescript/plugin/editor/RescriptStatementUpDownMover.kt`
  - `StatementUpDownMover` を継承
  - `checkAvailable(editor, file, info, down)` で caret 位置の PSI 要素からトップレベル宣言を特定
  - `PsiTreeUtil.findFirstParent` で宣言ノード（NAVIGABLE_TYPES + OPEN_STATEMENT + INCLUDE_STATEMENT）を探す
  - 先行する ANNOTATION ノードも宣言に含める（`@genType let foo = ...` をまとめて移動）
  - 隣接する宣言（上/下）を探し、`info.toMove` / `info.toMove2` に行範囲を設定
- **新規**: `src/test/kotlin/com/rescript/plugin/editor/RescriptStatementUpDownMoverTest.kt`
  - テストケース: 宣言の上下移動、アノテーション付き宣言の一括移動、ファイル先頭/末尾での移動不可、モジュール内宣言
- **変更**: `src/main/resources/META-INF/plugin.xml` — `<statementUpDownMover>` 追加

### plugin.xml 追加内容
```xml
<!-- Statement Up/Down Mover -->
<statementUpDownMover
    implementation="com.rescript.plugin.editor.RescriptStatementUpDownMover"/>
```

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p3-batch-tier2` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p3-batch-tier2
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/statement-mover
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-statement-mover
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/statement-mover

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 2: Qualified Name Copy

```
cd /Users/ngtz/Documents/repos/rescript-wt-qualified-name

ブランチ `feature/qualified-name` で Qualified Name Copy を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-qualified-name/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### 機能概要
Cmd+Shift+Alt+C で完全修飾名（`Module.SubModule.functionName`）をコピーする QualifiedNameProvider。

### 実装内容
- **新規**: `src/main/kotlin/com/rescript/plugin/navigation/RescriptQualifiedNameProvider.kt`
  - `QualifiedNameProvider` を実装
  - `getQualifiedName(element)`:
    - 対象: NAVIGABLE_TYPES のいずれかに合致する PSI 要素
    - ファイル名（拡張子なし）をルートモジュール名に変換（先頭大文字化: `myModule` → `MyModule`）
    - 親 MODULE_DECLARATION を辿り、`RescriptPsiUtils.extractName()` で各レベルの名前を取得
    - パス結合: `FileName.Module.SubModule.functionName`
  - `qualifiedNameToElement(fqn, project)`: null を返す（LSP が担当）
- **新規**: `src/test/kotlin/com/rescript/plugin/navigation/RescriptQualifiedNameProviderTest.kt`
  - テストケース: トップレベル let, ネストモジュール内 let, module 自体, type, 非対象要素
- **変更**: `src/main/resources/META-INF/plugin.xml` — `<qualifiedNameProvider>` 追加

### plugin.xml 追加内容
```xml
<!-- Qualified Name Copy -->
<qualifiedNameProvider
    implementation="com.rescript.plugin.navigation.RescriptQualifiedNameProvider"/>
```

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p3-batch-tier2` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p3-batch-tier2
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/qualified-name
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-qualified-name
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/qualified-name

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```

---

## Window 3: Smart Enter

```
cd /Users/ngtz/Documents/repos/rescript-wt-smart-enter

ブランチ `feature/smart-enter` で Smart Enter Processor を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成
`.steering/20260218-smart-enter/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### 機能概要
Shift+Enter で文を補完して改行する SmartEnterProcessor。未閉じ括弧の補完、switch ブレース補完、パイプ行 `=>` 補完など。

### 実装内容
- **新規**: `src/main/kotlin/com/rescript/plugin/editor/RescriptSmartEnterProcessor.kt`
  - `SmartEnterProcessor` を継承
  - `process(project, editor, psiFile)`:
    1. caret 位置の行テキストをレキサーで解析
    2. 未閉じ括弧の補完: `{` → `}`, `(` → `)`, `[` → `]`
       - 行頭からレキサーでトークン化し、括弧のバランスを計算
       - 未閉じなら閉じ括弧を挿入して改行
    3. `switch` 文のブレース補完: `switch expr` の後に `{` がなければ ` {\n| \n}` を挿入
    4. パイプ行の `=>` 補完: `| pattern` の後に `=>` がなければ追加
    5. 上記以外: 通常改行 + インデント
  - `RescriptLexer` を使ったトークン解析（`RescriptLineIndentProvider` のパターンを踏襲）
- **新規**: `src/test/kotlin/com/rescript/plugin/editor/RescriptSmartEnterProcessorTest.kt`
  - テストケース: 未閉じ `{` 補完, 未閉じ `(` 補完, switch ブレース補完, `|` 後の `=>` 補完, 通常改行
- **変更**: `src/main/resources/META-INF/plugin.xml` — `<lang.smartEnterProcessor>` 追加

### plugin.xml 追加内容
```xml
<!-- Smart Enter -->
<lang.smartEnterProcessor language="ReScript"
    implementationClass="com.rescript.plugin.editor.RescriptSmartEnterProcessor"/>
```

## ステップ 2: 実装
設計に従い実装。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: コミット
tasklist.md を更新してコミット。
※ 共有ドキュメント（CLAUDE.md, product-requirements.md, functional-design.md）はバッチブランチで一括更新するため、このウィンドウでは更新不要。

## ステップ 5: マージ確認
コミット完了後、ユーザーに「バッチブランチ `feature/p3-batch-tier2` にマージして worktree を削除しますか？」と確認。
承認された場合:
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin checkout feature/p3-batch-tier2
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/smart-enter
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-smart-enter
  git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/smart-enter

## ステップ 6: 元のディレクトリに戻る
cd /Users/ngtz/Documents/repos/rescript-intellij-plugin
```
