# 設計書: ペースト変換機能の改善

## 変更対象ファイル

| ファイル | 変更内容 |
|---------|---------|
| `paste/RescriptPasteAsRescriptProcessor.kt` | TypeScript 型注釈除去、TS 固有宣言変換、JSX パターン変換、検出ロジック拡張 |
| `paste/RescriptPasteAsJsxProcessor.kt` | JSX 誤判定防止ロジック追加 |
| `paste/RescriptPasteAsRescriptProcessorTest.kt` | TypeScript/JSX 変換のテスト追加 |
| `paste/RescriptPasteAsJsxProcessorTest.kt` | JSX 誤判定テスト追加 |

新規ファイルの追加は不要。既存の2ファイルの拡張で対応する。

## 設計方針

### 行単位の正規表現変換を維持

現在の `convertLine()` メソッドの行単位変換アーキテクチャをそのまま維持する。TypeScript の型注釈除去も正規表現ベースで行う。AST パースは行わない（best-effort アプローチ）。

### 変換パイプラインの順序

`convertLine()` 内の変換は以下の順序で実行する（順序が重要）:

1. **TypeScript 型注釈の除去**（先に型情報を消す）
2. **既存の JS→ReScript 変換**（`const`→`let`, `function`→arrow 等）
3. **JSX パターンの変換**（`&&` → 三項、`.map()` → `->Array.map()` 等）

型注釈を先に除去することで、後続の `const`/`function` 変換が正しく動作する。

## 詳細設計

### 1. TypeScript 型注釈除去 (FR-1)

追加する正規表現パターン:

