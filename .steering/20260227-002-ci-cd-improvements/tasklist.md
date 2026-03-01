# Tasklist: CI/CD パイプライン整備

## ドキュメント整備

- [x] CLAUDE.md に CI/CD セクションを追加
- [x] `sphinx-docs/dev/ci-cd.md` を新規作成
- [x] `sphinx-docs/dev/index.md` の toctree に `ci-cd` を追加

## インテグレーションテスト

- [x] `src/test/testData/` ディレクトリ構成を作成
- [x] testData ファイルを作成（highlighting, folding, structure, indent）
- [x] `RescriptHighlightingIntegrationTest` を実装
- [x] `RescriptFoldingIntegrationTest` を実装
- [x] `RescriptStructureViewIntegrationTest` を実装
- [x] `RescriptIndentIntegrationTest` を実装
- [x] `RescriptParserIntegrationTest` を実装
- [x] `RescriptLexerIntegrationTest` を実装

## 検証・コミット

- [x] `./gradlew clean buildPlugin` が成功する
- [x] 全テストがパスする
- [x] ドキュメント同期（CLAUDE.md, README.md, sphinx-docs, product-requirements.md）
- [x] 機能単位でコミット
- [x] main にマージ
