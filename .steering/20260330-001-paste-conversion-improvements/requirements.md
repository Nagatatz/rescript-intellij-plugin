# 要求定義: ペースト変換機能の改善

## 背景

現在の `RescriptPasteAsRescriptProcessor` は基本的な JavaScript パターン（`const`, `function`, `===` 等）のみを変換する。TypeScript/TSX 固有の構文（型注釈、インターフェース、ジェネリクス等）は変換されず、ペースト後に手動で除去する必要がある。

また `RescriptPasteAsJsxProcessor` は HTML → ReScript JSX の変換を行うが、React JSX/TSX 特有のパターン（`{...}` 式、`className` が既に使われている React JSX 等）との区別ができていない。

## 目的

TypeScript/TSX コードを ReScript ファイルにペーストした際に、TypeScript 固有の構文を適切に除去・変換し、より使いやすい ReScript コードを生成する。

## 機能要件

### FR-1: TypeScript 型注釈の除去

TypeScript の型注釈をペースト時に自動除去する。

| TypeScript | 変換後 |
|---|---|
| `const x: string = "hello"` | `let x = "hello"` |
| `function foo(a: number, b: string): boolean {` | `let foo = (a, b) => {` |
| `const arr: Array<number> = [1, 2, 3]` | `let arr = [1, 2, 3]` |
| `(x: number) => x + 1` | `(x) => x + 1` |
| `let result = value as string` | `let result = value` |
| `let result = <string>value` | `let result = value` |

### FR-2: TypeScript 固有宣言の変換

| TypeScript | 変換後 |
|---|---|
| `interface Props { name: string }` | `// interface Props { name: string }` (コメントアウト) |
| `type Props = { name: string }` | `type props = { name: string }` (ReScript type に変換、ただし完全な変換は困難なので best-effort) |
| `enum Color { Red, Green, Blue }` | `// enum Color { Red, Green, Blue }` (コメントアウト) |

### FR-3: JSX/TSX パターンの変換

React JSX/TSX 固有のパターンを ReScript JSX に変換する。

| JSX/TSX | ReScript |
|---|---|
| `{variable}` (JSX 式) | `{variable}` (そのまま) |
| `{condition && <div />}` | `{condition ? <div /> : React.null}` |
| `{items.map(item => <li key={item.id}>{item.name}</li>)}` | `{items->Array.map(item => <li key={item.id}>{item.name}</li>)}` |
| `<Component {...props} />` | `// spread は ReScript JSX で非サポート — 手動変換が必要` (コメント付加) |
| `<>{children}</>` | `<>{children}</>` (そのまま — ReScript でも同じ) |
| `className={styles.container}` | `className={styles["container"]}` (ドットアクセスは ReScript 辞書アクセスに) |

### FR-4: 検出ロジックの拡張

TypeScript/TSX コードを正しく検出するため、`looksLikeJavaScript` を拡張する。

追加検出パターン:
- 型注釈パターン: `: string`, `: number`, `: boolean`, `: React.FC<`
- TypeScript キーワード: `interface `, `type ` (行頭), `enum `, `as ` (型アサーション)
- JSX パターン: `<Component`, `<div className=`, `<>`, `</>`
- TSX 固有: `React.FC<`, `React.useState<`

### FR-5: JSX 検出の改善

`RescriptPasteAsJsxProcessor` が React JSX を HTML と誤判定しないよう改善する。

- `className=` が既に使われている → HTML ではなく JSX（変換不要、JS→ReScript 変換に委譲）
- `{...}` 式が含まれる → JSX（HTML ではない）
- `onClick={handler}` のように camelCase イベントハンドラ → 既に JSX

## 非機能要件

- 変換は best-effort とし、完全に正しい ReScript コードの生成を保証しない
- 変換後のコードにコンパイルエラーが残る場合は、コメントでヒントを付加する
- 既存の JS→ReScript 変換、HTML→JSX 変換との互換性を維持する
- パフォーマンスに影響を与えない（正規表現ベースの行単位変換を維持）

## 受け入れ条件

- [ ] TypeScript の型注釈（変数、パラメータ、戻り値、型アサーション）がペースト時に除去される
- [ ] TypeScript 固有宣言（interface, enum）がコメントアウトされる
- [ ] JSX/TSX の `&&` 条件レンダリングが三項演算子に変換される
- [ ] `.map()` が `->Array.map()` に変換される
- [ ] 検出ロジックが TypeScript/TSX コードを認識する
- [ ] JSX プロセッサが React JSX を HTML と誤判定しない
- [ ] 既存のテストがすべてパスする
- [ ] 新しい変換ルールに対するテストが追加されている
