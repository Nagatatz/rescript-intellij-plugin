# 各ウィンドウへの命令文

## セットアップ

各ウィンドウで以下のディレクトリを開いて `claude` を起動し、命令文を貼り付ける。

| ウィンドウ | ディレクトリ | ブランチ |
|-----------|-------------|---------|
| Window 1 | `../rescript-wt-switch` | `feature/res-resi-switch` |
| Window 2 | `../rescript-wt-live-templates` | `feature/live-templates` |
| Window 3 | `../rescript-wt-file-templates` | `feature/file-templates` |
| Window 4 | `../rescript-wt-spell-checking` | `feature/spell-checking` |

---

## Window 1: `.res`/`.resi` 切り替え

```
ブランチ `feature/res-resi-switch` で `.res`/`.resi` ファイル切り替え機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-res-resi-switch/` ディレクトリを作成し、以下の3ファイルを作成してください。

### requirements.md

- 機能: `Alt+O` キーボードショートカットで `.res` ↔ `.resi` ファイルを切り替える
- ユーザーストーリー: ReScript 開発者として、実装ファイルとインターフェースファイルをショートカットで素早く切り替えたい
- 受け入れ条件:
  - `.res` で `Alt+O` → 同名 `.resi` を開く
  - `.resi` で `Alt+O` → 同名 `.res` を開く
  - 対応ファイルが存在しない場合はエラーにならない
  - メニュー「Navigate > Switch .res/.resi」からもアクセス可能

### design.md

- 新規ファイル: `src/main/kotlin/com/rescript/plugin/navigation/RescriptSwitchFileAction.kt`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針: `AnAction` を継承。`actionPerformed` で現在ファイルの拡張子を切り替え、`VirtualFile.parent.findChild()` で対応ファイルを検索し、`FileEditorManager.openFile()` で開く。`update` で `.res`/`.resi` 以外を無効化。
- plugin.xml: `</idea-plugin>` の直前に `<actions>` セクションを新規追加。`<action id="ReScript.SwitchFile">` を登録。`<keyboard-shortcut first-keystroke="alt O" keymap="$default"/>`。`<add-to-group group-id="GoToMenu" anchor="last"/>`。

### tasklist.md

タスクリスト形式で作成。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認

`./gradlew buildPlugin` を実行し、成功を確認してください。

## ステップ 4: ドキュメント更新

以下のドキュメントを更新してください:

1. **CLAUDE.md** — プロジェクト構成の `navigation/` セクションに `RescriptSwitchFileAction.kt` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに `.res/.resi 切り替え` を追加。「将来機能 > P1」テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」テーブルに `action` (RescriptSwitchFileAction) を追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット

- tasklist.md のすべてのタスクを `[x]` に更新
- コミットメッセージ: `✨ Add .res/.resi file switch action (Alt+O)`
- ドキュメント更新と tasklist.md 更新をコミットに含める

## ステップ 6: マージ確認

コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合、以下を実行:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/res-resi-switch`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove ../rescript-wt-switch`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/res-resi-switch`
```

---

## Window 2: Live Templates

```
ブランチ `feature/live-templates` で Live Templates 機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-live-templates/` ディレクトリを作成し、以下の3ファイルを作成してください。

### requirements.md

- 機能: ReScript の頻出構文パターンをスニペットとして提供する
- ユーザーストーリー: ReScript 開発者として、頻出する構文をスニペットで素早く挿入したい
- 受け入れ条件:
  - 以下の15スニペットが利用可能:
    `let`, `letfn`, `mod`, `modt`, `typ`, `typv`, `typr`, `ext`, `sw`, `try`, `for`, `if`, `ife`, `pipe`, `log`
  - Settings > Editor > Live Templates > ReScript で確認・編集可能
  - Tab キーで変数部分を移動できる

### design.md

