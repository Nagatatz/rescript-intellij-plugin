# Design: JSX ハイライト修正

## 修正 1: Islands テーマ対応

**ファイル**: `src/main/resources/META-INF/plugin.xml`

`additionalTextAttributes` に "Dark" と "Light" スキームを追加し、同じ XML ファイルを参照する。

```xml
<additionalTextAttributes scheme="Darcula" file="colorSchemes/RescriptDarcula.xml"/>
<additionalTextAttributes scheme="Dark" file="colorSchemes/RescriptDarcula.xml"/>
<additionalTextAttributes scheme="Default" file="colorSchemes/RescriptDefault.xml"/>
<additionalTextAttributes scheme="Light" file="colorSchemes/RescriptDefault.xml"/>
```

## 修正 2: レクサー修正

**ファイル**: `src/main/java/com/rescript/plugin/lang/Rescript.flex`

### 方針

boolean フラグ `inJsxOpenTag` と brace depth カウンター `jsxAttrBraceDepth` を追加:

- `IN_JSX_TAG_NAME` で `[^]`（空白等）に遭遇 → `inJsxOpenTag = true` にしてから `INITIAL` へ遷移
- `IN_JSX_TAG_NAME` で `>` に遭遇 → フラグ変更なし（既に `TAG_GT` を返す）
- `INITIAL` の `">"` ルール: `inJsxOpenTag && jsxAttrBraceDepth == 0` なら `TAG_GT` を返しフラグリセット
- `INITIAL` の `"{"` ルール: `inJsxOpenTag` なら `jsxAttrBraceDepth++`
- `INITIAL` の `"}"` ルール: `inJsxOpenTag && jsxAttrBraceDepth > 0` なら `jsxAttrBraceDepth--`
- `INITIAL` の `"</"` ルール: フラグリセット

## 修正 3: パーサー修正

**ファイル**: `src/main/kotlin/com/rescript/plugin/lang/RescriptParser.kt`

フォールバックとして `GT` も受け入れるよう修正:

- `skipJsxAttributes`: `TAG_GT` に加えて `GT` でも return
- `parseJsxTagOrSelfClosing` の when 分岐: `GT` も `TAG_GT` と同じ処理

## 修正 4: テスト追加

**ファイル**: `src/test/kotlin/com/rescript/plugin/lang/RescriptLexerTest.kt`

- JSX 属性付きタグの `>` が `TAG_GT` になることを確認
- ブレース内の `>` が `GT` のままであることを確認
