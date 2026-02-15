# design.md - インデント・コードスタイル

## 1. 実装アプローチ

### 方針

現在のプラグインは軽量パーサー（トップレベル宣言のみ）を採用しており、式レベルの PSI ツリーを持たない。そのため、PSI ツリーに依存する `FormattingModelBuilder` ではなく、エディタのドキュメントテキストから直接インデントを計算する `LineIndentProvider` を採用する。

ドキュメント全体のリフォーマット（Ctrl+Alt+L）は LSP の `textDocument/formatting` に委譲し、プラグイン側では Enter キー押下時のスマートインデントに特化する。

### 採用する Extension Point

| Extension Point | クラス | 目的 |
|---|---|---|
| `com.intellij.lineIndentProvider` | `RescriptLineIndentProvider` | Enter キー押下時のスマートインデント |
| `com.intellij.langCodeStyleSettingsProvider` | `RescriptCodeStyleSettingsProvider` | Settings UI にインデント設定を表示 |

### 不要な Extension Point

| Extension Point | 理由 |
|---|---|
| `com.intellij.lang.formatter` | PSI ツリーが不十分。リフォーマットは LSP に委譲 |
| `com.intellij.enterHandlerDelegate` | `LineIndentProvider` でカバーされるため不要 |
| `com.intellij.typedHandler` | 現時点ではスコープ外 |
| `com.intellij.enterBetweenBracesDelegate` | 後述の通り、`LineIndentProvider` 内でブレース分割ロジックを実装 |

## 2. コンポーネント設計

### 2.1 RescriptLineIndentProvider

**ファイル**: `src/main/kotlin/com/rescript/plugin/codestyle/RescriptLineIndentProvider.kt`

**役割**: Enter キー押下時にカーソル位置のインデント文字列を返す。

**インターフェース**:
```kotlin
class RescriptLineIndentProvider : LineIndentProvider {
    fun isSuitableFor(language: Language?): Boolean
    fun getLineIndent(project: Project, editor: Editor, language: Language?, offset: Int): String?
}
```

**インデント計算ロジック**:

