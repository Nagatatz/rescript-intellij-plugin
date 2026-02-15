# Tasklist: セマンティックハイライティング

## タスク

- [x] 1. `RescriptSemanticTokensSupport.kt` を新規作成する
  - `com.rescript.plugin.lsp` パッケージに配置
  - `LspSemanticTokensSupport()` を継承
  - `shouldAskServerForSemanticTokens()` を `true` でオーバーライド
  - `getTextAttributesKey()` で8トークンタイプを `SEMANTIC_*` キーにマッピング
- [x] 2. `RescriptLspServerDescriptor.kt` に `lspCustomization` プロパティを追加する
  - `LspCustomization` をオーバーライドし、`semanticTokensCustomizer` に `RescriptSemanticTokensSupport()` を設定
- [x] 3. `./gradlew buildPlugin` でビルドが通ることを確認する
- [x] 4. `docs/functional-design.md` の LSP 統合セクションにセマンティックトークン対応の記述を追加する
- [x] 5. コミットする（`✨ Add LSP semantic token highlighting support`）