```kotlin
// 変数の型注釈: `const x: Type = ...` → `const x = ...`
// const/let/var の後の `: Type` を除去（`=` の前まで）
private val VAR_TYPE_ANNOTATION = Regex("""((?:const|let|var)\s+\w+)\s*:\s*[^=]+(\s*=)""")

// 関数パラメータの型注釈: `(a: number, b: string)` → `(a, b)`
private val PARAM_TYPE_ANNOTATION = Regex("""(\w+)\s*:\s*[\w<>\[\]|&.?]+""")

// 関数の戻り値型注釈: `): ReturnType {` → `) {`
private val RETURN_TYPE_ANNOTATION = Regex("""\)\s*:\s*[\w<>\[\]|&.?]+(\s*(?:\{|=>))""")

// 型アサーション (as): `value as string` → `value`
private val AS_ASSERTION = Regex("""\s+as\s+[\w<>\[\]|&.?]+""")

// アングルブラケット型アサーション: `<string>value` → `value`（JSX と区別が必要）
// → JSX との衝突リスクが高いため、対応しない
```

**注意:** ジェネリクス型 `Array<number>` 等のネストした `<>` は完全にパースしない。最外のマッチで best-effort 除去する。

### 2. TypeScript 固有宣言の変換 (FR-2)

```kotlin
// interface 宣言 → コメントアウト
if (trimmed.startsWith("interface ")) {
    return "// $result"
}

// enum 宣言 → コメントアウト
if (trimmed.startsWith("enum ")) {
    return "// $result"
}

// type 宣言 → そのまま残す（ReScript にも type がある）
// ただし `type X = { ... }` の形式はある程度互換性があるため変換しない
```

### 3. JSX/TSX パターンの変換 (FR-3)

```kotlin
// `&&` 条件レンダリング → 三項演算子
// `{condition && <expr>}` → `{condition ? <expr> : React.null}`
private val JSX_AND_PATTERN = Regex("""\{([^}]+)\s*&&\s*([^}]+)\}""")

// `.map(` → `->Array.map(`
private val DOT_MAP_PATTERN = Regex("""\.map\(""")

// `.filter(` → `->Array.filter(`
private val DOT_FILTER_PATTERN = Regex("""\.filter\(""")

// `.forEach(` → `->Array.forEach(`
private val DOT_FOREACH_PATTERN = Regex("""\.forEach\(""")

// `...props` スプレッド検出 → 警告コメント付加
private val JSX_SPREAD_PATTERN = Regex("""\{\.\.\.(\w+)\}""")
```

**`.map()` 等の変換について:** 行単位で `obj.map(` を `obj->Array.map(` に変換する。`obj` が Array かどうかの型推論は行わない（best-effort）。

### 4. 検出ロジックの拡張 (FR-4)

`looksLikeJavaScript()` に追加する検出パターン:

```kotlin
// 既存パターンに追加
private val TS_KEYWORDS = listOf("interface ", "enum ")

// 行内の TypeScript パターン検出
private val TS_PATTERNS = listOf(
    Regex(""": (string|number|boolean|any|void|never|unknown)\b"""),  // 基本型注釈
    Regex("""React\.(FC|Component|useState|useEffect|useRef)\b"""),   // React 型
    Regex("""\bas\s+(string|number|boolean|any)\b"""),                // 型アサーション
)
```

`looksLikeJavaScript` を `looksLikeJavaScriptOrTypeScript` にリネームする（internal メソッドのため影響なし）。

### 5. JSX 誤判定防止 (FR-5)

`RescriptPasteAsJsxProcessor.looksLikeHtml()` に除外条件を追加:

```kotlin
internal fun looksLikeHtml(text: String): Boolean {
    val trimmed = text.trim()

    // 基本チェック（既存）
    if (!trimmed.contains("<") || !trimmed.contains(">")) return false
    if (!HTML_TAG_PATTERN.containsMatchIn(trimmed)) return false

    // JSX 除外: React JSX の特徴があれば HTML ではないと判定
    if (looksLikeJsx(trimmed)) return false

    return true
}

private fun looksLikeJsx(text: String): Boolean {
    // className= が既に使われている → JSX
    if (text.contains("className=")) return true
    // {式} が含まれる → JSX (HTML の属性値ではない)
    if (Regex("""\{[^}]+\}""").containsMatchIn(text)) return true
    // camelCase イベントハンドラ → JSX
    if (Regex("""on[A-Z]\w+=""").containsMatchIn(text)) return true
    return false
}
```

JSX と判定された場合、`RescriptPasteAsJsxProcessor` は何もせず、`RescriptPasteAsRescriptProcessor` 側で JSX パターン変換を処理する。

## 処理フローの競合回避

両プロセッサは `CopyPastePostProcessor` として登録されているため、同じペーストに対して両方が実行される可能性がある。

**現在の動作:** 両プロセッサは独立した `TextBlockTransferableData` サブクラス（`JsTransferData` / `HtmlTransferData`）を使うため、衝突しない。`extractTransferableData` でそれぞれの判定が通ったものだけが処理される。

**改善後の動作:**
- JSX/TSX コード → `looksLikeHtml` が false（JSX 除外）→ `looksLikeJavaScript` が true → JS プロセッサのみが処理
- 純粋な HTML → `looksLikeHtml` が true → HTML プロセッサのみが処理
- 純粋な JS/TS → `looksLikeHtml` が false → `looksLikeJavaScript` が true → JS プロセッサのみが処理

## テスト計画

### RescriptPasteAsRescriptProcessorTest に追加するテスト

| テスト | 入力 | 期待出力 |
|-------|------|---------|
| 変数型注釈除去 | `const x: string = "hello";` | `let x = "hello"` |
| パラメータ型注釈除去 | `function foo(a: number, b: string) {` | `let foo = (a, b) => {` |
| 戻り値型注釈除去 | `function foo(): boolean {` | `let foo = () => {` |
| 型アサーション除去 | `const x = value as string;` | `let x = value` |
| interface コメントアウト | `interface Props { name: string }` | `// interface Props { name: string }` |
| enum コメントアウト | `enum Color { Red }` | `// enum Color { Red }` |
| `&&` → 三項演算子 | `{visible && <div />}` | `{visible ? <div /> : React.null}` |
| `.map(` 変換 | `items.map(x =>` | `items->Array.map(x =>` |
| TS 検出 | `const x: string = "hello"` | `looksLikeJavaScript` が true |
| export type 除去 | `export type Props = {...}` | `// export type Props = {...}` |

### RescriptPasteAsJsxProcessorTest に追加するテスト

| テスト | 入力 | 期待結果 |
|-------|------|---------|
| JSX は HTML と判定しない | `<div className="foo">{bar}</div>` | `looksLikeHtml` が false |
| camelCase ハンドラは JSX | `<button onClick={handler}>` | `looksLikeHtml` が false |
| 純粋な HTML は引き続き検出 | `<div class="foo">bar</div>` | `looksLikeHtml` が true |
