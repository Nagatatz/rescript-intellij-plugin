# タスクリスト: ペースト変換機能の改善

## 1. TypeScript 型注釈除去・固有宣言変換 (FR-1, FR-2)

- [x] 1-1. `RescriptPasteAsRescriptProcessor` に型注釈除去の正規表現パターンを追加
  - 変数型注釈: `const x: Type = ...` → `const x = ...`
  - パラメータ型注釈: `(a: number, b: string)` → `(a, b)`
  - 戻り値型注釈: `): ReturnType {` → `) {`
  - 型アサーション: `value as string` → `value`
- [x] 1-2. TypeScript 固有宣言の変換を追加
  - `interface` → コメントアウト
  - `enum` → コメントアウト
  - `export type` / `export interface` / `export enum` → コメントアウト
- [x] 1-3. `convertLine()` の変換パイプライン順序を調整（型注釈除去を最初に実行）
- [x] 1-4. テスト追加（`RescriptPasteAsRescriptProcessorTest`）
  - 変数型注釈除去、パラメータ型注釈除去、戻り値型注釈除去、型アサーション除去
  - interface/enum コメントアウト、export type コメントアウト

## 2. JSX/TSX パターンの変換 (FR-3)

- [x] 2-1. `&&` 条件レンダリングの三項演算子変換を追加
- [x] 2-2. `.map()` / `.filter()` / `.forEach()` の `->Array.*` 変換を追加
- [x] 2-3. スプレッド `{...props}` に警告コメントを付加
- [x] 2-4. テスト追加
  - `&&` → 三項演算子、`.map(` 変換、スプレッド警告

## 3. 検出ロジックの拡張 (FR-4)

- [x] 3-1. `looksLikeJavaScript` に TypeScript パターン検出を追加
  - 基本型注釈（`: string`, `: number` 等）
  - TypeScript キーワード（`interface`, `enum`）
  - React 型パターン（`React.FC<`, `React.useState<` 等）
- [x] 3-2. テスト追加
  - TypeScript コードの検出、JSX/TSX コードの検出

## 4. JSX 誤判定防止 (FR-5)

- [x] 4-1. `RescriptPasteAsJsxProcessor.looksLikeHtml()` に JSX 除外ロジックを追加
  - `className=` 検出 → JSX と判定
  - `{式}` 検出 → JSX と判定
  - camelCase イベントハンドラ検出 → JSX と判定
- [x] 4-2. テスト追加（`RescriptPasteAsJsxProcessorTest`）
  - JSX が HTML と誤判定されないことを確認
  - 純粋な HTML は引き続き検出されることを確認

## 5. コミット前検証

- [x] 5-1. `./gradlew ktlintCheck` が成功する
- [x] 5-2. `./gradlew clean buildPlugin` が成功する
- [x] 5-3. `./gradlew test` が成功する（既存テスト + 新規テスト）
- [x] 5-4. KDoc コメントが全クラス・メソッドに付与されている

## 6. ドキュメント更新

- [x] 6-1. `CLAUDE.md` — JS→ReScript 変換の説明に TypeScript/JSX 対応を追記
- [x] 6-2. `README.md` — Features セクションに TypeScript/JSX ペースト変換を追記
- [x] 6-3. `sphinx-docs/user/features/` — 該当ページに TypeScript/JSX 変換の説明を追加
- [x] 6-4. `docs/product-requirements.md` — 実装済み機能の説明を更新

## 7. コミット・マージ

- [x] 7-1. 機能コミット: `✨ Add TypeScript/JSX paste conversion support`
- [x] 7-2. tasklist.md の全タスクを `[x]` に更新
- [x] 7-3. ユーザーにマージ可否を確認
- [x] 7-4. main にマージ、ブランチ削除
