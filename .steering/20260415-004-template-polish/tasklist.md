# タスクリスト: テンプレート品質向上 (1〜7)

## Step 1: 共通インフラ拡張

- [ ] `TemplateVersions.kt` に `VITEST_COVERAGE_V8` と `NODE_MAJOR` 追加
- [ ] `CommonFiles.kt` に `mitLicense()`, `nvmrc()`, `dependabotYaml()`, `envExample()` 追加
- [ ] `CommonFilesTest.kt` (新規 or 既存) に 4 ヘルパのテスト追加
- [ ] コミット: `✨ Add nvmrc/LICENSE/dependabot/env-example helpers to CommonFiles`

## Step 2: 全 14 テンプレに共通ファイル (.nvmrc / LICENSE / dependabot) + coverage

- [ ] Basic / Electron / CloudflareWorkers / AwsLambda / GoogleCloudRun / ReactNative / CliTool / NpmLibrary / Nextjs / ViteReact / Hono / HonoGraphql / Monorepo / FullStack にそれぞれ追加
- [ ] `@vitest/coverage-v8` devDep + `test:coverage` script 追加
- [ ] 各 *TemplateFilesTest.kt を更新
- [ ] コミット: `✨ Add .nvmrc, LICENSE, dependabot, and coverage script to all templates`

## Step 3: env を使う 5 テンプレに `.env.example`

- [ ] Hono REST: `DATABASE_URL`
- [ ] Hono GraphQL: `DATABASE_URL`
- [ ] Full-Stack: `DATABASE_URL`
- [ ] Monorepo server: `DATABASE_URL`
- [ ] Google Cloud Run: `PORT`
- [ ] 各テストで `.env.example` の存在を検証
- [ ] コミット: `✨ Add .env.example to templates that read env vars`

## Step 4: Hono 系 4 テンプレに `app.onError`

- [ ] `ProjectFileBuilders.honoBindings()` に `onError` 追加
- [ ] Hono REST / Hono GraphQL / Full-Stack Server.res / Monorepo server Server.res に `app->Hono.onError(...)` 追加
- [ ] 各テストで onError 呼び出しの存在を検証
- [ ] コミット: `✨ Add global app.onError handler to Hono templates`

## Step 5: Hono 系 4 テンプレの route test 拡張

- [ ] Hono REST: `/api/health` or similar DB-free route を `app.request()` で叩く
- [ ] Hono GraphQL: `/` or `/graphql` (GET returns GraphiQL HTML) を `app.request()` で叩く
- [ ] Full-Stack: `/api/health` を `app.request()` で叩く
- [ ] Monorepo server: `/api/hello` を `app.request()` で叩く
- [ ] 各テストで `app.request` assertion 追加
- [ ] コミット: `✨ Upgrade Hono template tests to use app.request() route harness`

## Step 6: ドキュメント更新

- [ ] CLAUDE.md — wizard 記述を更新 (LICENSE / .nvmrc / dependabot / coverage / env.example に言及)
- [ ] README.md — 同様
- [ ] docs/product-requirements.md — 同様
- [ ] sphinx-docs/user/features/advanced.md — Quality of Life セクション拡充
- [ ] sphinx-docs/locale/ja/LC_MESSAGES/user/features/advanced.po — 対応する翻訳追加
- [ ] コミット: `📝 Document template polish additions (LICENSE, nvmrc, dependabot, coverage, env.example)`

## Step 7: 検証 + マージ

- [ ] `./gradlew ktlintCheck buildPlugin test` 成功
- [ ] `tasklist.md` を全て `[x]` に更新
- [ ] ユーザーにマージ可否確認
- [ ] main にマージ + ブランチ削除 + セッション終了
