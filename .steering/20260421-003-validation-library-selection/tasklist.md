# Tasklist — Wizard Validation Library Selection (zod / sury)

**参照:** `.claude/rules/definition-of-done.md` の 5 フェーズに沿う。

---

## Phase 1: 計画

- [x] `.steering/20260421-003-validation-library-selection/` 作成
- [x] `requirements.md` 作成・承認
- [x] `design.md` 作成・承認（初版）
- [x] `tasklist.md` 作成・承認（ExitPlanMode で承認済み）
- [x] foundation 2 コミットを main にマージ済み（97f0172）
- [x] sury v10.0.4 の実機 API 確認（`S.object` / `S.parseOrThrow` / `S.Error(err).message`、`namespace: false` で `S` 直接参照可）
- [x] Next.js 完全 ReScript 化の実現可能性検証（`NextServer.res` バインディング + `GreetRoute.res` + `route.ts` shim、コンパイル pass）
- [x] per-template 方針決定（モジュール名 `Validation.res`、nextjs 完全 ReScript 化、`parseXxx: JSON.t => result<_, string>` 統一）
- [x] `design.md` 改訂（sury 実機結果・Next.js 方針・`Validation.res` 命名を反映）
- [ ] `tasklist.md` 改訂（本ドキュメント）の承認
- [ ] `EnterWorktree` で `validation-library-per-template` worktree に入る

---

## Phase 2: 実装

各コミットで個別 `git add`、ktlint/test pass を確認してからコミット。

### コミット 1: `✨ Add ValidationLibrary enum + Wizard ComboBox` ✅

- [x] `src/main/kotlin/com/rescript/plugin/wizard/ValidationLibrary.kt` 新規
- [x] `src/main/kotlin/com/rescript/plugin/wizard/RescriptProjectWizardStep.kt` に ComboBox + updateDataModel 追加
- [x] `src/main/kotlin/com/rescript/plugin/wizard/RescriptModuleBuilder.kt` に field + ctx 構築追加
- [x] `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateContext.kt` に `validationLibrary` field 追加（デフォルト ZOD）
- [x] `src/test/kotlin/com/rescript/plugin/wizard/ValidationLibraryTest.kt` 新規 (4/4 pass)
- [x] `src/test/kotlin/com/rescript/plugin/wizard/RescriptModuleBuilderTest.kt` で新フィールドテスト
- [x] `src/test/kotlin/com/rescript/plugin/wizard/templates/TemplateContextTest.kt` で round-trip
- [x] ktlint + build + test pass（全 `*TemplateFilesTest` は無修正で pass、デフォルト ZOD で既存挙動）

### コミット 2: `🔧 Add SURY dependency version` ✅

- [x] `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt` に `SURY = "^10.0.0"` 追加
- [x] `TemplateVersionsTest.kt` に ZOD/SURY を semver 検証対象として追加

### 命名・配置の原則

- バリデーション API のモジュール名は **`Validation.res`**（drizzle `Schema.res` との衝突回避）
- 公開 API は **`parseXxx: JSON.t => result<T, string>`** に統一（両 variant 同一シグネチャ）
- variant 固有のみ `variants/{zod,sury}/src/Validation.res` に配置。Server.res / Routes.res / drizzle Schema.res は共通
- zod 版は既存の `@module("zod")` バインディングパターンを踏襲。sury 版は `S.object` / `S.parseOrThrow` / `S.Error(err).message`（`open Sury` 不要）

### コミット 3: `♻️ Extract hono Validation module with zod/sury variants`

- [ ] `src/main/resources/templates/hono/variants/zod/src/Validation.res` 新規（既存 `Schema.res` の zod 部分を切り出し + `parseCreateUserInput: JSON.t => result<_, string>` 統一）
- [ ] `src/main/resources/templates/hono/variants/sury/src/Validation.res` 新規
- [ ] 既存 `src/main/resources/templates/hono/src/Schema.res` は drizzle 部分のみに縮小
- [ ] `src/main/resources/templates/hono/src/Routes.res` を `Validation.parseCreateUserInput` 呼び出しに書き換え、400 エラー応答を追加
- [ ] `HonoTemplateFiles.kt` で依存切替（`zod` or `sury` の 1 つのみ）+ `variants/<key>/src/Validation.res` のロード追加
- [ ] `HonoTemplateFilesTest.kt` で ZOD / SURY 両バリアントを検証
- [ ] `TemplateResourcesSmokeTest.knownPlaceholders` を必要に応じて更新
- [ ] ktlint / test pass

### コミット 4: `♻️ Extract hono-graphql Validation module with zod/sury variants`

- [ ] `templates/hono-graphql/variants/{zod,sury}/src/Validation.res` 新規
- [ ] `templates/hono-graphql/src/Resolvers.res`（または該当箇所）を `Validation.parseXxx` 呼び出しに書き換え（mutation 入力検証を追加）
- [ ] `HonoGraphqlTemplateFiles.kt` で依存切替 + リソース切替
- [ ] `HonoGraphqlTemplateFilesTest.kt` で ZOD / SURY 両バリアントを検証

