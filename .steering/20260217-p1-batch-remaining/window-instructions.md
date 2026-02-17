# 各ウィンドウへの命令文

## 前提

各ウィンドウは `/Users/ngtz/Documents/repos/rescript-intellij-plugin` で Claude Code が起動済み。
命令文の冒頭で worktree ディレクトリへ `cd` する。

| ウィンドウ | worktree パス | ブランチ |
|-----------|-------------|---------|
| Window 1 | `../rescript-wt-json-schema` | `feature/json-schema` |
| Window 2 | `../rescript-wt-raw-js` | `feature/raw-js-highlight` |
| Window 3 | `../rescript-wt-postfix` | `feature/postfix-completion` |
| Window 4 | `../rescript-wt-console-filter` | `feature/console-filter` |
| Window 5 | `../rescript-wt-notification` | `feature/editor-notification` |
| Window 6 | `../rescript-wt-goto-related` | `feature/goto-related` |

---

## Window 1: JSON Schema 提供

```
まず `cd /Users/ngtz/Documents/repos/rescript-wt-json-schema` を実行してください。以降すべての作業をこのディレクトリで行います。

ブランチ `feature/json-schema` で rescript.json の JSON Schema 提供機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-json-schema/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要点
- 機能: `rescript.json` / `bsconfig.json` に対して JSON Schema を提供し、プロパティ補完・バリデーションを有効にする
- 受け入れ条件:
  - `rescript.json` でプロパティ名の補完が効く
  - 不正なプロパティに対してバリデーション警告が表示される
  - `bsconfig.json` にも同じスキーマが適用される

### design.md の要点
- 新規ファイル:
  - `src/main/kotlin/com/rescript/plugin/config/RescriptJsonSchemaProviderFactory.kt`
  - `src/main/resources/schemas/rescript.schema.json`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針:
  - `JsonSchemaProviderFactory` を実装
  - `isAvailable` で `rescript.json` または `bsconfig.json` のファイル名を検出
  - スキーマは `resources/schemas/rescript.schema.json` にバンドル
  - JSON プラグインへの optional dependency が必要: `<depends optional="true" config-file="rescript-json.xml">com.intellij.modules.json</depends>`
  - rescript-json.xml に jsonSchemaProviderFactory を登録
- スキーマの主要プロパティ: `name` (string), `sources` (object/array), `package-specs` (object/array), `suffix` (string), `bs-dependencies` (string[]), `bs-dev-dependencies` (string[]), `bsc-flags` (string[]), `jsx` (object: version, mode), `reason` (object), `warnings` (object), `namespace` (boolean/string), `gentypeconfig` (object), `uncurried` (boolean)
- rescript-lang の公式ドキュメント https://rescript-lang.org/docs/manual/latest/build-configuration を参考にスキーマを作成

### tasklist.md
タスクリスト形式で作成。テスト省略理由: JSON Schema の統合テストは IDE フレームワーク全体の起動が必要で単体テスト困難。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: ドキュメント更新
1. **CLAUDE.md** — プロジェクト構成の `config/` に `RescriptJsonSchemaProviderFactory.kt` を追加、`resources/` に `schemas/rescript.schema.json` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに追加。P1 テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」に追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット
- tasklist.md を `[x]` に更新
- コミットメッセージ: `✨ Add JSON Schema for rescript.json`

## ステップ 6: マージ確認
コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/json-schema`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-json-schema`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/json-schema`
```

---

## Window 2: %raw() JS ハイライト

```
まず `cd /Users/ngtz/Documents/repos/rescript-wt-raw-js` を実行してください。以降すべての作業をこのディレクトリで行います。

ブランチ `feature/raw-js-highlight` で %raw() 内の JavaScript ハイライト機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-raw-js-highlight/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要点
- 機能: `%raw("...")` および `%%raw(\`...\`)` 内の JavaScript コードをハイライトする
- 受け入れ条件:
  - `%raw("...")` 内の文字列が JavaScript としてハイライトされる
  - `%%raw(\`...\`)` テンプレートリテラル内も同様
  - JavaScript プラグインが利用不可な環境でもエラーにならない

