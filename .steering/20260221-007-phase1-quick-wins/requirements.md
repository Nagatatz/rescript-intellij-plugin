# Requirements: Phase 1 — Quick Wins (8 features)

## 概要

調査で特定された未実装機能のうち、低コスト・即効性の高い8件を実装する。
すべて低〜低中難易度（★）で、パーサー変更不要。

## 対象機能

### 1. 未使用結果の `->ignore` 追加 (#71)
- **種類:** Intention Action (Alt+Enter)
- **概要:** 未使用式の末尾に `->ignore` を追加する Intention
- **トリガー:** カーソルが式の上にあり、式の結果が使用されていない（LSP 診断 or トップレベルの式）
- **受け入れ条件:**
  - Alt+Enter で "Add ->ignore" が表示される
  - 選択すると `expr` → `expr->ignore` に変換される
  - テスト作成

### 2. 未使用変数の `_` プレフィックス (#91)
- **種類:** Intention Action (Alt+Enter)
- **概要:** 未使用変数に `_` プレフィックスを追加して警告を抑制する Intention
- **トリガー:** `let` 束縛の識別子にカーソルがある場合
- **受け入れ条件:**
  - Alt+Enter で "Add _ prefix to suppress unused warning" が表示される
  - 選択すると `let x = ...` → `let _x = ...` に変換される
  - テスト作成

### 3. 冗長ブロック削除 (#72)
- **種類:** Intention Action (Alt+Enter)
- **概要:** 単一式のみを含む `{ expr }` を `expr` に変換
- **トリガー:** ブレースブロックに単一の式のみ含まれる場合
- **受け入れ条件:**
  - Alt+Enter で "Remove redundant braces" が表示される
  - 選択すると `{ expr }` → `expr` に変換される
  - 複数式を含むブロックでは表示されない
  - テスト作成

### 4. デコレータ/属性補完 (#90)
- **種類:** Completion Contributor
- **概要:** `@` 入力時に ReScript デコレータ (@genType, @module, @val, @scope 等) を補完
- **トリガー:** `@` 文字の後
- **受け入れ条件:**
  - `@` 入力後に補完候補リストに ReScript デコレータが表示される
  - 各デコレータの説明テキストが付与されている
  - テスト作成

### 5. 演算子優先順位ホバー表示 (#92)
- **種類:** Documentation Provider 拡張
- **概要:** `->`, `++`, `===` 等の演算子にホバーすると優先順位情報を表示
- **トリガー:** 演算子トークン上でホバー (Ctrl+Q)
- **受け入れ条件:**
  - 演算子上で Ctrl+Q すると優先順位と説明が表示される
  - テスト作成

### 6. Long Line Inspection Policy (#80)
- **種類:** Inspection Suppressor 拡張
- **概要:** `@module`, `%raw`, `%ffi` 内および長い文字列リテラル内の長行警告を抑制
- **トリガー:** 自動（インスペクション実行時）
- **受け入れ条件:**
  - `@module("very-long-path")` 等で長行警告が出ない
  - `%raw()` 内で長行警告が出ない
  - 通常コードの長行警告は維持
  - テスト作成

### 7. 識別子ケース修正 (#73)
- **種類:** Intention Action (Alt+Enter)
- **概要:** 識別子の命名規則違反を修正（モジュール名は PascalCase、変数名は camelCase）
- **トリガー:** 命名規則に違反する識別子にカーソルがある場合
- **受け入れ条件:**
  - `myModule` → `MyModule`（モジュール名を PascalCase に）
  - `MyVar` → `myVar`（変数名を camelCase に）
  - テスト作成

### 8. MultiLang Commenter (#79)
- **種類:** Commenter 拡張
- **概要:** `%raw()` ブロック内でコメント切り替え時に JavaScript コメント (`//`, `/* */`) を使用
- **トリガー:** `%raw()` 内でコメント切り替え (Cmd+/)
- **受け入れ条件:**
  - `%raw()` 外では ReScript コメント (`//`, `/* */`) が使用される
  - `%raw()` 内では JavaScript コメント (`//`, `/* */`) が使用される
  - テスト作成

## 制約事項

- パーサー (`RescriptParser.kt`) やレクサー (`Rescript.flex`) の変更は最小限にとどめる
- 既存の extension point 登録パターンに従う
- IntelliJ Platform 2025.3+ API を使用
