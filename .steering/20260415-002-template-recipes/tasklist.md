# タスクリスト: テンプレート実用度向上 + ドキュメント拡充

## Step 1: 基盤拡張

- [ ] `TemplateVersions.kt` に DB / GraphQL / OpenAPI / validation 関連定数追加
- [ ] `ProjectFileBuilders.honoBindings()` に request/middleware/status バインディング追加
- [ ] `ProjectTemplate` に `HONO_GRAPHQL`, `FULL_STACK` enum エントリ追加
- [ ] 新規テンプレ用 `HonoGraphqlTemplateFiles.kt` / `FullStackTemplateFiles.kt` のスタブ追加 (空 generate)
- [ ] テスト更新
- [ ] コミット: `✨ Extend foundation for richer templates (versions, Hono bindings, new template stubs)`

## Step 2: Phase 1.1 — Basic / Library / CLI

- [ ] BASIC: Files.res / Cli.res 追加 (fs/promises バインド + CLI 引数)
- [ ] NPM_LIBRARY: 複数関数 (sync + async) + 詳細テスト
- [ ] CLI_TOOL: サブコマンド dispatcher + Args.res
- [ ] テスト更新
- [ ] コミット: `✨ Enrich Basic, Library, and CLI templates with practical examples`

## Step 3: Phase 1.2 — Vite+React / Electron

- [ ] VITE_REACT: useState フォーム + fetch API デモ
- [ ] ELECTRON: IPC (preload + main + renderer)
- [ ] テスト更新
- [ ] コミット: `✨ Add interactive examples to Vite+React and Electron templates`

## Step 4: Phase 1.3 — Next.js / React Native

- [ ] NEXTJS: Server Component + Client Component + Route Handler (POST)
- [ ] REACT_NATIVE: useState + FlatList + Button
- [ ] テスト更新
- [ ] コミット: `✨ Add full-stack patterns to Next.js and interactive UI to React Native`

## Step 5: Phase 1.4 — Serverless 系 (CF Workers / Lambda / Cloud Run)

- [ ] CLOUDFLARE_WORKERS: POST + KV ストア + wrangler.jsonc bindings 例
- [ ] AWS_LAMBDA: POST + APIGateway イベント型 + DynamoDB README 例
- [ ] GOOGLE_CLOUD_RUN: POST + 環境変数 + Cloud SQL README 例
- [ ] テスト更新
- [ ] コミット: `✨ Add POST/storage examples to CF Workers, AWS Lambda, Cloud Run`

## Step 6: Phase 1.5 — Hono REST 拡張

- [ ] HONO: SQLite + Drizzle + drizzle-kit
- [ ] HONO: ロガーミドルウェア + エラーハンドリング
- [ ] HONO: CRUD ハンドラ (POST/GET/PUT/DELETE) + ID パラメータ
- [ ] HONO: `@hono/zod-openapi` で OpenAPI 仕様自動生成
- [ ] HONO: `@scalar/hono-api-reference` で `/docs` ホスト
- [ ] HONO: data/ を .gitignore 追加
- [ ] HonoTemplateFilesTest 更新
- [ ] コミット: `✨ Upgrade Hono template with SQLite/Drizzle and OpenAPI/Scalar UI`

## Step 7: Phase 2.1 — Hono GraphQL (新規テンプレート)

- [ ] HonoGraphqlTemplateFiles.kt 実装
- [ ] graphql-yoga + GraphiQL マウント
- [ ] SQLite + Drizzle + users CRUD リゾルバ
- [ ] graphql-markdown スクリプト + `pnpm docs:graphql`
- [ ] HonoGraphqlTemplateFilesTest.kt 新規追加
- [ ] ProjectTemplateTest 更新 (13 → 14 entries は次ステップで)
- [ ] コミット: `✨ Add Hono GraphQL template (yoga + GraphiQL + Drizzle)`

## Step 8: Phase 1.6 + 2.2 — Monorepo 拡張 + Full-Stack 新規

- [ ] MONOREPO: server に Drizzle + libsql + CRUD 追加
- [ ] MONOREPO: client に form + fetch 追加
- [ ] MONOREPO: shared/Api.res で Request/Response 型定義
- [ ] FULL_STACK: 新規テンプレート完全実装
- [ ] FullStackTemplateFilesTest.kt 新規追加
- [ ] MonorepoTemplateFilesTest 更新
- [ ] ProjectTemplateTest 更新 (12 → 14 entries)
- [ ] コミット: `✨ Enhance Monorepo and add Full-Stack template (single package)`

## Step 9: README 大幅拡充

- [ ] 各テンプレに `readmeExtended()` ヘルパー追加 (Architecture/Layout/Recipes/Troubleshooting/Resources)
- [ ] Mermaid 図を含む
- [ ] テスト追加: `*TemplateFilesTest` で README が必要なセクションを含むことを検証
- [ ] コミット: `📝 Expand template READMEs with architecture, recipes, and troubleshooting`

## Step 10: sphinx-docs templates/ セクション

- [ ] `sphinx-docs/user/templates/index.md` (カタログ + 比較表)
- [ ] 14 個別テンプレページ (`basic.md` 〜 `full-stack.md`)
- [ ] toctree 更新
- [ ] コミット: `📝 Add per-template guide pages to sphinx-docs`

## Step 11: sphinx-docs recipes 拡充

- [ ] `recipes/add-hono-endpoint.md`
- [ ] `recipes/add-graphql-resolver.md`
- [ ] `recipes/setup-drizzle.md`
- [ ] `recipes/add-openapi-docs.md`
- [ ] recipes/index.md と toctree 更新
- [ ] コミット: `📝 Add four new recipes for Hono, GraphQL, Drizzle, and OpenAPI`

## Step 12: ドキュメント同期

- [ ] CLAUDE.md レイヤー 3 のテンプレート数を 12 → 14 に更新、新機能を反映
- [ ] README.md Features セクション更新
- [ ] docs/product-requirements.md の Project Wizard 行を更新
- [ ] sphinx-docs/user/features/advanced.md の Project Wizard セクション更新
- [ ] コミット: `📝 Update top-level docs for 14 templates and richer template content`

## Step 13: 統合テスト拡張 + 検証

- [ ] `TemplateIntegrationTest` で 14 テンプレ全件パスを確認
- [ ] HONO / HONO_GRAPHQL / FULL_STACK で `drizzle-kit generate` 実行
- [ ] HONO_GRAPHQL で `pnpm docs:graphql` 実行
- [ ] `./gradlew ktlintCheck buildPlugin checkKdoc test` 成功
- [ ] CI/integration-tests.yml は変更不要 (新規テンプレも自動カバー)

## Step 14: マージ

- [ ] tasklist.md 全タスク [x] 確認
- [ ] AskUserQuestion でマージ可否確認
- [ ] worktree から main へマージ
- [ ] worktree クリーンアップ + セッション終了

---

## コミット粒度まとめ

計 12 コミット予定:
1. 基盤拡張
2. Basic/Library/CLI 実用化
3. Vite+React/Electron 実用化
4. Next.js/React Native 実用化
5. Serverless 3 種実用化
6. Hono REST + SQLite + OpenAPI
7. Hono GraphQL 新規
8. Monorepo 拡張 + Full-Stack 新規
9. README 拡充
10. sphinx-docs templates/
11. sphinx-docs recipes 4 個
12. トップレベルドキュメント更新

## テスト免除対象

- 新規追加クラス全てに `*Test.kt` を作成 (テンプレートファイル生成は純粋関数なのでテスト容易)
- 例外なし
