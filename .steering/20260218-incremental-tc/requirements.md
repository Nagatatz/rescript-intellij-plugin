# Requirements: Incremental Type Checking 設定

## 概要
LSP の Incremental Type Checking を Settings UI からトグル可能にする。

## 機能要件
- Settings > Languages & Frameworks > ReScript にチェックボックス追加
- デフォルト: 有効
- 変更時に LSP サーバーを自動再起動
- LSP `initializationOptions` に `incrementalTypechecking.enabled` を送信

## テスト省略理由
- RescriptConfigurable は Swing UI コンポーネントのため単体テスト困難
- RescriptProjectSettings.State のデフォルト値テストは作成可能
