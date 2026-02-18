# Tasklist: P3 rescript-vscode ギャップ 5機能バッチ実装

## 事前準備

- [x] requirements.md 作成・承認
- [x] design.md 作成・承認
- [x] tasklist.md 作成・承認
- [x] ステアリングドキュメントをコミット
- [x] git worktree セットアップ（5ブランチ）
- [x] window-instructions.md 作成

## Feature 1: reanalyze 統合（ブランチ: `feature/reanalyze`）

- [ ] `RescriptReanalyzeAnnotator.kt` 作成
- [ ] `plugin.xml` に登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add reanalyze integration for dead code analysis`

## Feature 2: Markdown ReScript ハイライト（ブランチ: `feature/markdown-highlight`）

- [ ] `RescriptMarkdownCodeFenceProvider.kt` 作成
- [ ] `rescript-markdown.xml` 作成
- [ ] `plugin.xml` に optional dependency 追加
- [ ] `build.gradle.kts` に bundledPlugin 追加
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add ReScript syntax highlighting in Markdown code fences`

## Feature 3: Paste as JSON.t（ブランチ: `feature/paste-as-json`）

- [ ] `RescriptPasteAsJsonAction.kt` 作成
- [ ] `plugin.xml` に action 登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add Paste as JSON.t action`

## Feature 4: `//#region` 折りたたみ（ブランチ: `feature/region-folding`）

- [ ] `RescriptCustomFoldingProvider.kt` 作成
- [ ] `RescriptFoldingBuilder.kt` を `CustomFoldingBuilder` に変更
- [ ] `plugin.xml` に customFoldingProvider 登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成・更新
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add //#region folding support`

## Feature 5: Incremental Type Checking 設定（ブランチ: `feature/incremental-tc`）

- [ ] `RescriptProjectSettings.kt` に設定追加
- [ ] `RescriptConfigurable.kt` に UI 追加
- [ ] `RescriptLspServerDescriptor.kt` の初期化オプション更新
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add incremental type checking setting`

## マージ作業（メインウィンドウ）

- [ ] 5ブランチすべてのビルド成功確認
- [ ] バッチブランチ `feature/p3-batch-vscode-gap` に順次マージ（plugin.xml 競合解決）
- [ ] マージ後 `./gradlew buildPlugin` 成功確認
- [ ] git worktree クリーンアップ
- [ ] ドキュメント一括更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `📝 Update docs for P3 vscode gap features`
- [ ] バッチブランチを `main` にマージ
- [ ] バッチブランチを削除
