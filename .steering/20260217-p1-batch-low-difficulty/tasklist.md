# Tasklist: P1 低難易度バッチ実装

## 事前準備

- [x] requirements.md 作成・承認
- [x] design.md 作成・承認
- [x] tasklist.md 作成・承認
- [ ] git worktree セットアップ（4ブランチ）

## Feature 1: `.res`/`.resi` 切り替え（ブランチ: `feature/res-resi-switch`）

- [x] `RescriptSwitchFileAction.kt` 作成
- [x] `plugin.xml` に `<action>` 登録
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add .res/.resi file switch action (Alt+O)`

## Feature 2: Live Templates（ブランチ: `feature/live-templates`）

- [x] `resources/liveTemplates/ReScript.xml` 作成（15テンプレート）
- [x] `plugin.xml` に `<defaultLiveTemplates>` 登録
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add ReScript live templates`

## Feature 3: File Templates（ブランチ: `feature/file-templates`）

- [x] `RescriptCreateFileAction.kt` 作成
- [x] テンプレートファイル 3つ作成（Module, Interface, Component）
- [x] `plugin.xml` に `<internalFileTemplate>` + `<action>` 登録
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add ReScript file templates (New > ReScript File)`

## Feature 4: Spell Checking（ブランチ: `feature/spell-checking`）

- [x] `RescriptSpellcheckingStrategy.kt` 作成
- [x] `plugin.xml` に `<spellchecker.support>` 登録
- [x] `./gradlew buildPlugin` 成功確認
- [x] ドキュメント更新（CLAUDE.md, product-requirements.md, functional-design.md）
- [x] コミット: `✨ Add spell checking support for ReScript files`

## マージ作業（メインウィンドウ）

- [ ] 4ブランチすべてのビルド成功確認
- [ ] `main` に順次マージ（plugin.xml 競合解決）
- [ ] マージ後 `./gradlew buildPlugin` 成功確認
- [ ] git worktree クリーンアップ
