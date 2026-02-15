# Requirements: パンくずナビゲーション

## 概要

ReScript ファイル編集時に、エディタ上部（またはエディタ下部）にパンくずナビゲーション（Breadcrumbs）を表示する。カーソル位置に基づき、現在のコンテキスト（ファイル > モジュール > 宣言）を階層的に表示する。

## ユーザーストーリー

- ReScript 開発者として、エディタ内でカーソルがどの宣言・モジュールに属しているかを視覚的に確認したい
- ReScript 開発者として、パンくずをクリックして該当宣言にジャンプしたい
- ReScript 開発者として、Sticky Lines 機能でもコンテキスト情報を活用したい

## 受け入れ条件

1. `.res` / `.resi` ファイルでパンくずナビゲーションが表示される
2. 以下の PSI 要素がパンくず項目として認識される:
   - `MODULE_DECLARATION` — モジュール名を表示
   - `LET_DECLARATION` — let バインディング名を表示
   - `TYPE_DECLARATION` — 型名を表示
   - `EXTERNAL_DECLARATION` — external 名を表示
   - `EXCEPTION_DECLARATION` — 例外名を表示
3. ネストされたモジュール内の宣言で、正しい階層が表示される（例: `File > Module > let`）
4. パンくず項目にアイコンが表示される（ストラクチャービューと同じアイコン）
5. パンくず項目にツールチップが表示される（宣言の種類を表示）
6. `OPEN_STATEMENT`、`INCLUDE_STATEMENT`、`ANNOTATION` はパンくず対象外
7. IDE の設定でパンくずの表示/非表示を切り替えられる（IntelliJ Platform 標準動作）
8. ビルドが通ること

## 制約事項

- 既存の軽量パーサー（トップレベル宣言のみ認識）の範囲内で実装する
- パーサーの変更は行わない
- IntelliJ Platform の `BreadcrumbsProvider` API を使用する
