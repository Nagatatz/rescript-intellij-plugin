# Tasklist: %raw() JavaScript ハイライト

## 実装タスク

- [x] `RescriptRawJsInjector.kt` を作成
- [x] `rescript-js-injection.xml` を作成
- [x] `plugin.xml` に optional dependency を追加
- [x] ビルド確認 (`./gradlew buildPlugin`)
- [x] ドキュメント更新 (CLAUDE.md, product-requirements.md, functional-design.md)
- [x] tasklist.md を完了状態に更新してコミット

## テスト省略理由

言語インジェクションのテストは JavaScript プラグインとの結合が必要で、単体テストとして実装が困難。IntelliJ Platform のテストフレームワークで JavaScript プラグインをロードする環境構築が必要になるため、テスト作成を省略する。
