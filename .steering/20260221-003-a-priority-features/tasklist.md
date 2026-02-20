# タスクリスト: A 優先度機能一括実装

## 実装タスク

- [x] 1. Completion Confidence（補完ポップアップ制御）
  - [x] 1-1. `RescriptCompletionConfidence.kt` を実装
  - [x] 1-2. `plugin.xml` に登録
  - [x] 1-3. `RescriptCompletionConfidenceTest.kt` を作成

- [x] 2. Live Template Context（テンプレートコンテキスト）
  - [x] 2-1. `RescriptTemplateContextType.kt` を実装
  - [x] 2-2. `plugin.xml` に登録
  - [x] 2-3. `ReScript.xml` の全テンプレートのコンテキストを `OTHER` → `RESCRIPT` に更新
  - [x] 2-4. `RescriptTemplateContextTypeTest.kt` を作成

- [x] 3. Live Template Macros（テンプレートマクロ）
  - [x] 3-1. `RescriptModuleNameMacro` と `RescriptComponentNameMacro` を実装
  - [x] 3-2. `plugin.xml` に登録
  - [x] 3-3. `RescriptLiveTemplateMacrosTest.kt` を作成

- [x] 4. Additional Snippets（スニペット追加）
  - [x] 4-1. `ReScript.xml` に新 Live Templates 追加（`@module`, `@val`, `@send`, `@get`, `@set`, `comp`）
  - [x] 4-2. `RescriptPostfixTemplateProvider.kt` に `PromisePostfixTemplate`, `AwaitPostfixTemplate` を追加
  - [x] 4-3. `RescriptPostfixTemplateProviderTest.kt` を作成

- [x] 5. Problem Highlight Filter（ハイライト抑制）
  - [x] 5-1. `RescriptProblemHighlightFilter.kt` を実装
  - [x] 5-2. `plugin.xml` に登録
  - [x] 5-3. `RescriptProblemHighlightFilterTest.kt` を作成

- [x] 6. Enter Handler（ドキュメントコメント継続）
  - [x] 6-1. `RescriptEnterHandler.kt` を実装
  - [x] 6-2. `plugin.xml` に登録
  - [x] 6-3. `RescriptEnterHandlerTest.kt` を作成

- [x] 7. Join Lines（スマート行結合）
  - [x] 7-1. `RescriptJoinLinesHandler.kt` を実装
  - [x] 7-2. `plugin.xml` に登録
  - [x] 7-3. `RescriptJoinLinesHandlerTest.kt` を作成

- [x] 8. Extend/Shrink Word Selection（選択拡大/縮小）
  - [x] 8-1. `RescriptWordSelectionHandler.kt` を実装（文字列・括弧・コメントの3ハンドラ）
  - [x] 8-2. `plugin.xml` に登録
  - [x] 8-3. `RescriptWordSelectionHandlerTest.kt` を作成

- [x] 9. Highlight Usages（キーワードハイライト）
  - [x] 9-1. `RescriptHighlightUsagesHandlerFactory.kt` を実装
  - [x] 9-2. `plugin.xml` に登録
  - [x] 9-3. `RescriptHighlightUsagesHandlerFactoryTest.kt` を作成

- [x] 10. Goto Super（.res → .resi ジャンプ）
  - [x] 10-1. `RescriptGotoSuperHandler.kt` を実装
  - [x] 10-2. `plugin.xml` に登録
  - [x] 10-3. `RescriptGotoSuperHandlerTest.kt` を作成

- [x] 11. External Documentation（外部ドキュメント）
  - [x] 11-1. `RescriptDocumentationProvider.kt` を実装（URL マッピング含む）
  - [x] 11-2. `plugin.xml` に登録
  - [x] 11-3. `RescriptDocumentationProviderTest.kt` を作成

- [x] 12. Run Anything Provider（コマンド実行）
  - [x] 12-1. `RescriptRunAnythingProvider.kt` を実装
  - [x] 12-2. `plugin.xml` に登録
  - [x] 12-3. `RescriptRunAnythingProviderTest.kt` を作成

- [x] 13. Expression Type Info（式の型表示）
  - [x] 13-1. `RescriptExpressionTypeProvider.kt` を実装
  - [x] 13-2. `plugin.xml` に登録
  - [x] 13-3. テスト省略（LSP 結合が必須のため単体テスト困難）

## ビルド・検証

- [x] 14. `./gradlew buildPlugin` でビルド成功を確認
- [x] 15. ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md, README.md, sphinx-docs）

## Git

- [x] 16. tasklist 更新 + コミット
- [x] 17. main にマージして worktree を削除
