# Requirements: Gutter Run Icons

## 概要

.res ファイルのエディタガターに▶実行アイコンを表示し、クリックで既存の `RescriptRunConfigurationType` によるビルド実行構成を起動する。

## 機能要件

- .res ファイルのガターに▶実行アイコンを表示する
- アイコンクリックで既存の `RescriptRunConfigurationType` によるビルド実行構成を起動する
- rescript.json が存在するプロジェクトでのみ表示する
- 右クリックメニューに Run/Debug オプションを表示する
- ファイル内最初のトップレベル宣言（`LET_DECLARATION`, `TYPE_DECLARATION`, `MODULE_DECLARATION`）のキーワード要素にのみアイコンを表示する

## 非機能要件

- リーフ要素のみに反応し、重複アイコンを防止する
- LSP 不要（純粋な PSI ベースの実装）

## 受け入れ条件

- [x] .res ファイルの最初のトップレベル宣言にガター実行アイコンが表示される
- [x] rescript.json が存在しないプロジェクトではアイコンが表示されない
- [x] .resi ファイルではアイコンが表示されない
- [x] ビルドが成功する
- [x] テストが通る
