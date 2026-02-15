# タスクリスト: リネームリファクタリング

| 項目 | 内容 |
|---|---|
| 機能名 | リネームリファクタリング |
| 作成日 | 2026-02-16 |
| 進捗 | 12 / 12 完了 |

## フェーズ1: 準備

- [x] `lsp4jServerClass` オーバーライドを `RescriptLspServerDescriptor.kt` に追加し、`LanguageServer` インタフェースをプロキシに公開する
- [x] 既存の LSP 機能（補完、定義ジャンプ、ホバー等）が壊れていないことを `./gradlew buildPlugin` で確認する

## フェーズ2: 実装

- [x] `RescriptNamesValidator.kt` を `refactor/` パッケージに新規作成する（`isIdentifier`, `isKeyword` の実装）
- [x] `RescriptRenameHandler.kt` を `refactor/` パッケージに新規作成する
  - [x] `isAvailableOnDataContext()`: ReScript ファイル判定 + LSP サーバー起動確認 + カーソル位置の識別子判定
  - [x] `invoke()`: `textDocument/prepareRename` 送信（サーバー未対応時はフォールバック）
  - [x] `invoke()`: リネームダイアログ表示と新しい名前の取得
  - [x] `invoke()`: `textDocument/rename` 送信と `WorkspaceEdit` の適用（`WriteCommandAction` 内で実行）
- [x] `plugin.xml` に `renameHandler` と `namesValidator` を登録する

## フェーズ3: テスト

- [x] `./gradlew buildPlugin` がエラーなしで完了することを確認する
- [x] `./gradlew test` が全件パスすることを確認する

## フェーズ4: 仕上げ

- [x] tasklist.md を最終更新し、コミットする（`✨ Add rename refactoring via LSP textDocument/rename`）

## 完了条件

- [ ] すべてのタスクが完了していること
- [ ] ビルドが成功すること (`./gradlew buildPlugin`)
- [ ] 受け入れ条件をすべて満たしていること

---

## 振り返り

<!-- モード3（/steering review）で記録する -->

### 実装で工夫した点

### 発生した問題と解決策

### 設計変更の理由

### 次回への改善点
