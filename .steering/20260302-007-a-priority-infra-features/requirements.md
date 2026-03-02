# 要求定義書: A 優先度機能 (#112, #113, #114)

## 概要

ロードマップの A 優先度に分類される3機能を実装する。

- **#112 ビルド自動開始プロンプト** — プロジェクト起動時に `rescript build -w` の開始を提案するバルーン通知
- **#113 Dump LSP State** — LSP サーバーの内部状態をデバッグ出力するアクション
- **#114 offset↔position 変換共通化** — 18+ ファイルに重複する変換ロジックをユーティリティに集約

## 機能要件

### #112 ビルド自動開始プロンプト

- ReScript プロジェクトを開いた際に、`rescript build -w`（ウォッチモードビルド）の開始を提案するバルーン通知を表示する
- 通知には以下のアクションを含む:
  - **Start Build Watch** — `rescript build -w` を実行構成として起動
  - **Don't show again** — 以後このプロジェクトで通知を表示しない
- 以下の条件をすべて満たす場合のみ通知を表示する:
  - ReScript プロジェクトである（`rescript.json` または `bsconfig.json` が存在する）
  - ReScript CLI が利用可能である（`node_modules/.bin/rescript` が存在する）
  - ユーザーが「Don't show again」を選択していない
  - `rescript build -w` がまだ実行中でない

### #113 Dump LSP State

- Tools メニューから「Dump ReScript LSP State」アクションを実行できる
- LSP サーバーに対して状態ダンプリクエストを送信し、結果をダイアログで表示する
- LSP サーバーが起動していない場合は、その旨のメッセージを表示する
- リクエストがタイムアウトまたは失敗した場合は、エラーメッセージを表示する

### #114 offset↔position 変換共通化

- `RescriptOffsetUtils` ユーティリティオブジェクトを作成する
- 以下の変換関数を提供する:
  - `offsetToPosition(document, offset)` → LSP `Position`
  - `positionToOffset(document, position)` → `Int`
- 既存コード中の重複変換ロジックをユーティリティ呼び出しに置換する

## 受け入れ条件

- [ ] #112: プロジェクト起動時にバルーン通知が表示され、Start Build Watch で `rescript build -w` が起動する
- [ ] #112: Don't show again で以後通知が表示されない
- [ ] #113: Tools > Dump ReScript LSP State で LSP 状態が表示される
- [ ] #113: LSP 未起動時に適切なメッセージが表示される
- [ ] #114: `RescriptOffsetUtils` が作成され、重複コードが置換されている
- [ ] #114: 既存テストが全てパスする（動作に変更なし）
- [ ] `./gradlew clean buildPlugin` が成功する
