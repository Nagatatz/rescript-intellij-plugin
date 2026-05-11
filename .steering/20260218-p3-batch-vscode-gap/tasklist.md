# Tasklist: P3 rescript-vscode ギャップ 5機能バッチ実装

## 事前準備

- [x] requirements.md 作成・承認
- [x] design.md 作成・承認
- [x] tasklist.md 作成・承認
- [x] ステアリングドキュメントをコミット
- [x] git worktree セットアップ（5ブランチ）
- [x] window-instructions.md 作成

## Feature 1: reanalyze 統合（ブランチ: `feature/reanalyze`）

- [x] `RescriptReanalyzeAnnotator.kt` 作成
- [x] `plugin.xml` に登録
- [x] ステアリングドキュメント作成
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add reanalyze integration for dead code analysis`

## Feature 2: Markdown ReScript ハイライト（ブランチ: `feature/markdown-highlight`）

- [x] `RescriptMarkdownCodeFenceProvider.kt` 作成
- [x] `rescript-markdown.xml` 作成
- [x] `plugin.xml` に optional dependency 追加
- [x] `build.gradle.kts` に bundledPlugin 追加
- [x] ステアリングドキュメント作成
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add Markdown code fence highlighting for ReScript`

## Feature 3: Paste as JSON.t（ブランチ: `feature/paste-as-json`）

- [x] `RescriptPasteAsJsonAction.kt` 作成
- [x] `plugin.xml` に action 登録
- [x] ステアリングドキュメント作成
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add Paste as JSON.t action for ReScript files`

## Feature 4: `//#region` 折りたたみ（ブランチ: `feature/region-folding`）

- [x] `RescriptCustomFoldingProvider.kt` 作成
- [x] `RescriptFoldingBuilder.kt` を `CustomFoldingBuilder` に変更
- [x] `plugin.xml` に customFoldingProvider 登録
- [x] ステアリングドキュメント作成
- [x] テスト作成・更新
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add //#region custom folding support`

## Feature 5: Incremental Type Checking 設定（ブランチ: `feature/incremental-tc`）

- [x] `RescriptProjectSettings.kt` に設定追加
- [x] `RescriptConfigurable.kt` に UI 追加
- [x] `RescriptLspServerDescriptor.kt` の初期化オプション更新
- [x] ステアリングドキュメント作成
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add incremental type checking setting with LSP restart`

## マージ作業（メインウィンドウ）

- [x] 5ブランチすべてのビルド成功確認
- [x] バッチブランチ `feature/p3-batch-vscode-gap` に順次マージ（plugin.xml 競合解決）
- [x] マージ後 `./gradlew buildPlugin` 成功確認
- [x] git worktree クリーンアップ
- [x] ドキュメント一括更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `📝 Update docs for P3 vscode gap features`
- [x] バッチブランチを `main` にマージ
- [x] バッチブランチを削除
