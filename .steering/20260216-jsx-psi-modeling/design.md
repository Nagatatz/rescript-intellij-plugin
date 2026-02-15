# Design: JSX PSI モデリング

## 設計方針

既存の軽量パーサーアーキテクチャ (トップレベル宣言のみ認識 + 式レベルはスキップ) を拡張し、JSX 構造を PSI ノードとして認識する。JSX パースは宣言本体のスキップ処理中に行い、トップレベル解析と式レベルスキップのバランスを維持する。

## PSI 要素型の追加

### RescriptElementTypes に追加する要素型

```kotlin
JSX_ELEMENT              // <tag>...</tag>
JSX_SELF_CLOSING_ELEMENT // <tag />
JSX_FRAGMENT             // <> ... </>
```

属性やテキストノードは個別にモデル化しない。タグ名の認識はレクサーが既に行っている (`JSX_TAG_NAME`, `JSX_COMPONENT_NAME`)。

## パーサー変更設計

### 現在の動作

```
parseTopLevel → else → skipToEndOfDeclaration (JSX トークンはすべてスキップ)
```

### 変更後の動作

```
parseTopLevel → else → skipToEndOfDeclaration → TAG_LT を検出 → tryParseJsx
                                                TAG_LT_SLASH → braceDepth==0 なら return
```

### JSX パース戦略

パーサーの `skipToEndOfDeclaration` メソッド内で `TAG_LT` トークンを検出した際に JSX パースを試みる。また、トップレベルで直接 JSX が出現した場合 (`parseTopLevel` の `else` ブランチ) にも対応する。

#### tryParseJsx メソッド

```
tryParseJsx(builder):
  mark()

  if TAG_LT:
    advance()  // consume '<'

    if TAG_GT:
      // フラグメント: <> ... </>
      advance()  // consume '>'
      parseJsxChildren()
      if TAG_LT_SLASH:
        advance()  // consume '</'
        if TAG_GT:
          advance()  // consume '>'
      done(JSX_FRAGMENT)

    else if JSX_TAG_NAME or JSX_COMPONENT_NAME:
      tagName = currentText
      advance()  // consume tag name
      skipJsxAttributes()

      if TAG_AUTO_CLOSE:
        advance()  // consume '/>'
        done(JSX_SELF_CLOSING_ELEMENT)

      else if TAG_GT:
        advance()  // consume '>'
        parseJsxChildren()
        expectClosingTag(tagName)
        done(JSX_ELEMENT)

      else:
        rollback()  // JSX ではない

    else:
      rollback()  // JSX ではない
```

#### parseJsxChildren

JSX の子要素を再帰的にパースする:
- `TAG_LT` → `tryParseJsx` (ネストした JSX)
- `TAG_LT_SLASH` → 子要素パース終了 (閉じタグの開始)
- `LBRACE` → `skipBalanced(LBRACE, RBRACE)` (式コンテナ `{expr}`)
- その他 → テキストとしてスキップ

#### skipJsxAttributes

開始タグ内の属性をスキップする:
- `TAG_GT` または `TAG_AUTO_CLOSE` まで単純にトークンを消費
- 属性の個別パースは行わない

#### expectClosingTag

閉じタグを消費する:
- `TAG_LT_SLASH` → タグ名 → `TAG_GT` のシーケンスを期待
- 見つからない場合はマーカーを `done` して続行 (エラー耐性)

### 統合ポイント

1. **`skipToEndOfDeclaration` 内**: `TAG_LT` を検出した際に `tryParseJsx` を呼び出し、成功すれば JSX ノードが作成される。失敗すれば従来通りスキップ。

2. **`parseTopLevel` の `else` ブランチ**: トップレベルに直接 JSX が出現した場合 (`TAG_LT` で始まる) にも `tryParseJsx` を呼び出す。

3. **`TAG_LT_SLASH` の処理**: `skipToEndOfDeclaration` で `TAG_LT_SLASH` が braceDepth==0 の状態で出現した場合、JSX 閉じタグの開始なので、宣言終了の境界として扱わずスキップを継続する。

## コード折りたたみの拡張

### RescriptFoldingBuilder の変更

折りたたみ対象に `JSX_ELEMENT` と `JSX_FRAGMENT` を追加:

```kotlin
if (node.elementType in setOf(
    RescriptElementTypes.JSX_ELEMENT,
    RescriptElementTypes.JSX_FRAGMENT,
)) {
    val startLine = document.getLineNumber(node.startOffset)
    val endLine = document.getLineNumber(node.startOffset + node.textLength)
    if (endLine > startLine) {
        descriptors += FoldingDescriptor(node, node.textRange)
    }
}
```

プレースホルダーテキスト:
- `JSX_ELEMENT` → `<tag>...</tag>` (先頭タグ名を取得)
- `JSX_FRAGMENT` → `<>...</>`

## 変更対象ファイル

| ファイル | 変更内容 |
|---------|---------|
| `lang/psi/RescriptPsi.kt` | JSX_ELEMENT, JSX_SELF_CLOSING_ELEMENT, JSX_FRAGMENT を追加 |
| `lang/RescriptParser.kt` | tryParseJsx, parseJsxChildren, skipJsxAttributes, expectClosingTag を追加。skipToEndOfDeclaration に JSX 検出ロジック追加 |
| `folding/RescriptFoldingBuilder.kt` | JSX_ELEMENT, JSX_FRAGMENT の折りたたみ対応 |
| `lang/RescriptParserTest.kt` | JSX パースのテストケース追加 |

## リスクと対策

### R1: 既存パーサーテストの破壊
- **対策**: JSX パースは TAG_LT トークンがある場合のみ発動。既存テストの多くは JSX を含まないため影響なし。`testJsxComponentNoErrors` は JSX を含むが、新しいパースが成功すればエラーなしの結果は変わらない。

### R2: 無限ループのリスク
- **対策**: parseJsxChildren で EOF チェックを必ず行う。TAG_LT_SLASH が見つからない場合は一定のトークン数でタイムアウトしてロールバック。

### R3: パフォーマンス劣化
- **対策**: JSX パースは TAG_LT トークンの出現時のみ実行。大半のトークンは従来通りスキップされる。rollback はマーカーの破棄のみでコストが低い。
