# Requirements: Editor Notification Bar

## 概要

`@rescript/language-server` が未インストールの場合、`.res` / `.resi` ファイル編集時にエディタ上部に案内バーを表示する機能。

## ユーザーストーリー

ReScript 開発者として、LSP サーバーが見つからない場合にエディタ上部に案内バーが表示されることで、セットアップ方法を把握したい。

## 受け入れ条件

- [ ] LSP サーバー未検出時、`.res` / `.resi` ファイルのエディタ上部に警告バーが表示される
- [ ] "Configure..." ボタンクリックで Settings > Languages & Frameworks > ReScript を開く
- [ ] "Dismiss" ボタンクリックでバーを閉じ、以後再表示しない
- [ ] LSP サーバーが利用可能な場合はバーを表示しない
- [ ] `RescriptProjectSettings` の `lspServerPath` が設定済みの場合はバーを表示しない
- [ ] dismiss フラグはプロジェクト単位で管理される

## 制約事項

- テスト省略: LSP サーバー検出との結合、EditorNotificationPanel の UI 表示テストが単体テスト困難
