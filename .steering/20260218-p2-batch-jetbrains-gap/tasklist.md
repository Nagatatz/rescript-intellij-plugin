# Tasklist: P2 JetBrains ギャップ 5機能バッチ実装

## 事前準備

- [x] requirements.md 作成・承認
- [x] design.md 作成・承認
- [x] tasklist.md 作成・承認
- [x] ステアリングドキュメントをコミット
- [x] git worktree セットアップ（5ブランチ）
- [x] window-instructions.md 作成

## Feature 1: Quick Fix（ブランチ: `feature/quick-fix`）

- [x] 動作確認（LSP codeAction の自動対応検証）
- [x] ステアリングドキュメント作成
- [x] テスト作成（テスト省略理由: LSP サーバーとの結合が必須で単体テスト困難）
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Verify Quick Fix support via LSP code actions`

## Feature 2: Intention Actions（ブランチ: `feature/intention-actions`）

- [x] `RescriptWrapWithIntention.kt` 作成（Some, Ok, Error, AddGenType）
- [x] `plugin.xml` に登録
- [x] ステアリングドキュメント作成
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add ReScript intention actions`

## Feature 3: Surround With（ブランチ: `feature/surround-with`）

- [x] `RescriptSurroundDescriptor.kt` 作成（if, switch, try, block）
- [x] `plugin.xml` に登録
- [x] ステアリングドキュメント作成
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add Surround With support`

## Feature 4: Import Optimizer（ブランチ: `feature/import-optimizer`）

- [x] `RescriptImportOptimizer.kt` 作成
- [x] `plugin.xml` に登録
- [x] ステアリングドキュメント作成
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add Import Optimizer for duplicate open removal`

## Feature 5: Gutter Run Icons（ブランチ: `feature/run-line-marker`）

- [x] `RescriptRunLineMarkerContributor.kt` 作成
- [x] `plugin.xml` に登録
- [x] ステアリングドキュメント作成
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add gutter run icons for ReScript files`

## マージ作業（メインウィンドウ）

- [x] 5ブランチすべてのビルド成功確認
- [x] バッチブランチ `feature/p2-batch-jetbrains-gap` に順次マージ（plugin.xml 競合解決）
- [x] マージ後 `./gradlew buildPlugin` 成功確認
- [x] git worktree クリーンアップ
- [x] ドキュメント一括更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `📝 Update docs for P2 JetBrains gap features`
- [ ] バッチブランチを `main` にマージ
- [ ] バッチブランチを削除
