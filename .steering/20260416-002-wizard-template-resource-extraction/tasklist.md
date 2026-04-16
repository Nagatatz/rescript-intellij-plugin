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

### コミット 1: `✨ Add TemplateResourceLoader utility`

- [ ] `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateResourceLoader.kt` 新規
  - `internal object TemplateResourceLoader`
  - `fun load(path: String, vars: Map<String, String> = emptyMap(), strict: Boolean = true): String`
  - KDoc をクラスと `load` に英語で付与
- [ ] `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateResourceLoaderTest.kt` 新規
  - ダミーリソース: `src/test/resources/templates/__test__/basic.txt`, `with-placeholder.txt`
  - ケース:
    - [ ] 存在するリソースをロード
    - [ ] プレースホルダ置換
    - [ ] strict=true で未置換 → `IllegalStateException`
    - [ ] strict=false で未置換 → 原文残存
    - [ ] 存在しないパス → `IllegalStateException`
    - [ ] UTF-8 マルチバイト文字のロード
- [ ] `./gradlew ktlintCheck && ./gradlew test --tests "*TemplateResourceLoaderTest"` が通る
- [ ] 個別 `git add` でコミット

### コミット 2: `✨ Add templates resource smoke test`

- [ ] `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateResourcesSmokeTest.kt` 新規
  - 既知プレースホルダ許容リスト (初期値 = 設計した dynamic プレースホルダ名の列挙)
  - `src/main/resources/templates/` 下を再帰走査 (存在しない場合は空リスト = パス)
  - 各ファイルが UTF-8 で読める
  - `{{key}}` が含まれる場合、`key` が許容リストにあること
- [ ] `./gradlew test --tests "*TemplateResourcesSmokeTest"` が通る (`templates/` がまだ空でもパスする実装)
- [ ] 個別 `git add` でコミット

### コミット 3: `♻️ Extract static content from HonoTemplateFiles to resources`

- [ ] **スナップショット採取 (コミット対象外):** refactor 前に `HonoTemplateFiles.generate(TemplateContext("svc", NPM))` と `PNPM` 2ケースの Map を `/tmp/hono-snapshot-{pre}-{pm}/` に保存
- [ ] `src/main/resources/templates/hono/` 下に抽出:
  - `src/Logger.res`
  - `src/Schema.res`
  - `src/Db.res`
  - `src/ZodOpenapi.res`
  - `src/Scalar.res`
  - `src/Routes/Users.res`
  - `src/Server.res`
  - `drizzle.config.ts`
  - `src/__tests__/Server.test.mjs`
  - `readme/api.md`
  - `readme/database.md` (プレースホルダ: `{{cmdDbGenerate}}`, `{{cmdDbMigrate}}`)
  - `readme/openapi.md`
  - `readme/project-layout.md`
- [ ] `HonoTemplateFiles.kt` を修正:
  - 該当 `private fun` を削除
  - `generate()` の該当行を `TemplateResourceLoader.load("hono/...", ...)` に差し替え
  - `databaseSection` は `TemplateResourceLoader.load("hono/readme/database.md", mapOf("cmdDbGenerate" to ctx.runCmd("db:generate"), "cmdDbMigrate" to ctx.runCmd("db:migrate")))` に
- [ ] スナップショット採取 (post) → `diff -r pre/ post/` で完全一致を確認
- [ ] `TemplateResourcesSmokeTest` の許容プレースホルダ一覧に `cmdDbGenerate`, `cmdDbMigrate` を追加
- [ ] 既存 `HonoTemplateFilesTest` が**無修正で**通ること
- [ ] `./gradlew ktlintCheck && ./gradlew test --tests "*HonoTemplateFilesTest" --tests "*TemplateResourcesSmokeTest"` が通る
- [ ] 個別 `git add` でコミット (`.kt` + 新規 resources のみ)

### コミット 4: `♻️ Extract static content from HonoGraphqlTemplateFiles to resources`

- [ ] スナップショット採取 (pre)
- [ ] `HonoGraphqlTemplateFiles.kt` を読み、静的 `private fun` を洗い出し
- [ ] `src/main/resources/templates/hono-graphql/` 下に抽出 (実装時に具体ファイル一覧を確定)
- [ ] Kotlin 側を `TemplateResourceLoader.load(...)` 呼び出しに差し替え
- [ ] ctx 依存セクションがあればプレースホルダ化 (許容リスト更新)
- [ ] スナップショット採取 (post) → `diff -r` で一致
- [ ] 既存 `HonoGraphqlTemplateFilesTest` が無修正で通ること
- [ ] `./gradlew ktlintCheck && ./gradlew test --tests "*HonoGraphql*" --tests "*TemplateResourcesSmokeTest"` が通る
- [ ] 個別 `git add` でコミット

### コミット 5: `♻️ Extract static content from ReactNativeCliTemplateFiles to resources`

- [ ] スナップショット採取 (pre)
- [ ] `ReactNativeCliTemplateFiles.kt` を読み、静的 `private fun` を洗い出し
- [ ] `src/main/resources/templates/react-native-cli/` 下に抽出
- [ ] Kotlin 側を `TemplateResourceLoader.load(...)` に差し替え
- [ ] ctx 依存セクションのプレースホルダ化 (許容リスト更新)
- [ ] スナップショット採取 (post) → `diff -r` で一致
- [ ] 既存 `ReactNativeCliTemplateFilesTest` が無修正で通ること
- [ ] `./gradlew ktlintCheck && ./gradlew test --tests "*ReactNativeCli*" --tests "*TemplateResourcesSmokeTest"` が通る
- [ ] 個別 `git add` でコミット

### コミット 6: `♻️ Extract static content from FullStackTemplateFiles to resources`

- [ ] スナップショット採取 (pre)
- [ ] `FullStackTemplateFiles.kt` を読み、静的 `private fun` を洗い出し
- [ ] `src/main/resources/templates/full-stack/` 下に抽出
- [ ] Kotlin 側を `TemplateResourceLoader.load(...)` に差し替え
- [ ] ctx 依存セクションのプレースホルダ化 (許容リスト更新)
- [ ] スナップショット採取 (post) → `diff -r` で一致
- [ ] 既存 `FullStackTemplateFilesTest` が無修正で通ること
- [ ] `./gradlew ktlintCheck && ./gradlew test --tests "*FullStack*" --tests "*TemplateResourcesSmokeTest"` が通る
- [ ] 個別 `git add` でコミット

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
