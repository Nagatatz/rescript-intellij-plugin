# Design: パーサーエラーリカバリ

## 実装アプローチ

IntelliJ Platform の `PsiBuilder` が提供するエラー報告 API を活用し、既存の軽量パーサーにエラーリカバリロジックを追加する。

### 使用する PsiBuilder API

| API | 用途 |
|-----|------|
| `PsiBuilder.error(message)` | 現在位置にエラーノード（`PsiErrorElement`）を挿入 |
| `PsiBuilder.Marker.error(message)` | マーカー範囲をエラーノードとして完了 |

## 変更対象ファイル

| ファイル | 変更内容 |
|---------|---------|
| `RescriptParser.kt` | エラーリカバリロジック追加 |
| `RescriptParserTest.kt` | エラーリカバリのテストケース追加 |

## 詳細設計

### 1. トップレベルの不明トークンに対するエラーノード (R1)

**現在の実装:**
```kotlin
else -> b.advanceLexer() // 無言でスキップ
```

**変更後:**
```kotlin
else -> {
    val errorMarker = b.mark()
    // 次のトップレベルキーワードまで不明トークンをまとめてスキップ
    while (!b.eof() && !isTopLevelStart(b.tokenType)) {
        b.advanceLexer()
    }
    errorMarker.error("Unexpected token")
}
```

**ポイント:**
- 連続する不明トークンを1つのエラーノードにまとめる（個別にエラーノードを作ると PSI ツリーが膨大になる）
- `isTopLevelStart()` で次の有効な宣言の開始を検出してリカバリ

### 2. 宣言内の識別子欠落エラー (R2)

**`parseDeclaration` の変更:**

識別子が期待される位置にない場合、`PsiBuilder.error()` でエラーを挿入し、宣言パースを継続する。

```kotlin
// consume identifier (lident or uident or _)
if (b.tokenType in listOf(LIDENT, UIDENT, UNDERSCORE)) {
    b.advanceLexer()
} else if (!b.eof() && !isTopLevelStart(b.tokenType)) {
    b.error("Expected identifier")
}
```

**`parseModuleDeclaration` の変更:**

`module` の後に `UIDENT` がない場合のエラー報告。

```kotlin
if (b.tokenType == RescriptTokenTypes.UIDENT) {
    b.advanceLexer()
} else if (!b.eof() && b.tokenType != RescriptTokenTypes.LBRACE && !isTopLevelStart(b.tokenType)) {
    b.error("Expected module name")
}
```

**設計判断:**
- `b.error()` はゼロ幅のエラーノードを挿入する（トークンを消費しない）
- 宣言ノード自体は `m.done(elementType)` で正常に完了させる → ストラクチャビューへの影響なし
- エラー後も `skipToEndOfDeclaration()` で宣言の残りを消費し、次の宣言に正しくリカバリ

### 3. 不均衡な括弧からのリカバリ (R3)

**`parseModuleDeclaration` の変更:**

モジュール本体で `}` が見つからないまま EOF に到達した場合、またはトップレベルキーワードに到達した場合のリカバリ。

現在の実装では `while (!b.eof() && b.tokenType != RBRACE)` で `parseTopLevel()` をループしているが、`}` がない場合にファイル末尾まで消費してしまう。

```kotlin
// module body
if (b.tokenType == RescriptTokenTypes.LBRACE) {
    b.advanceLexer() // consume '{'
    while (!b.eof() && b.tokenType != RescriptTokenTypes.RBRACE) {
        parseTopLevel(b)
    }
    if (b.tokenType == RescriptTokenTypes.RBRACE) {
        b.advanceLexer() // consume '}'
    } else {
        b.error("Expected '}'")
    }
}
```

**`skipToEndOfDeclaration` の改善:**

EOF到達時にエラーは報告しない（呼び出し元が判断する）。現在の実装は EOF で自然に終了するため、変更不要。

## PSI ツリーへの影響

### 正常なコード（変更なし）
```
FILE
  LET_DECLARATION
    LET, LIDENT("x"), EQ, INT_VALUE("42")
```

### エラーを含むコード（改善後）
```
FILE
  PsiErrorElement("Unexpected token")   ← 不明トークン
    INT_VALUE("42"), EQ, INT_VALUE("1")
  LET_DECLARATION                        ← リカバリ後、正常に認識
    LET, LIDENT("x"), EQ, INT_VALUE("1")
```

```
FILE
  LET_DECLARATION                        ← 識別子欠落でもノード生成
    LET, PsiErrorElement("Expected identifier"), EQ, INT_VALUE("42")
```

## 下流機能への影響分析

| 機能 | 影響 | 理由 |
|------|------|------|
| ストラクチャビュー | なし | 宣言ノードは常に生成される。`extractName()` は識別子がなければ `(anonymous)` を返す（既存ロジック） |
| コード折りたたみ | なし | 宣言ノードの存在と `{` の有無で判定（エラーノードに関係しない） |
| シンタックスハイライト | なし | レクサーベースであり、パーサーに依存しない |
| LSP 機能 | なし | LSP は独立したプロセスであり、PSI ツリーを参照しない |

## テスト計画

| テストケース | 検証内容 |
|-------------|---------|
| 不明トークンのみのファイル | エラーノード生成 & パーサークラッシュなし |
| `let = 42` | 識別子欠落エラー & LET_DECLARATION ノード生成 |
| `module = {}` | モジュール名欠落エラー & MODULE_DECLARATION ノード生成 |
| エラー後の正常宣言 | リカバリ後に後続宣言が正しく認識される |
| 閉じ括弧欠落のモジュール | エラー報告 & 後続宣言のリカバリ |
| 複合エラー | 複数エラーが存在するファイルでの安定性 |
| 既存テスト全パス | 回帰なし |