### コミット 5: `♻️ Add zod/sury Validation to AwsLambda server`

- [ ] `templates/aws-lambda/variants/{zod,sury}/src/Validation.res` 新規（`createOrderPayload` 用 parseCreateOrderPayload）
- [ ] `templates/aws-lambda/src/Server.res` の `await ctx->Hono.req->Hono.jsonBody` を `Validation.parseCreateOrderPayload` 呼び出しに置換し、400 応答を追加
- [ ] `AwsLambdaTemplateFiles.kt` で依存切替 + リソース切替
- [ ] `AwsLambdaTemplateFilesTest.kt` で ZOD / SURY 両バリアントを検証

### コミット 6: `♻️ Add zod/sury Validation to CloudflareWorkers server`

- [ ] `templates/cloudflare-workers/variants/{zod,sury}/src/Validation.res` 新規（`greetingPayload` 用 parseGreetingPayload）
- [ ] `templates/cloudflare-workers/src/Server.res` を Validation 呼び出しに書き換え + 400 応答
- [ ] `CloudflareWorkersTemplateFiles.kt` / `CloudflareWorkersTemplateFilesTest.kt` を更新

### コミット 7: `♻️ Add zod/sury Validation to GoogleCloudRun server`

- [ ] `templates/google-cloud-run/variants/{zod,sury}/src/Validation.res` 新規（`echoPayload` 用 parseEchoPayload）
- [ ] `templates/google-cloud-run/src/Server.res` を Validation 呼び出しに書き換え + 400 応答
- [ ] `GoogleCloudRunTemplateFiles.kt` / `GoogleCloudRunTemplateFilesTest.kt` を更新

### コミット 8: `♻️ Convert Nextjs route handler to ReScript with zod/sury Validation`

- [ ] `templates/nextjs/src/NextServer.res` 新規（共通バインディング）
- [ ] `templates/nextjs/src/app/api/greet/GreetRoute.res` 新規（共通ハンドラ本体）
- [ ] `templates/nextjs/src/app/api/greet/route.ts` を 1 行の re-export shim に置換
- [ ] `templates/nextjs/variants/{zod,sury}/src/app/api/greet/Validation.res` 新規（`greetInput` 用 parseGreetInput）
- [ ] `NextjsTemplateFiles.kt` で依存切替 + リソース切替
- [ ] `NextjsTemplateFilesTest.kt` で ZOD / SURY 両バリアントを検証

### コミット 9: `♻️ Add zod/sury Validation to FullStack server`

- [ ] `templates/full-stack/variants/{zod,sury}/src/server/Validation.res` 新規（`createUserReq` 用 parseCreateUserReq）
- [ ] `templates/full-stack/src/server/Routes.res` を Validation 呼び出しに書き換え + 400 応答
- [ ] `FullStackTemplateFiles.kt` / `FullStackTemplateFilesTest.kt` を更新

### コミット 10: `♻️ Add zod/sury Validation to Monorepo server`

- [ ] `templates/monorepo/variants/{zod,sury}/packages/server/src/Validation.res` 新規
- [ ] `templates/monorepo/packages/server/src/Server.res` を Validation 呼び出しに書き換え + 400 応答
- [ ] `MonorepoTemplateFiles.kt` / `MonorepoTemplateFilesTest.kt` を更新

### コミット 11: `📝 Document validation library selection`

- [ ] `CLAUDE.md` レイヤー 3 の `wizard/` 行に `ValidationLibrary` 選択の 1 文追記
- [ ] `docs/repository-structure.md` の `wizard/` パッケージ欄に `ValidationLibrary` を追加
- [ ] `sphinx-docs/user/features/advanced.md`（Wizard 節）に 1 段落追記
- [ ] `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` を同期（`make gettext` → `make update-po` → `msgstr` 日本語化 → `make build-ja` 成功確認）

---

## Phase 3: コミット前検証

各コミットで以下:

- [ ] `./gradlew ktlintCheck` pass
- [ ] `./gradlew clean buildPlugin` pass
- [ ] `./gradlew test` pass
- [ ] 新規クラス/object に英語 KDoc
- [ ] deprecated API 新規利用なし
- [ ] `TemplateResourcesSmokeTest` が pass（新プレースホルダ追加時は `knownPlaceholders` を更新）
- [ ] 個別 `git add`、絵文字プレフィックス

---

## Phase 4: マージ前

- [ ] 全 Phase 2 / Phase 3 項目が `[x]`
- [ ] `./gradlew clean buildPlugin test koverVerify verifyPluginStructure` が pass
- [ ] Kover minBound 85 を下回らない
- [ ] `AskUserQuestion` でマージ可否を確認
- [ ] `./gradlew runIde` による Wizard の手動確認（ユーザー側）

---

## Phase 5: マージ後

- [ ] `git checkout main && git merge worktree-validation-library-selection`
- [ ] worktree / ブランチ片付け（セッション終了で自動クリーンアップ）
