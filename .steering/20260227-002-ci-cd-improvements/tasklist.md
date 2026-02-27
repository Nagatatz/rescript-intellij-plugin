# Tasklist: CI/CD パイプライン整備

## ドキュメント整備

- [ ] CLAUDE.md に CI/CD セクションを追加
- [ ] `sphinx-docs/dev/ci-cd.md` を新規作成
- [ ] `sphinx-docs/dev/index.md` の toctree に `ci-cd` を追加

## インテグレーションテスト

- [ ] `src/test/testData/` ディレクトリ構成を作成
- [ ] testData ファイルを作成（highlighting, folding, structure, indent）
- [ ] `RescriptHighlightingIntegrationTest` を実装
- [ ] `RescriptFoldingIntegrationTest` を実装
- [ ] `RescriptStructureViewIntegrationTest` を実装
- [ ] `RescriptIndentIntegrationTest` を実装
- [ ] `RescriptParserIntegrationTest` を実装
- [ ] `RescriptLexerIntegrationTest` を実装

## 検証・コミット

- [ ] `./gradlew clean buildPlugin` が成功する
- [ ] 全テストがパスする
- [ ] ドキュメント同期（CLAUDE.md, README.md, sphinx-docs, product-requirements.md）
- [ ] 機能単位でコミット
- [ ] main にマージ
