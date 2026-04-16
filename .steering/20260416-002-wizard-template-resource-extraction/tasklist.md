# Tasklist — Wizard Template Resource Extraction

**参照:** `.claude/rules/definition-of-done.md` の5フェーズに沿う。

---

## Phase 1: 計画

- [x] `.steering/20260416-002-wizard-template-resource-extraction/` 作成
- [x] `requirements.md` 作成・承認
- [x] `design.md` 作成・承認
- [x] `tasklist.md` 作成・承認
- [x] `EnterWorktree` で `wizard-template-extract` worktree に入る

---

## Phase 2: 実装

### コミット 1: `✨ Add TemplateResourceLoader utility` ✅

- [x] `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateResourceLoader.kt` 新規
- [x] `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateResourceLoaderTest.kt` 新規 (6 テスト全 pass)
- [x] ダミーリソース `src/test/resources/templates/__test__/{basic,with-placeholder,utf8}.txt`
- [x] `./gradlew ktlintCheck && ./gradlew test --tests "*TemplateResourceLoaderTest"` が通る
- [x] 個別 `git add` でコミット (f3a9f40)

### コミット 2: `✨ Add templates resource smoke test` ✅

- [x] `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateResourcesSmokeTest.kt` 新規 (2 テスト pass)
- [x] `src/main/resources/templates/` 下の再帰走査 + プレースホルダ検証
- [x] 個別 `git add` でコミット (6d9ed8f)

### コミット 3: `♻️ Extract static content from HonoTemplateFiles to resources` ✅

- [x] pre-snapshot 採取 (`/tmp/tpl-snap-pre/hono/{NPM,PNPM,YARN}/`)
- [x] `src/main/resources/templates/hono/` に 13 ファイル抽出 (`.res` 7, `drizzle.config.ts`, `Server.test.mjs`, `readme/*.md` 4)
- [x] `HonoTemplateFiles.kt` 修正: 13 private fun 削除、`generate()` が `TemplateResourceLoader.load(...)` 呼び出しに差し替え
- [x] `TemplateResourcesSmokeTest` の許容プレースホルダに `cmdDbGenerate`, `cmdDbMigrate` 追加
- [x] post-snapshot 採取 → `diff -r pre/hono post/hono` で完全一致確認
- [x] 既存 `HonoTemplateFilesTest` (11 テスト) 無修正で pass
- [x] smoke test / loader test pass、ktlint pass

### コミット 4: `♻️ Extract static content from HonoGraphqlTemplateFiles to resources` ✅

