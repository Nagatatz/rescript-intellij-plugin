# Design: TODO Indexer

## 実装アプローチ

`LexerBasedTodoIndexer` を継承し、既存の `RescriptLexer` をベースにした TODO パターンインデクシング用レクサーを提供する。`IdAndTodoScannerBasedOnFilterLexer` を使用してコメントトークンから TODO パターンを検出する。

## 変更するコンポーネント

### 新規ファイル

#### `src/main/kotlin/com/rescript/plugin/indexing/RescriptTodoIndexer.kt`

`LexerBasedTodoIndexer` を継承。

```kotlin
class RescriptTodoIndexer : LexerBasedTodoIndexer() {
    override fun createLexer(consumer: OccurrenceConsumer): Lexer {
        return IdAndTodoScannerBasedOnFilterLexer(
            RescriptLexer(),
            consumer,
        )
    }
}
```

`IdAndTodoScannerBasedOnFilterLexer` は:
- 内部で `RescriptLexer` を使ってトークンを分解
- コメントトークン（`SINGLE_COMMENT`, `MULTI_COMMENT`）を検出
- コメント内容を IntelliJ の TODO パターン設定と照合
- マッチした箇所を TODO インデックスに登録

### 変更ファイル

#### `src/main/kotlin/com/rescript/plugin/lang/RescriptParserDefinition.kt`

`getCommentTokens()` が正しく `COMMENTS` TokenSet を返していることを確認（既存実装で対応済みのはず）。

#### `src/main/resources/META-INF/plugin.xml`

```xml
<todoIndexer filetype="ReScript"
    implementationClass="com.rescript.plugin.indexing.RescriptTodoIndexer"/>
```

注意: `.resi` ファイル用にも登録が必要な場合は追加する。

```xml
<todoIndexer filetype="ReScript Interface"
    implementationClass="com.rescript.plugin.indexing.RescriptTodoIndexer"/>
```

## 影響範囲

- 既存コードへの変更は `plugin.xml` への登録追加のみ
- 既存の `RescriptLexer` を再利用
- 新規パッケージ `indexing` を追加
