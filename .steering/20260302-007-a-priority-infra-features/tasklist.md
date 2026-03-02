# タスクリスト: A 優先度機能 (#112, #113, #114)

## #112 ビルド自動開始プロンプト

- [x] `run/RescriptBuildWatchStartupActivity.kt` を作成
- [x] `plugin.xml` に `postStartupActivity` を登録
- [x] テスト免除（IDE ライフサイクル依存の ProjectActivity）
- [x] コミット: `✨ Add build watch auto-start prompt at project open`

## #113 Dump LSP State

- [x] `lsp/RescriptDumpLspStateAction.kt` を作成
- [x] `plugin.xml` に action を登録（ToolsMenu）
- [x] テスト免除（LSP 結合必須の AnAction）
- [x] コミット: `✨ Add Dump LSP State action`

## #114 offset↔position 変換共通化

- [x] `util/RescriptOffsetUtils.kt` を作成
- [x] `util/RescriptOffsetUtilsTest.kt` を作成
- [x] 以下のファイルをリファクタリング（ユーティリティ呼び出しに置換）:
  - [x] `lsp/RescriptLspUtils.kt`
  - [x] `lsp/RescriptExpressionTypeProvider.kt`
  - [x] `refactor/RescriptRenameHandler.kt`
  - [x] 調査の結果、以下はインライン offset→Position 変換パターンを含まないため変更不要:
    - `refactor/RescriptExtractVariableHandler.kt`
    - `refactor/RescriptExtractFunctionHandler.kt`
    - `refactor/RescriptExtractComponentHandler.kt`
    - `refactor/RescriptInlineHandler.kt`
    - `intention/RescriptFilterMapChainIntention.kt`
    - `intention/RescriptExpandDestructuringIntention.kt`
    - `intention/RescriptAddTypeAnnotationIntention.kt`
    - `intention/RescriptCaseSplitIntention.kt`
    - `editor/RescriptEnterHandler.kt`
    - `editor/RescriptSmartEnterProcessor.kt`
    - `quickfix/RescriptReanalyzeQuickFix.kt`
- [x] コミット: `♻️ Extract offset-position conversion to RescriptOffsetUtils`

## 共通

- [x] ドキュメント更新（CLAUDE.md, README.md, sphinx-docs, product-requirements.md）
- [x] コミット: `📝 Update docs for #112, #113, #114`
- [x] `./gradlew clean buildPlugin` 成功確認
- [x] main にマージ
