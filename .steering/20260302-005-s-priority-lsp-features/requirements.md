# Requirements: S-Priority LSP Features (#110, #111)

## 概要

S優先度の2機能を実装する:
1. **#110 Restart LSP Action** — Tools メニューから LSP サーバーを再起動するアクション
2. **#111 LSP Initialization Options** — VSCode 拡張が送信している6つの初期化オプションを追加

## 受け入れ条件

### #110 Restart LSP Action
- [ ] Tools メニューに「Restart ReScript Language Server」アクションが表示される
- [ ] アクション実行で LSP サーバーが再起動される
- [ ] プロジェクトが開いていない場合はアクションが無効化される

### #111 LSP Initialization Options
- [ ] 以下の6設定が Settings > ReScript に追加される:
  - Signature Help enabled (default: true)
  - Signature Help for constructor payloads (default: true)
  - Cache project config (default: true)
  - Inlay Hints enabled (default: false)
  - Inlay Hints max length (default: 25)
  - Compile status enabled (default: true)
- [ ] 設定値が LSP サーバーの初期化オプションとして送信される
- [ ] 設定変更後に Apply すると LSP サーバーが再起動される（既存動作）
