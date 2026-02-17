# Tasklist: P1 残り全6機能バッチ実装

## 事前準備

- [x] requirements.md 作成・承認
- [x] design.md 作成・承認
- [x] tasklist.md 作成・承認
- [ ] git worktree セットアップ（6ブランチ）

## Feature 1: JSON Schema（ブランチ: `feature/json-schema`）

- [ ] `RescriptJsonSchemaProviderFactory.kt` 作成
- [ ] `rescript.schema.json` 作成
- [ ] `plugin.xml` に登録（optional dependency 含む）
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `✨ Add JSON Schema for rescript.json`

## Feature 2: %raw() JS ハイライト（ブランチ: `feature/raw-js-highlight`）

- [ ] `RescriptRawJsInjector.kt` 作成
- [ ] `plugin.xml` に登録（optional dependency 含む）
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `✨ Add JavaScript highlighting in %raw() blocks`

## Feature 3: Postfix Completion（ブランチ: `feature/postfix-completion`）

- [ ] `RescriptPostfixTemplateProvider.kt` 作成
- [ ] `plugin.xml` に登録
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `✨ Add ReScript postfix completion templates`

## Feature 4: Console Filter（ブランチ: `feature/console-filter`）

- [ ] `RescriptConsoleFilterProvider.kt` 作成
- [ ] `plugin.xml` に登録
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `✨ Add console filter for ReScript compiler output`

## Feature 5: Editor Notification Bar（ブランチ: `feature/editor-notification`）

- [ ] `RescriptEditorNotificationProvider.kt` 作成
- [ ] `plugin.xml` に登録
- [ ] テスト作成（テスト省略理由: LSP サーバー検出との結合が必要で単体テスト困難）
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `✨ Add editor notification for missing language server`

## Feature 6: Go to Related（ブランチ: `feature/goto-related`）

- [ ] `RescriptGotoRelatedProvider.kt` 作成
- [ ] `plugin.xml` に登録
- [ ] テスト作成
- [ ] `./gradlew buildPlugin` 成功確認
- [ ] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [ ] コミット: `✨ Add Go to Related for .res/.resi/.js files`

## マージ作業（メインウィンドウ）

- [ ] 6ブランチすべてのビルド成功確認
- [ ] `main` に順次マージ（plugin.xml 競合解決）
- [ ] マージ後 `./gradlew buildPlugin` 成功確認
- [ ] git worktree クリーンアップ