- 新規ファイル: `src/main/resources/liveTemplates/ReScript.xml`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針: Kotlin コード不要。XML 定義のみ。`<templateSet group="ReScript">` 内に `<template>` 要素で各スニペットを定義。context は `<option name="OTHER" value="true"/>` を設定。
- テンプレート一覧:

  | 略語 | テンプレートテキスト | 説明 |
  |------|---------------------|------|
  | `let` | `let $NAME$ = $VALUE$$END$` | let binding |
  | `letfn` | `let $NAME$ = ($PARAMS$) => {\n  $END$\n}` | let function |
  | `mod` | `module $NAME$ = {\n  $END$\n}` | module definition |
  | `modt` | `module type $NAME$ = {\n  $END$\n}` | module type |
  | `typ` | `type $NAME$ = $END$` | type definition |
  | `typv` | `type $NAME$ =\n  | $VARIANT$$END$` | variant type |
  | `typr` | `type $NAME$ = {\n  $FIELD$: $TYPE$,\n  $END$\n}` | record type |
  | `ext` | `external $NAME$: $TYPE$ = "$JS_NAME$"` | external binding |
  | `sw` | `switch $EXPR$ {\n| $PATTERN$ => $END$\n}` | switch expression |
  | `try` | `try {\n  $END$\n} catch {\n| $EXN$ => $HANDLER$\n}` | try-catch |
  | `for` | `for $VAR$ in $START$ to $FINISH$ {\n  $BODY$$END$\n}` | for loop |
  | `if` | `if $COND$ {\n  $END$\n}` | if expression |
  | `ife` | `if $COND$ {\n  $THEN$\n} else {\n  $END$\n}` | if-else expression |
  | `pipe` | `->$FUNC$($END$)` | pipe operator |
  | `log` | `Console.log($END$)` | console log |

- plugin.xml: `<extensions>` 内に `<defaultLiveTemplates>/liveTemplates/ReScript</defaultLiveTemplates>` を追加

### tasklist.md

タスクリスト形式で作成。

## ステップ 2: 実装

設計に従い実装してください。各テンプレートには適切な `description` 属性と `toReformat="true"` を設定してください。

## ステップ 3: ビルド確認

`./gradlew buildPlugin` を実行し、成功を確認してください。

## ステップ 4: ドキュメント更新

以下のドキュメントを更新してください:

1. **CLAUDE.md** — プロジェクト構成の `resources/` セクションに `liveTemplates/ReScript.xml` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに `Live Templates` を追加。「将来機能 > P1」テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」テーブルに `defaultLiveTemplates` を追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット

- tasklist.md のすべてのタスクを `[x]` に更新
- コミットメッセージ: `✨ Add ReScript live templates`
- ドキュメント更新と tasklist.md 更新をコミットに含める

## ステップ 6: マージ確認

コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合、以下を実行:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/live-templates`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove ../rescript-wt-live-templates`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/live-templates`
```

---

## Window 3: File Templates

```
ブランチ `feature/file-templates` で File Templates 機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-file-templates/` ディレクトリを作成し、以下の3ファイルを作成してください。

### requirements.md

- 機能: `New > ReScript File` メニューからテンプレートファイルを作成する
- ユーザーストーリー: ReScript 開発者として、新しい ReScript ファイルを IDE の New メニューから作成したい
- 受け入れ条件:
  - `New > ReScript File` でダイアログが表示され、テンプレート（Module / Interface / Component）を選択できる
  - Module: `.res` ファイルを作成
  - Interface: `.resi` ファイルを作成
  - Component: React コンポーネントの `.res` ファイルを作成
  - ファイル名は先頭大文字に自動変換（ReScript モジュール命名規則）

### design.md

- 新規ファイル:
  - `src/main/kotlin/com/rescript/plugin/template/RescriptCreateFileAction.kt`
  - `src/main/resources/fileTemplates/internal/ReScript Module.res.ft`
  - `src/main/resources/fileTemplates/internal/ReScript Interface.resi.ft`
  - `src/main/resources/fileTemplates/internal/ReScript Component.res.ft`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針:
  - `CreateFileFromTemplateAction` を継承したアクションクラスを作成
  - `buildDialog` でファイル名入力 + テンプレート選択ダイアログを表示
  - 入力名の先頭を大文字に変換
  - `RescriptIcons.FILE` をアイコンとして使用
- テンプレート内容:
  - `ReScript Module.res.ft`: `// ${NAME} module\n`
  - `ReScript Interface.resi.ft`: `// ${NAME} interface\n`
  - `ReScript Component.res.ft`:
    ```
    @react.component
    let make = () => {
      <div> {React.string("${NAME}")} </div>
    }
    ```
- plugin.xml:
  - `<extensions>` 内に3つの `<internalFileTemplate name="..."/>` を追加
  - `</idea-plugin>` の直前に `<actions>` セクションを追加（もし既にあればその中に追加）
  - `<action id="ReScript.NewFile" class="...RescriptCreateFileAction" text="ReScript File">` を登録
  - `<add-to-group group-id="NewGroup" anchor="before" relative-to-action="NewFile"/>`