### design.md の要点
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/injection/RescriptRawJsInjector.kt`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針:
  - `MultiHostInjector` を実装
  - `elementsToInjectIn()` で ReScript の PSI 要素（`ASTWrapperPsiElement` 等）を返す
  - `getLanguagesToInject()` でファイル全体のテキストを走査し、`%raw(` / `%%raw(` に続く文字列リテラルを見つけたら JavaScript を注入
  - 実際のアプローチ: PSI ツリーの `STRING_VALUE` トークンを持つ PsiElement を走査し、その前のトークン列が `PERCENT` + `RAW` + `LPAREN` であるかチェック
  - JavaScript 言語は `Language.findLanguageByID("JavaScript")` または `Language.findLanguageByID("ECMAScript 6")` で取得（null なら注入しない）
  - JavaScript プラグインへの optional dependency: `<depends optional="true" config-file="rescript-js-injection.xml">JavaScript</depends>`
  - `rescript-js-injection.xml` に `<multiHostInjector>` を登録
- レクサーの状態: `Rescript.flex` で `raw` は `RAW` トークンとして認識済み。`%raw("js code")` は `PERCENT` → `RAW` → `LPAREN` → `STRING_VALUE` → `RPAREN` のトークン列になる

### tasklist.md
タスクリスト形式で作成。テスト省略理由: 言語インジェクションのテストは JavaScript プラグインとの結合が必要で単体テスト困難。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: ドキュメント更新
1. **CLAUDE.md** — プロジェクト構成に `injection/RescriptRawJsInjector.kt` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに追加。P1 テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」に追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット
- tasklist.md を `[x]` に更新
- コミットメッセージ: `✨ Add JavaScript highlighting in %raw() blocks`

## ステップ 6: マージ確認
コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/raw-js-highlight`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-raw-js`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/raw-js-highlight`
```

---

## Window 3: Postfix Completion

```
まず `cd /Users/ngtz/Documents/repos/rescript-wt-postfix` を実行してください。以降すべての作業をこのディレクトリで行います。

ブランチ `feature/postfix-completion` で Postfix Completion 機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-postfix-completion/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要点
- 機能: `.switch`, `.pipe`, `.log`, `.some`, `.ok`, `.error`, `.ignore` 等の式後方補完
- 受け入れ条件:
  - `expr.switch` → `switch expr { | _ => }` に展開
  - `expr.pipe` → `expr->` に展開
  - `expr.log` → `Console.log(expr)` に展開
  - `expr.some` → `Some(expr)` に展開
  - `expr.ok` → `Ok(expr)` に展開
  - `expr.error` → `Error(expr)` に展開
  - `expr.ignore` → `expr->ignore` に展開
  - Settings > Editor > General > Postfix Completion で確認可能

### design.md の要点
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/completion/RescriptPostfixTemplateProvider.kt`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針:
  - `PostfixTemplateProvider` を実装
  - 各テンプレートは簡易な `PostfixTemplate` を継承したインナークラスとして実装
  - `isApplicable`: ReScript ファイルであること、コメント・文字列内でないことを確認
  - `expand`: `DocumentUtil` / `PsiDocumentManager` を使用してテキスト置換
  - 各テンプレートのキーは `.switch`, `.pipe`, `.log`, `.some`, `.ok`, `.error`, `.ignore`
- plugin.xml:
  ```xml
  <codeInsight.postfixTemplateProvider language="ReScript"
      implementationClass="com.rescript.plugin.completion.RescriptPostfixTemplateProvider"/>
  ```

### tasklist.md
タスクリスト形式で作成。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: ドキュメント更新
1. **CLAUDE.md** — プロジェクト構成に `completion/RescriptPostfixTemplateProvider.kt` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに追加。P1 テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」に追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット
- tasklist.md を `[x]` に更新
- コミットメッセージ: `✨ Add ReScript postfix completion templates`

## ステップ 6: マージ確認
コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/postfix-completion`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-postfix`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/postfix-completion`
```

---

## Window 4: Console Filter

```
まず `cd /Users/ngtz/Documents/repos/rescript-wt-console-filter` を実行してください。以降すべての作業をこのディレクトリで行います。

ブランチ `feature/console-filter` で Console Filter 機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-console-filter/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要点
- 機能: ReScript コンパイラのエラー出力に含まれるファイルパス:行番号をクリック可能なハイパーリンクにする
- 受け入れ条件:
  - コンパイルエラー出力のファイルパスがクリック可能リンクになる
  - クリックで該当ファイルの該当行にジャンプ
  - Run/Debug ウィンドウの出力に対して動作

### design.md の要点
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/run/RescriptConsoleFilterProvider.kt`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針:
  - `ConsoleFilterProvider` を実装し、`Filter` を返す
  - ReScript コンパイラのエラー出力パターンを正規表現でマッチ:
    - パターン1: `  /path/to/file.res:10:5-15` — 絶対パス:行:列
    - パターン2: `  path/to/file.res:10:5-10:15` — 相対パス:行:列-行:列
    - パターン3: `  File "path/to/file.res", line 10` — OCaml 形式
  - 正規表現: `\s*((?:[A-Za-z]:)?[^\s:]+\.resi?):(\d+):(\d+)` でファイルパス、行、列を抽出
  - マッチした部分に `OpenFileHyperlinkInfo` を設定
  - プロジェクトの `basePath` を基準に相対パスを解決
- plugin.xml:
  ```xml
  <consoleFilterProvider implementation="com.rescript.plugin.run.RescriptConsoleFilterProvider"/>
  ```

### tasklist.md
タスクリスト形式で作成。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: ドキュメント更新
1. **CLAUDE.md** — プロジェクト構成の `run/` セクションに `RescriptConsoleFilterProvider.kt` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに追加。P1 テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」に追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット
- tasklist.md を `[x]` に更新
- コミットメッセージ: `✨ Add console filter for ReScript compiler output`

## ステップ 6: マージ確認
コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/console-filter`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-console-filter`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/console-filter`
```

---

## Window 5: Editor Notification Bar

```
まず `cd /Users/ngtz/Documents/repos/rescript-wt-notification` を実行してください。以降すべての作業をこのディレクトリで行います。

ブランチ `feature/editor-notification` で Editor Notification Bar 機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-editor-notification/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要点
- 機能: `@rescript/language-server` が未インストールの場合、.res ファイル編集時にエディタ上部に案内バーを表示
- 受け入れ条件:
  - LSP サーバー未検出時、エディタ上部に警告バー表示
  - "Configure" で Settings > Languages & Frameworks > ReScript を開く
  - "Dismiss" で閉じ、再表示しない
  - LSP 利用可能時はバー非表示

### design.md の要点
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/editor/RescriptEditorNotificationProvider.kt`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針:
  - `EditorNotificationProvider` を実装
  - `collectNotificationData()` で `.res` / `.resi` ファイルかチェック
  - LSP サーバーの検出ロジック: プロジェクトルートの `node_modules/@rescript/language-server/` ディレクトリの存在チェックに簡略化
  - `RescriptProjectSettings` の `lspServerPath` が設定済みなら、バーを表示しない
  - 見つからない場合 `EditorNotificationPanel` を返す:
    - テキスト: "ReScript Language Server not found. Install @rescript/language-server for full IDE support."
    - アクション "Configure...": `ShowSettingsUtil.getInstance().showSettingsDialog(project, "ReScript")`
    - アクション "Dismiss": `PropertiesComponent` で dismiss フラグを保存、`EditorNotifications.getInstance(project).updateAllNotifications()` で再描画
  - dismiss したら以後表示しない（`PropertiesComponent` のプロジェクト単位キーで管理）
- plugin.xml:
  ```xml
  <editorNotificationProvider
      implementation="com.rescript.plugin.editor.RescriptEditorNotificationProvider"/>
  ```

### tasklist.md
タスクリスト形式で作成。テスト省略理由: LSP サーバー検出との結合、EditorNotificationPanel の UI 表示テストが単体テスト困難。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: ドキュメント更新
1. **CLAUDE.md** — プロジェクト構成の `editor/` セクションに `RescriptEditorNotificationProvider.kt` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに追加。P1 テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」に追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット
- tasklist.md を `[x]` に更新
- コミットメッセージ: `✨ Add editor notification for missing language server`

## ステップ 6: マージ確認
コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/editor-notification`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-notification`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/editor-notification`
```

---

## Window 6: Go to Related

```
まず `cd /Users/ngtz/Documents/repos/rescript-wt-goto-related` を実行してください。以降すべての作業をこのディレクトリで行います。

ブランチ `feature/goto-related` で Go to Related 機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-goto-related/` ディレクトリを作成し、requirements.md, design.md, tasklist.md を作成。

### requirements.md の要点
- 機能: `Navigate > Related Symbol` (Ctrl+Alt+Home) で `.res` ↔ `.resi` ↔ `.js` 間の関連ファイルジャンプ
- 受け入れ条件:
  - `.res` から: 対応 `.resi` と生成 `.js` ファイルが候補に表示
  - `.resi` から: 対応 `.res` ファイルが候補に表示
  - 候補を選択すると該当ファイルが開く
  - 対応ファイルが存在しない場合は候補に表示されない

### design.md の要点
- 新規ファイル: `src/main/kotlin/com/rescript/plugin/navigation/RescriptGotoRelatedProvider.kt`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針:
  - `GotoRelatedProvider` を実装
  - `getItems(context: DataContext)` で現在のファイルを取得
  - `.res` ファイルの場合:
    - 同ディレクトリの `.resi` ファイルを検索 (`parent.findChild`)
    - 生成 JS: プロジェクトルートの `lib/js/` 配下で同じ相対パス + `.bs.js` / `.mjs` を検索
  - `.resi` ファイルの場合:
    - 同ディレクトリの `.res` ファイルを検索
  - 各ファイルを `GotoRelatedItem(psiFile, "ReScript Related")` として返す
- plugin.xml:
  ```xml
  <gotoRelatedProvider
      implementation="com.rescript.plugin.navigation.RescriptGotoRelatedProvider"/>
  ```

### tasklist.md
タスクリスト形式で作成。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認
`./gradlew buildPlugin` を実行し、成功を確認。

## ステップ 4: ドキュメント更新
1. **CLAUDE.md** — プロジェクト構成の `navigation/` セクションに `RescriptGotoRelatedProvider.kt` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに追加。P1 テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」に追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット
- tasklist.md を `[x]` に更新
- コミットメッセージ: `✨ Add Go to Related for .res/.resi/.js files`

## ステップ 6: マージ確認
コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/goto-related`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove /Users/ngtz/Documents/repos/rescript-wt-goto-related`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/goto-related`
```