- [x] pre-snapshot 採取
- [x] `src/main/resources/templates/hono-graphql/` に 13 ファイル抽出 (.res 6 + .graphql + drizzle.config.ts + .mjs + readme/*.md 4)
- [x] `HonoGraphqlTemplateFiles.kt` 386→127行、13 private fun 削除
- [x] `cmdDbGenerate`/`cmdDbMigrate` プレースホルダ再利用 (既に許容リスト済み)
- [x] post-snapshot → `diff -r` 完全一致
- [x] 既存 `HonoGraphqlTemplateFilesTest` (13 テスト) 無修正で pass
- [x] ktlint pass

### コミット 5: `♻️ Extract static content from ReactNativeCliTemplateFiles to resources` ✅

- [x] pre-snapshot 採取
- [x] `src/main/resources/templates/react-native-cli/` に 15 ファイル抽出 (JS/TS 3 + .res 3 + app.json + test.mjs + readme/*.md 7)
- [x] `ReactNativeCliTemplateFiles.kt` 404→162行
- [x] 新規プレースホルダ: `projectName`/`installCmd`/`execReactNative`/`cmdResDev`/`cmdStart`/`cmdAndroid`
- [x] post-snapshot → `diff -r` 完全一致
- [x] 既存 `ReactNativeCliTemplateFilesTest` (14 テスト) 無修正で pass
- [x] ktlint pass

### コミット 6: `♻️ Extract static content from FullStackTemplateFiles to resources` ✅

- [x] pre-snapshot 採取
- [x] `src/main/resources/templates/full-stack/` に 19 ファイル抽出 (.res 11 + .ts + .html + .mjs 2 + readme/*.md 5)
- [x] `FullStackTemplateFiles.kt` 479→183行
- [x] 既存プレースホルダ再利用 (projectName, cmdDbGenerate, cmdDbMigrate)
- [x] post-snapshot → `diff -r` 完全一致
- [x] 既存 `FullStackTemplateFilesTest` (13 テスト) 無修正で pass
- [x] ktlint pass

### コミット 7: `♻️ Extract static content from MonorepoTemplateFiles to resources`

- [ ] スナップショット採取 (pre) — NPM / PNPM 両方 (monorepo は PM 分岐多し)
- [ ] `MonorepoTemplateFiles.kt` を読み、静的 `private fun` を洗い出し
- [ ] `src/main/resources/templates/monorepo/` 下に抽出
- [ ] Kotlin 側を `TemplateResourceLoader.load(...)` に差し替え
- [ ] ctx 依存セクションのプレースホルダ化 (許容リスト更新)
- [ ] スナップショット採取 (post) → `diff -r` で一致 (NPM / PNPM 両方)
- [ ] 既存 `MonorepoTemplateFilesTest` が無修正で通ること
- [ ] `./gradlew ktlintCheck && ./gradlew test --tests "*Monorepo*" --tests "*TemplateResourcesSmokeTest"` が通る
- [ ] 個別 `git add` でコミット

### コミット 8: `📝 Document TemplateResourceLoader in CLAUDE.md and repository-structure.md`

- [ ] `CLAUDE.md` 「ユーティリティ」項に `TemplateResourceLoader` の1行を追加
- [ ] `docs/repository-structure.md` の `src/main/resources/` テーブルに `templates/` 行を追加
- [ ] `git add` でコミット

---

## Phase 3: コミット前検証

各コミットで以下を満たしていることを確認:

### コード品質
- [ ] すべての新規 `class` / `object` に英語 KDoc
- [ ] `./gradlew ktlintCheck` pass
- [ ] 新規コード警告なし

### テスト
- [ ] 新規 `TemplateResourceLoader` / `TemplateResourcesSmokeTest` のテストファイル存在
- [ ] `./gradlew test` 全体 pass
- [ ] 既存 5 テンプレートテストが**無修正で** pass
- [ ] カバレッジ `minBound(85)` を下回らない (`./gradlew koverHtmlReport`)

### ドキュメント同期
- [ ] `CLAUDE.md` 更新 (コミット 8)
- [ ] `docs/repository-structure.md` 更新 (コミット 8)
- [ ] `README.md`, `sphinx-docs/user/features/*`, `docs/product-requirements.md` は**更新不要** (内部リファクタリング、ユーザー可視機能の変更なし)

### Extension Point
- [ ] 新規 Extension Point なし → `plugin.xml` 変更不要

### Git
- [ ] コミット 1〜8 が機能単位で分割済み
- [ ] 絵文字プレフィックス付与
- [ ] 個別 `git add` (全 `-A` / `.` 禁止)

### セキュリティ
- [ ] `TemplateResourceLoader.load()` の `path` 引数はコード内ハードコード限定
- [ ] ユーザー入力は `vars` map 経由のみ
- [ ] 絶対パスは UI/エラーメッセージに露出していない

---

## Phase 4: マージ前

- [ ] 全タスクが `[x]`
- [ ] requirements.md の AC-01〜AC-08 すべて満たす
- [ ] `./gradlew clean buildPlugin` が通る
- [ ] `unzip -l build/distributions/rescript-intellij-plugin-*.zip | grep 'templates/'` でリソースがパッケージされていることを確認
- [ ] `./gradlew runIde` で 5 テンプレートを実際に Wizard から生成 → ファイル内容が期待通り (任意、重い場合は省略してスナップショット diff で代替)
- [ ] `AskUserQuestion` でマージ可否を確認

---

## Phase 5: マージ後

- [ ] `git checkout main && git merge worktree-wizard-template-extract`
- [ ] `git branch -d worktree-wizard-template-extract`
- [ ] セッション終了で worktree 自動クリーンアップ
- [ ] 親セッションで Task #1 を `completed` に更新、Task #2 を `in_progress` に

---

## 備考

- スナップショット diff は CI で再現しないため、ローカル verification step として実施。refactor 完全性の最重要ガード。
- コミット 3〜7 はスナップショット一致が取れない場合、そのコミットを破棄して原因調査する。**一致するまで次のテンプレートに進まない**。
- `TemplateResourcesSmokeTest` の「許容プレースホルダ一覧」は各コミットで更新するため、同テストの修正は各リファクタコミットに同梱する (`[`TemplateResourcesSmokeTest.kt` update + 新リソース + Kotlin 差し替え] = 1コミット)。