```
1. offset から現在行と前の行の情報を取得
2. 前の行のインデント文字列（先頭のスペース/タブ）を取得
3. 前の行の末尾（ホワイトスペース・コメント除く）を確認:
   a. `{`, `(`, `[` で終わる → インデント +1 レベル
   b. `=>` で終わる → インデント +1 レベル
   c. それ以外 → 前の行のインデントを維持
4. 返す前に、CodeStyleSettings からインデントサイズを取得して適用
```

**インデントサイズの取得**:
```kotlin
val settings = CodeStyle.getSettings(editor)
val indentOptions = settings.getIndentOptions(RescriptFileType.INSTANCE)
val indentSize = indentOptions.INDENT_SIZE  // デフォルト: 2
val useTab = indentOptions.USE_TAB_CHARACTER // デフォルト: false
```

**エッジケースの扱い**:

| ケース | 動作 |
|---|---|
| ファイル先頭で Enter | `""` を返す |
| 前の行が空行 | さらに前の非空行を探してインデントを維持 |
| 前の行がコメントのみ | コメントのインデントを維持 |
| 文字列リテラル内 | `null` を返す（デフォルト動作に委譲） |

### 2.2 RescriptCodeStyleSettingsProvider

**ファイル**: `src/main/kotlin/com/rescript/plugin/codestyle/RescriptCodeStyleSettingsProvider.kt`

**役割**: Settings > Editor > Code Style > ReScript にインデント設定 UI を提供。

**実装内容**:
```kotlin
class RescriptCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage(): Language = RescriptLanguage

    override fun customizeDefaults(
        commonSettings: CommonCodeStyleSettings,
        indentOptions: CommonCodeStyleSettings.IndentOptions
    ) {
        indentOptions.INDENT_SIZE = 2
        indentOptions.CONTINUATION_INDENT_SIZE = 2
        indentOptions.TAB_SIZE = 2
        indentOptions.USE_TAB_CHARACTER = false
    }

    override fun customizeSettings(
        consumer: CodeStyleSettingsCustomizable,
        settingsType: SettingsType
    ) {
        // Tabs and Indents タブのみ表示
        if (settingsType == SettingsType.INDENT_SETTINGS) {
            consumer.showStandardOptions(
                "INDENT_SIZE",
                "CONTINUATION_INDENT_SIZE",
                "TAB_SIZE",
                "USE_TAB_CHARACTER",
                "SMART_TABS"
            )
        }
    }

    override fun getCodeSample(settingsType: SettingsType): String {
        // プレビュー用の ReScript コードサンプル
    }
}
```

**デフォルト設定値**:

| 設定 | デフォルト値 | 説明 |
|---|---|---|
| Indent size | 2 | ReScript の標準インデント |
| Continuation indent size | 2 | 継続行のインデント |
| Tab size | 2 | タブ幅 |
| Use tab character | false | スペースを使用 |

## 3. ファイル構成

### 新規作成ファイル

```
src/main/kotlin/com/rescript/plugin/codestyle/
├── RescriptLineIndentProvider.kt          # スマートインデント
└── RescriptCodeStyleSettingsProvider.kt   # コードスタイル設定 UI
```

### 変更ファイル

```
src/main/resources/META-INF/plugin.xml     # Extension Point 登録追加
```

### テストファイル

```
src/test/kotlin/com/rescript/plugin/codestyle/
└── RescriptLineIndentProviderTest.kt      # インデントロジックのユニットテスト
```

## 4. plugin.xml への登録

```xml
<!-- Code style -->
<langCodeStyleSettingsProvider
        implementation="com.rescript.plugin.codestyle.RescriptCodeStyleSettingsProvider"/>

<!-- Smart indentation on Enter -->
<lineIndentProvider
        implementation="com.rescript.plugin.codestyle.RescriptLineIndentProvider"/>
```

## 5. インデントルール詳細

### インデント増加トリガー（前の行の末尾）

| トリガー | 例 | 備考 |
|---|---|---|
| `{` | `module Foo = {` | ブロック開始 |
| `(` | `let foo = bar(` | 関数引数の複数行 |
| `[` | `let arr = [` | 配列/リストリテラル |
| `=>` | `\| Some(x) =>` | switch のアーム、アロー関数 |

### 末尾判定の注意点

末尾のホワイトスペースは無視する。行末コメント（`// ...`）がある場合は、コメントの前の実質的な末尾を判定する。

```rescript
let foo = {  // ← { が実質的な末尾
  bar
}
```

この判定にはレクサーを使用してトークン列を取得し、ホワイトスペースとコメントを除いた最後のトークンを確認する。

### インデント維持

上記のトリガーに該当しない場合、前の非空行のインデントレベルを維持する。

### `null` を返すケース（デフォルト動作に委譲）

- 文字列リテラル内（`STRING_VALUE`, `JS_STRING_OPEN`/`JS_STRING_CLOSE` 間）
- テンプレートリテラル内

## 6. テスト戦略

`LineIndentProvider` のテストは `getLineIndent()` メソッドを直接テストする。IntelliJ テストフレームワーク（`BasePlatformTestCase`）を使用してエディタ環境をシミュレートする。

### テストケース

| カテゴリ | テストケース |
|---|---|
| ブレース | `{` の後で +1 インデント |
| ブレース | `}` の行は -1 インデント |
| 括弧 | `(` の後で +1 インデント |
| 括弧 | `)` の行は -1 インデント |
| ブラケット | `[` の後で +1 インデント |
| アロー | `=>` の後で +1 インデント |
| 維持 | 通常の行は前の行のインデントを維持 |
| 空行 | 前の行が空の場合、さらに前の行を参照 |
| コメント | 行末コメントを無視して末尾を判定 |
| ネスト | 複数レベルのネストが正しく動作 |
| ファイル先頭 | 0 インデント |

## 7. 影響範囲

### 既存コードへの影響
- **plugin.xml**: Extension Point 登録を 2 行追加のみ
- **既存クラス**: 変更なし

### 他機能との相互作用
- **LSP フォーマッティング**: 競合なし。`LineIndentProvider` は Enter キーのみ、LSP は Ctrl+Alt+L のみ担当
- **BraceMatcher**: 既存の `RescriptBraceMatcher` とは独立して動作
- **FoldingBuilder**: 既存の `RescriptFoldingBuilder` とは独立して動作
