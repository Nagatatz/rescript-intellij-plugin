# Design: Quote Handler

## 実装アプローチ

`SimpleTokenSetQuoteHandler` を継承し、ReScript の文字列トークン（`STRING_VALUE`, `JS_STRING_OPEN`/`JS_STRING_CLOSE`）に対するスマート引用符補完を提供する。

## 変更するコンポーネント

### 新規ファイル

#### `src/main/kotlin/com/rescript/plugin/editor/RescriptQuoteHandler.kt`

`SimpleTokenSetQuoteHandler` を継承。

```kotlin
class RescriptQuoteHandler : SimpleTokenSetQuoteHandler(
    RescriptTokenTypes.STRING_VALUE,
    RescriptTokenTypes.JS_STRING_OPEN,
    RescriptTokenTypes.JS_STRING_CLOSE,
)
```

`SimpleTokenSetQuoteHandler` は以下を自動で処理する:
- `"` 入力時に `""` を挿入してカーソルを中間に配置
- 閉じ引用符の直前での overtype
- 指定トークン内での自動補完抑制

テンプレートリテラル（`` ` ``）の補完は、`JS_STRING_OPEN`/`JS_STRING_CLOSE` トークンが含まれることで自動的に処理される。

### 変更ファイル

#### `src/main/resources/META-INF/plugin.xml`

```xml
<lang.quoteHandler language="ReScript"
    implementationClass="com.rescript.plugin.editor.RescriptQuoteHandler"/>
```

## 影響範囲

- 既存コードへの変更は `plugin.xml` への登録追加のみ
- 新規パッケージ `editor` を追加
- 最もシンプルな実装（1ファイル + plugin.xml 登録のみ）
