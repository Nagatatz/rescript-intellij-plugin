# 要求定義: S 優先度機能一括実装

## 概要

他の JetBrains 言語プラグインとの機能ギャップ調査（`.steering/20260221-001-feature-research/research.md`）で特定された **S 優先度（最優先）** の 8 機能を一括実装する。いずれもパーサー変更不要で、高インパクトかつ実装容易な機能。

## 実装対象

### 1. Unwrap/Remove（編集操作）
- **概要:** Ctrl+Shift+Delete で囲む構造（`Some()`, `Ok()`, `Error()`, `if`, `switch`, `try`, `{}`）を除去
- **受け入れ条件:**
  - `Some(expr)` → `expr` に変換
  - `Ok(expr)` → `expr` に変換
  - `Error(expr)` → `expr` に変換
  - `if (cond) { body }` → `body` に変換
  - `switch expr { | _ => body }` → `body` に変換
  - `try { body } catch { ... }` → `body` に変換
  - `{ body }` → `body` に変換

### 2. Go to Test / Create Test（ナビゲーション）
- **概要:** Ctrl+Shift+T で実装ファイルとテストファイル間を移動。テスト未存在なら自動生成
- **受け入れ条件:**
  - `Foo.res` → `Foo_test.res` / `__tests__/Foo_test.res` / `Foo.test.res` を探索してジャンプ
  - テストファイルから実装ファイルへの逆方向ジャンプも対応
  - テスト未存在時はダイアログでテストファイル生成を提案
  - テストフレームワーク（jest/vitest）に応じたボイラープレートを挿入

### 3. Tree Structure Provider — .resi ファイルのネスト（プロジェクトビュー）
- **概要:** プロジェクトビューで `.resi` ファイルを対応する `.res` ファイルの下にネスト表示
- **受け入れ条件:**
  - `Foo.resi` が `Foo.res` の子として表示される
  - `.res` が存在しない `.resi` は通常通り表示
  - ネスト表示の有効/無効をプロジェクト設定で切り替え可能

### 4. Typed Handler — JSX 閉じタグ自動挿入（編集操作）
- **概要:** JSX の開始タグ完了時に閉じタグを自動挿入
- **受け入れ条件:**
  - `<div>` と入力（`>` 入力時）→ `</div>` が自動挿入されカーソルはタグ間に
  - `<Component>` → `</Component>` 自動挿入
  - `<Module.Component>` → `</Module.Component>` 自動挿入
  - 自己閉じタグ `<br />` では挿入しない
  - 文字列リテラル内、コメント内では動作しない

### 5. Bundled Dictionary（スペルチェック）
- **概要:** ReScript 固有用語の辞書をバンドルし、スペルチェック誤検出を防止
- **受け入れ条件:**
  - `genType`, `uncurried`, `polyvariant`, `functor`, `rescript`, `Belt`, `Js` 等がスペルエラーにならない
  - ReScript の標準モジュール名、アノテーション名、一般的な用語をカバー

### 6. Context Info — Declaration Range Handler（ナビゲーション）
- **概要:** 長い宣言内でスクロール時、囲む宣言のヘッダーをエディタ上部にスティッキー表示
- **受け入れ条件:**
  - `module` ブロック内でスクロール → モジュール宣言ヘッダーが上部に表示
  - `let` 束縛内でスクロール → let 宣言ヘッダーが表示
  - `type` 宣言内でも同様に動作

### 7. Test Source Filter（プロジェクトビュー）
- **概要:** テストファイル/ディレクトリを IDE がテストソースとして認識
- **受け入れ条件:**
  - `*_test.res`, `*.test.res` がテストファイルとして認識
  - `__tests__/` ディレクトリ配下がテストソースとして認識
  - テストファイルにテスト用アイコンデコレーションが表示

### 8. FindUsagesProvider + WordsScanner（Find Usages）
- **概要:** Find Usages の結果にシンボル種類を表示し、ワードスキャンを改善
- **受け入れ条件:**
  - Find Usages 結果に「function 'foo'」「module 'Bar'」「type 'user'」等のシンボル種類が表示
  - 識別子・コメント・文字列の分類が正しく行われる
  - コメント内の単語が Find Usages で適切に除外される

## 制約

- パーサー（`RescriptParser.kt`, `Rescript.flex`）の変更は行わない
- 既存の PSI 構造とトークンタイプを活用する
- 各機能は `plugin.xml` に Extension Point として登録する
