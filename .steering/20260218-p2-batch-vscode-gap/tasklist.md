# Tasklist: P2 rescript-vscode ギャップ 5機能バッチ実装

## 事前準備

- [x] requirements.md 作成・承認
- [x] design.md 作成・承認
- [x] tasklist.md 作成・承認

## 共有インフラ（バッチブランチ）

- [x] `lsp/RescriptLanguageServer.kt` 作成（カスタムリクエストインターフェース）
- [x] `lsp/RescriptLsp4jClient.kt` 作成（カスタム通知受信）
- [x] `lsp/RescriptCompilationStatusService.kt` 作成（状態保持・配信サービス）
- [x] `lsp/RescriptLspServerDescriptor.kt` 更新（`lsp4jServerClass` + `createLsp4jClient()`）
- [x] `plugin.xml` に `RescriptCompilationStatusService` 登録
- [x] `./gradlew buildPlugin` 成功確認
- [x] コミット: `✨ Add LSP custom request/notification infrastructure`
- [ ] git worktree セットアップ（5ブランチ）
- [ ] window-instructions.md 作成

## Feature 1: Signature Help（ブランチ: `feature/signature-help`）

- [ ] 動作確認（`runIde` で手動テスト）
- [ ] ステアリングドキュメント作成
- [ ] テスト作成（テスト省略理由: LSP サーバーとの結合が必須で単体テスト困難）
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Verify Signature Help support via LSP`

## Feature 2: Code Lens（ブランチ: `feature/code-lens`）

- [ ] `RescriptCodeVisionProvider.kt` 作成
- [ ] `plugin.xml` に登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add Code Lens via CodeVision API`

## Feature 3: インターフェース生成（ブランチ: `feature/create-interface`）

- [ ] `RescriptCreateInterfaceAction.kt` 作成
- [ ] `plugin.xml` にアクション登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add Create Interface File action`

## Feature 4: コンパイル済み JS（ブランチ: `feature/open-compiled`）

- [ ] `RescriptOpenCompiledJsAction.kt` 作成
- [ ] `plugin.xml` にアクション登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add Open Compiled JavaScript action`

## Feature 5: ビルドステータス（ブランチ: `feature/build-status`）

- [ ] `RescriptCompilerStatusWidgetFactory.kt` 作成（Factory + Widget）
- [ ] `plugin.xml` に登録
- [ ] ステアリングドキュメント作成
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] コミット: `✨ Add compiler build status widget`

## マージ作業（メインウィンドウ）

- [ ] 5ブランチすべてのビルド成功確認
- [ ] バッチブランチ `feature/p2-batch-vscode-gap` に順次マージ（plugin.xml 競合解決）
- [ ] マージ後 `./gradlew buildPlugin` 成功確認
- [ ] git worktree クリーンアップ
- [ ] ドキュメント一括更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `📝 Update docs for P2 vscode gap features`
- [ ] バッチブランチを `main` にマージ
- [ ] バッチブランチを削除
