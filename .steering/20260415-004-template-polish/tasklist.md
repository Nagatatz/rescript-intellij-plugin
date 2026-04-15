# タスクリスト: テンプレート品質向上 (1〜7)

## Step 1: 共通インフラ拡張

- [x] `TemplateVersions.kt` に `VITEST_COVERAGE_V8` と `NODE_MAJOR` 追加
- [x] `CommonFiles.kt` に `mitLicense()`, `nvmrc()`, `dependabotYaml()`, `envExample()` 追加
- [x] `CommonFilesTest.kt` (新規 or 既存) に 4 ヘルパのテスト追加
- [x] コミット: `✨ Add nvmrc/LICENSE/dependabot/env-example helpers to CommonFiles`

## Step 2: 全 14 テンプレに共通ファイル (.nvmrc / LICENSE / dependabot) + coverage

- [x] Basic / Electron / CloudflareWorkers / AwsLambda / GoogleCloudRun / ReactNative / CliTool / NpmLibrary / Nextjs / ViteReact / Hono / HonoGraphql / Monorepo / FullStack にそれぞれ追加
- [x] `@vitest/coverage-v8` devDep + `test:coverage` script 追加
- [x] 各 *TemplateFilesTest.kt を更新
- [x] コミット: `✨ Add .nvmrc, LICENSE, dependabot, and coverage script to all templates`

## Step 3: env を使う 5 テンプレに `.env.example`

- [x] Hono REST: `DATABASE_URL`
- [x] Hono GraphQL: `DATABASE_URL`
- [x] Full-Stack: `DATABASE_URL`
- [x] Monorepo server: `DATABASE_URL`
- [x] Google Cloud Run: `PORT`
- [x] 各テストで `.env.example` の存在を検証
- [x] コミット: `✨ Add .env.example to templates that read env vars`

## Step 4: Hono 系 4 テンプレに `app.onError`

- [x] `ProjectFileBuilders.honoBindings()` に `onError` 追加
- [x] Hono REST / Hono GraphQL / Full-Stack Server.res / Monorepo server Server.res に `app->Hono.onError(...)` 追加
- [x] 各テストで onError 呼び出しの存在を検証
- [x] コミット: `✨ Add global app.onError handler to Hono templates`

## Step 5: Hono 系 4 テンプレの route test 拡張

- [x] Hono REST: `/api/health` or similar DB-free route を `app.request()` で叩く
- [x] Hono GraphQL: `/` or `/graphql` (GET returns GraphiQL HTML) を `app.request()` で叩く
- [x] Full-Stack: `/api/health` を `app.request()` で叩く
- [x] Monorepo server: `/api/hello` を `app.request()` で叩く
- [x] 各テストで `app.request` assertion 追加
- [x] コミット: `✨ Upgrade Hono template tests to use app.request() route harness`

## Step 6: ドキュメント更新

- [x] CLAUDE.md — wizard 記述を更新 (LICENSE / .nvmrc / dependabot / coverage / env.example に言及)
- [x] README.md — 同様
- [x] docs/product-requirements.md — 同様
- [x] sphinx-docs/user/features/advanced.md — Quality of Life セクション拡充
- [x] sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po — 対応する翻訳追加
- [x] コミット: `📝 Document template polish additions (LICENSE, nvmrc, dependabot, coverage, env.example)`

## Step 7: 検証 + マージ

- [x] `./gradlew ktlintCheck buildPlugin test` 成功
- [x] `tasklist.md` を全て `[x]` に更新
- [x] ユーザーにマージ可否確認
- [x] main にマージ + ブランチ削除 + セッション終了
