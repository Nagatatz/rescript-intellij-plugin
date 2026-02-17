# Design: P1 低難易度バッチ実装

## 並列実装アーキテクチャ

4機能を git worktree で並列実装する。各機能は独立しており、ファイル競合は発生しない。

```
main ─┬─ feature/res-resi-switch    → ../rescript-wt-switch/
      ├─ feature/live-templates     → ../rescript-wt-live-templates/
      ├─ feature/file-templates     → ../rescript-wt-file-templates/
      └─ feature/spell-checking     → ../rescript-wt-spell-checking/
```

## 機能別設計

### 1. `.res`/`.resi` 切り替え

**実装方針:** `AnAction` を作成し、現在のファイルの拡張子を切り替えて対応ファイルを開く。

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptSwitchFileAction.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml` — `<action>` 登録

**設計詳細:**
```kotlin
// AnAction を継承
// actionPerformed:
//   1. 現在のファイルの VirtualFile を取得
//   2. 拡張子が "res" なら "resi"、"resi" なら "res" に切り替え
//   3. 同ディレクトリで対応ファイルを検索
//   4. 存在すれば FileEditorManager で開く
// update:
//   - .res / .resi ファイルでのみ有効化
```

**plugin.xml 登録:**
```xml
<actions>
    <action id="ReScript.SwitchFile"
            class="com.rescript.plugin.navigation.RescriptSwitchFileAction"
            text="Switch .res/.resi"
            description="Switch between .res and .resi files">
        <keyboard-shortcut first-keystroke="alt O" keymap="$default"/>
        <add-to-group group-id="GoToMenu" anchor="last"/>
    </action>
</actions>
```

### 2. Live Templates

**実装方針:** XML 定義ファイルを `resources/liveTemplates/` に配置し、`defaultLiveTemplates` extension point で登録する。コード不要。

**新規ファイル:**
- `src/main/resources/liveTemplates/ReScript.xml`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml` — `<defaultLiveTemplates>` 登録

**テンプレート一覧:**

| 略語 | 展開内容 | 変数 |
|------|---------|------|
| `let` | `let $NAME$ = $VALUE$$END$` | NAME, VALUE |
| `letfn` | `let $NAME$ = ($PARAMS$) => {\n  $END$\n}` | NAME, PARAMS |
| `mod` | `module $NAME$ = {\n  $END$\n}` | NAME |
| `modt` | `module type $NAME$ = {\n  $END$\n}` | NAME |
| `typ` | `type $NAME$ = $END$` | NAME |
| `typv` | `type $NAME$ =\n  \| $VARIANT$$END$` | NAME, VARIANT |
| `typr` | `type $NAME$ = {\n  $FIELD$: $TYPE$,\n  $END$\n}` | NAME, FIELD, TYPE |
| `ext` | `external $NAME$: $TYPE$ = "$JS_NAME$"` | NAME, TYPE, JS_NAME |
| `sw` | `switch $EXPR$ {\n\| $PATTERN$ => $END$\n}` | EXPR, PATTERN |
| `try` | `try {\n  $END$\n} catch {\n\| $EXNPATTERN$ => $HANDLER$\n}` | EXNPATTERN, HANDLER |
| `for` | `for $VAR$ in $START$ to $END_VAL$ {\n  $BODY$$END$\n}` | VAR, START, END_VAL, BODY |
| `if` | `if $COND$ {\n  $END$\n}` | COND |
| `ife` | `if $COND$ {\n  $THEN$\n} else {\n  $END$\n}` | COND, THEN |
| `pipe` | `->$FUNC$($END$)` | FUNC |
| `log` | `Console.log($END$)` | — |

**plugin.xml 登録:**
```xml
<defaultLiveTemplates>/liveTemplates/ReScript</defaultLiveTemplates>
```

### 3. File Templates

**実装方針:** `internalFileTemplate` で内部テンプレートを定義し、`CreateFileFromTemplateAction` を継承したアクションで「New」メニューに追加する。

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/template/RescriptCreateFileAction.kt`
- `src/main/resources/fileTemplates/internal/ReScript Module.res.ft`
- `src/main/resources/fileTemplates/internal/ReScript Interface.resi.ft`
- `src/main/resources/fileTemplates/internal/ReScript Component.res.ft`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml` — `<internalFileTemplate>` + `<action>` 登録

**テンプレート内容:**

ReScript Module (`ReScript Module.res.ft`):
```
// ${NAME} module
```

ReScript Interface (`ReScript Interface.resi.ft`):
```
// ${NAME} interface
```

ReScript Component (`ReScript Component.res.ft`):
```
@react.component
let make = () => {
  <div> {React.string("${NAME}")} </div>
}
```

**アクション設計:**
```kotlin
// CreateFileFromTemplateAction を継承
// buildDialog でファイル名入力ダイアログを表示
// 先頭文字を大文字に変換（ReScript モジュール命名規則）
```

**plugin.xml 登録:**
```xml
<internalFileTemplate name="ReScript Module"/>
<internalFileTemplate name="ReScript Interface"/>
<internalFileTemplate name="ReScript Component"/>

<action id="ReScript.NewModule"
        class="com.rescript.plugin.template.RescriptCreateFileAction"
        text="ReScript File"
        description="Create a new ReScript file">
    <add-to-group group-id="NewGroup" anchor="before" relative-to-action="NewFile"/>
</action>
```

### 4. Spell Checking

**実装方針:** `SpellcheckingStrategy` を継承し、コメント・文字列・識別子に対してスペルチェックを有効にする。

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/spellcheck/RescriptSpellcheckingStrategy.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml` — `<spellchecker.support>` 登録

**設計詳細:**
```kotlin
// SpellcheckingStrategy を継承
// getTokenizer:
//   - SINGLE_COMMENT, MULTI_COMMENT → SpellcheckingStrategy.TEXT_TOKENIZER
//   - STRING_VALUE → SpellcheckingStrategy.TEXT_TOKENIZER
//   - LIDENT, UIDENT → IdentifierTokenizer（camelCase/snake_case 分割）
//   - キーワード → EMPTY_TOKENIZER（チェック対象外）
//   - その他 → EMPTY_TOKENIZER
```

**plugin.xml 登録:**
```xml
<spellchecker.support language="ReScript"
                      implementationClass="com.rescript.plugin.spellcheck.RescriptSpellcheckingStrategy"/>
```

## ファイル競合分析

唯一の共有変更ファイルは `plugin.xml`。各機能が追加するセクションは異なるため、マージ時の競合は手動解決で対応する。

| 機能 | plugin.xml 変更箇所 |
|------|-------------------|
| res/resi 切り替え | `<actions>` セクション（新規追加） |
| Live Templates | `<extensions>` 内に `<defaultLiveTemplates>` |
| File Templates | `<extensions>` 内に `<internalFileTemplate>` + `<actions>` |
| Spell Checking | `<extensions>` 内に `<spellchecker.support>` |