### tasklist.md

タスクリスト形式で作成。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認

`./gradlew buildPlugin` を実行し、成功を確認してください。

## ステップ 4: ドキュメント更新

以下のドキュメントを更新してください:

1. **CLAUDE.md** — プロジェクト構成に `template/RescriptCreateFileAction.kt` と `fileTemplates/internal/` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに `File Templates` を追加。「将来機能 > P1」テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」テーブルに `internalFileTemplate` と `action` を追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット

- tasklist.md のすべてのタスクを `[x]` に更新
- コミットメッセージ: `✨ Add ReScript file templates (New > ReScript File)`
- ドキュメント更新と tasklist.md 更新をコミットに含める

## ステップ 6: マージ確認

コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合、以下を実行:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/file-templates`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove ../rescript-wt-file-templates`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/file-templates`
```

---

## Window 4: Spell Checking

```
ブランチ `feature/spell-checking` で Spell Checking 機能を実装してください。
ステアリングワークフローに従い、以下の手順で進めてください。
各ステアリングドキュメントの作成後、承認確認は不要です（親ウィンドウで承認済み）。連続して作成・実装してください。

## ステップ 1: ステアリングドキュメント作成

`.steering/20260217-spell-checking/` ディレクトリを作成し、以下の3ファイルを作成してください。

### requirements.md

- 機能: ReScript ファイル内のコメント・文字列・識別子に対するスペルチェック
- ユーザーストーリー: ReScript 開発者として、変数名やコメント内のスペルミスを IDE に検出してほしい
- 受け入れ条件:
  - コメント（行・ブロック）内のスペルミスを検出
  - 文字列リテラル内のスペルミスを検出
  - 識別子（camelCase/snake_case 分割後）のスペルミスを検出
  - キーワード（let, type 等）はチェック対象外
  - IDE のスペルチェック辞書に追加可能

### design.md

- 新規ファイル: `src/main/kotlin/com/rescript/plugin/spellcheck/RescriptSpellcheckingStrategy.kt`
- 変更ファイル: `src/main/resources/META-INF/plugin.xml`
- 実装方針:
  - `SpellcheckingStrategy` を継承
  - `getTokenizer(element: PsiElement)` をオーバーライド:
    - `element.node.elementType` で判定
    - `SINGLE_COMMENT`, `MULTI_COMMENT` → `SpellcheckingStrategy.TEXT_TOKENIZER`
    - `STRING_VALUE` → `SpellcheckingStrategy.TEXT_TOKENIZER`
    - `LIDENT`, `UIDENT` → `SpellcheckingStrategy.TEXT_TOKENIZER`（IDE が camelCase/snake_case を自動分割してくれる）
    - その他 → `EMPTY_TOKENIZER`
- plugin.xml: `<extensions>` 内に以下を追加:
  ```xml
  <spellchecker.support language="ReScript"
                        implementationClass="com.rescript.plugin.spellcheck.RescriptSpellcheckingStrategy"/>
  ```

### tasklist.md

タスクリスト形式で作成。

## ステップ 2: 実装

設計に従い実装してください。

## ステップ 3: ビルド確認

`./gradlew buildPlugin` を実行し、成功を確認してください。

## ステップ 4: ドキュメント更新

以下のドキュメントを更新してください:

1. **CLAUDE.md** — プロジェクト構成に `spellcheck/RescriptSpellcheckingStrategy.kt` を追加
2. **docs/product-requirements.md** — 「実装済み機能」テーブルに `スペルチェック` を追加。「将来機能 > P1」テーブルから該当行を削除
3. **docs/functional-design.md** — 「Extension Point 登録マップ」テーブルに `spellchecker.support` を追加。「未実装機能」テーブルから該当行を削除

## ステップ 5: コミット

- tasklist.md のすべてのタスクを `[x]` に更新
- コミットメッセージ: `✨ Add spell checking support for ReScript files`
- ドキュメント更新と tasklist.md 更新をコミットに含める

## ステップ 6: マージ確認

コミット完了後、ユーザーに「main ブランチにマージして worktree を削除しますか？」と確認してください。
承認された場合、以下を実行:
1. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin merge feature/spell-checking`
2. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin worktree remove ../rescript-wt-spell-checking`
3. `git -C /Users/ngtz/Documents/repos/rescript-intellij-plugin branch -d feature/spell-checking`
```
