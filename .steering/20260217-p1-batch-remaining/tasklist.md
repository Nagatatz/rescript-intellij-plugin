# Tasklist: P1 残り全6機能バッチ実装

## 事前準備

- [x] requirements.md 作成・承認
- [x] design.md 作成・承認
- [x] tasklist.md 作成・承認
- [x] git worktree セットアップ（6ブランチ）

## Feature 1: JSON Schema（ブランチ: `feature/json-schema`）

- [x] `RescriptJsonSchemaProviderFactory.kt` 作成
- [x] `rescript.schema.json` 作成
- [x] `plugin.xml` に登録（optional dependency 含む）
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add JSON Schema for rescript.json`

## Feature 2: %raw() JS ハイライト（ブランチ: `feature/raw-js-highlight`）

- [x] `RescriptRawJsInjector.kt` 作成
- [x] `plugin.xml` に登録（optional dependency 含む）
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add JavaScript highlighting in %raw() blocks`

## Feature 3: Postfix Completion（ブランチ: `feature/postfix-completion`）

- [x] `RescriptPostfixTemplateProvider.kt` 作成
- [x] `plugin.xml` に登録
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add ReScript postfix completion templates`

## Feature 4: Console Filter（ブランチ: `feature/console-filter`）

- [x] `RescriptConsoleFilterProvider.kt` 作成
- [x] `plugin.xml` に登録
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add console filter for ReScript compiler output`

## Feature 5: Editor Notification Bar（ブランチ: `feature/editor-notification`）

- [x] `RescriptEditorNotificationProvider.kt` 作成
- [x] `plugin.xml` に登録
- [x] テスト作成（テスト省略理由: LSP サーバー検出との結合が必要で単体テスト困難）
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add editor notification for missing language server`

## Feature 6: Go to Related（ブランチ: `feature/goto-related`）

- [x] `RescriptGotoRelatedProvider.kt` 作成
- [x] `plugin.xml` に登録
- [x] テスト作成
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add Go to Related for .res/.resi/.js files`

## マージ作業（メインウィンドウ）

- [x] 6ブランチすべてのビルド成功確認
- [x] バッチブランチ `feature/p1-batch-remaining` に順次マージ（plugin.xml 競合解決）
- [x] マージ後 `./gradlew buildPlugin` 成功確認
- [x] git worktree クリーンアップ
- [x] バッチブランチを `main` にマージ
- [x] バッチブランチを削除
