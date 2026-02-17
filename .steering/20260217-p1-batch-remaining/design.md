# Design: P1 残り全6機能バッチ実装

## 機能別設計

### 1. JSON Schema 提供

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/config/RescriptJsonSchemaProviderFactory.kt`
- `src/main/resources/schemas/rescript.schema.json`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**
- `JsonSchemaProviderFactory` を実装し、`rescript.json` / `bsconfig.json` ファイル名を検出
- スキーマは `resources/schemas/` にバンドル（rescript-vscode の公式スキーマを参考に作成）
- 主要プロパティ: `name`, `sources`, `package-specs`, `suffix`, `bs-dependencies`, `bs-dev-dependencies`, `bsc-flags`, `jsx`, `reason`, `warnings`

**plugin.xml:**
```xml
<json.catalog.exclusion implementation="..."/> <!-- 不要な場合は省略 -->
```
※ IntelliJ の JSON Schema は `com.intellij.modules.json` に依存するため、`<depends optional="true">` で宣言する

**注意:** `com.jetbrains.plugins.JSON` への optional dependency が必要。利用不可なら機能を無効化。

### 2. `%raw()` JS ハイライト

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/injection/RescriptRawJsInjector.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**
- `MultiHostInjector` を実装
- PSI ツリーの `STRING_VALUE` トークンを走査し、直前に `%raw(` または `%%raw(` パターンがあるかチェック
- 一致した場合、文字列リテラルの内側（クォートを除く）に `JavaScriptSupportLoader.ECMA_SCRIPT_6` を注入
- JavaScript プラグインへの optional dependency が必要

**plugin.xml:**
```xml
<multiHostInjector implementation="com.rescript.plugin.injection.RescriptRawJsInjector"/>
```

**注意:** JavaScript プラグインが利用不可な環境では自動的に無効化される。`<depends optional="true" config-file="rescript-js-injection.xml">JavaScript</depends>` で分離するか、ランタイムチェックで対応。

### 3. Postfix Completion

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/completion/RescriptPostfixTemplateProvider.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**
- `PostfixTemplateProvider` を実装
- 各テンプレートは `PostfixTemplateWithExpressionSelector` を継承
- テンプレート一覧:
  - `.switch` → `switch $expr$ {\n| _ => $END$\n}`
  - `.pipe` → `$expr$->`
  - `.log` → `Console.log($expr$)`
  - `.some` → `Some($expr$)`
  - `.ok` → `Ok($expr$)`
  - `.error` → `Error($expr$)`
  - `.ignore` → `$expr$->ignore`
- `isApplicable` で ReScript ファイルかつ文字列/コメント外であることを確認

**plugin.xml:**
```xml
<codeInsight.postfixTemplateProvider language="ReScript"
    implementationClass="com.rescript.plugin.completion.RescriptPostfixTemplateProvider"/>
```

### 4. Console Filter

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/run/RescriptConsoleFilterProvider.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**
- `ConsoleFilterProvider` を実装し、`Filter` を返す
- ReScript コンパイラのエラー出力パターンを正規表現でマッチ:
  - `  <filepath>:<line>:<col>(-<line>:<col>)?` 形式
  - `  We've found a bug for you!` の後の `  /path/to/file.res:10:5-15` 形式
- マッチした部分を `HyperlinkInfo` として `OpenFileDescriptor` でファイル:行にジャンプ

**plugin.xml:**
```xml
<consoleFilterProvider implementation="com.rescript.plugin.run.RescriptConsoleFilterProvider"/>
```

### 5. Editor Notification Bar

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/editor/RescriptEditorNotificationProvider.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**
- `EditorNotificationProvider` を実装
- `.res` / `.resi` ファイルを開いた際に `RescriptLspServerDescriptor` と同じロジックで LSP サーバーの存在を確認
- 見つからない場合、`EditorNotificationPanel` に警告メッセージとアクションを表示:
  - テキスト: "ReScript Language Server not found. Install @rescript/language-server for full IDE support."
  - アクション: "Install" → ターミナルで `npm install @rescript/language-server` を提案
  - アクション: "Configure" → Settings > Languages & Frameworks > ReScript を開く
  - アクション: "Dismiss" → 通知を閉じる（`fileKey` で記憶）
- `RescriptProjectSettings` の LSP パス設定も確認

**plugin.xml:**
```xml
<editorNotificationProvider
    implementation="com.rescript.plugin.editor.RescriptEditorNotificationProvider"/>
```

### 6. Go to Related

**新規ファイル:**
- `src/main/kotlin/com/rescript/plugin/navigation/RescriptGotoRelatedProvider.kt`

**変更ファイル:**
- `src/main/resources/META-INF/plugin.xml`

**設計:**
- `GotoRelatedProvider` を実装
- 現在のファイルに基づいて関連ファイルを検索:
  - `.res` → `.resi`（同ディレクトリ）、`.bs.js` / `.mjs`（`lib/js/` 配下）
  - `.resi` → `.res`（同ディレクトリ）
- 各関連ファイルを `GotoRelatedItem` として返す（グループ名: "ReScript Related"）

**plugin.xml:**
```xml
<gotoRelatedProvider
    implementation="com.rescript.plugin.navigation.RescriptGotoRelatedProvider"/>
```

## ファイル競合分析

共有変更ファイルは `plugin.xml` のみ。各機能は `<extensions>` 内の異なる行に追加するため、マージ時の競合は手動解決で対応。
