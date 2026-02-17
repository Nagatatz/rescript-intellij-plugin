# Tasklist: P2 JetBrains ギャップ 5機能バッチ実装

## 事前準備

- [x] requirements.md 作成・承認
- [x] design.md 作成・承認
- [x] tasklist.md 作成・承認
- [x] ステアリングドキュメントをコミット
- [x] git worktree セットアップ（5ブランチ）
- [x] window-instructions.md 作成

## Feature 1: Quick Fix（ブランチ: `feature/quick-fix`）

- [ ] 動作確認（LSP codeAction の自動対応検証）
- [ ] ステアリングドキュメント作成
- [ ] テスト作成（テスト省略理由: LSP サーバーとの結合が必須で単体テスト困難）
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Verify Quick Fix support via LSP code actions`

## Feature 2: Intention Actions（ブランチ: `feature/intention-actions`）

- [ ] `RescriptWrapWithIntention.kt` 作成（Some, Ok, Error, AddGenType）
- [ ] `plugin.xml` に登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add ReScript intention actions`

## Feature 3: Surround With（ブランチ: `feature/surround-with`）

- [ ] `RescriptSurroundDescriptor.kt` 作成（if, switch, try, block）
- [ ] `plugin.xml` に登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add Surround With support`

## Feature 4: Import Optimizer（ブランチ: `feature/import-optimizer`）

- [ ] `RescriptImportOptimizer.kt` 作成
- [ ] `plugin.xml` に登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add Import Optimizer for duplicate open removal`

## Feature 5: Gutter Run Icons（ブランチ: `feature/run-line-marker`）

- [ ] `RescriptRunLineMarkerContributor.kt` 作成
- [ ] `plugin.xml` に登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add gutter run icons for ReScript files`

## マージ作業（メインウィンドウ）

- [ ] 5ブランチすべてのビルド成功確認
- [ ] バッチブランチ `feature/p2-batch-jetbrains-gap` に順次マージ（plugin.xml 競合解決）
- [ ] マージ後 `./gradlew buildPlugin` 成功確認
- [ ] git worktree クリーンアップ
- [ ] ドキュメント一括更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `📝 Update docs for P2 JetBrains gap features`
- [ ] バッチブランチを `main` にマージ
- [ ] バッチブランチを削除
