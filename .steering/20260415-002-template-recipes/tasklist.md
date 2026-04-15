# タスクリスト: テンプレート実用度向上 + ドキュメント拡充

## Step 1: 基盤拡張

- [x] `TemplateVersions.kt` に DB / GraphQL / OpenAPI / validation 関連定数追加
- [x] `ProjectFileBuilders.honoBindings()` に request/middleware/status バインディング追加
- [x] `ProjectTemplate` に `HONO_GRAPHQL`, `FULL_STACK` enum エントリ追加
- [x] 新規テンプレ用 `HonoGraphqlTemplateFiles.kt` / `FullStackTemplateFiles.kt` のスタブ追加 (空 generate)
- [x] テスト更新
- [x] コミット: `✨ Extend foundation for richer templates (versions, Hono bindings, new template stubs)`

## Step 2: Phase 1.1 — Basic / Library / CLI

- [x] BASIC: Files.res / Cli.res 追加 (fs/promises バインド + CLI 引数)
- [x] NPM_LIBRARY: 複数関数 (sync + async) + 詳細テスト
- [x] CLI_TOOL: サブコマンド dispatcher + Args.res
- [x] テスト更新
- [x] コミット: `✨ Enrich Basic, Library, and CLI templates with practical examples`

## Step 3: Phase 1.2 — Vite+React / Electron

- [x] VITE_REACT: useState フォーム + fetch API デモ
- [x] ELECTRON: IPC (preload + main + renderer)
- [x] テスト更新
- [x] コミット: `✨ Add interactive examples to Vite+React and Electron templates`

## Step 4: Phase 1.3 — Next.js / React Native

- [x] NEXTJS: Server Component + Client Component + Route Handler (POST)
- [x] REACT_NATIVE: useState + FlatList + Button
- [x] テスト更新
- [x] コミット: `✨ Add full-stack patterns to Next.js and interactive UI to React Native`

## Step 5: Phase 1.4 — Serverless 系 (CF Workers / Lambda / Cloud Run)

- [x] CLOUDFLARE_WORKERS: POST + KV ストア + wrangler.jsonc bindings 例
- [x] AWS_LAMBDA: POST + APIGateway イベント型 + DynamoDB README 例
- [x] GOOGLE_CLOUD_RUN: POST + 環境変数 + Cloud SQL README 例
- [x] テスト更新
- [x] コミット: `✨ Add POST/storage examples to CF Workers, AWS Lambda, Cloud Run`

## Step 6: Phase 1.5 — Hono REST 拡張

- [x] HONO: SQLite + Drizzle + drizzle-kit
- [x] HONO: ロガーミドルウェア + エラーハンドリング
- [x] HONO: CRUD ハンドラ (POST/GET/PUT/DELETE) + ID パラメータ
- [x] HONO: `@hono/zod-openapi` で OpenAPI 仕様自動生成
- [x] HONO: `@scalar/hono-api-reference` で `/docs` ホスト
- [x] HONO: data/ を .gitignore 追加
- [x] HonoTemplateFilesTest 更新
- [x] コミット: `✨ Upgrade Hono template with SQLite/Drizzle and OpenAPI/Scalar UI`

## Step 7: Phase 2.1 — Hono GraphQL (新規テンプレート)

- [x] HonoGraphqlTemplateFiles.kt 実装
- [x] graphql-yoga + GraphiQL マウント
- [x] SQLite + Drizzle + users CRUD リゾルバ
- [x] graphql-markdown スクリプト + `pnpm docs:graphql`
- [x] HonoGraphqlTemplateFilesTest.kt 新規追加
- [x] ProjectTemplateTest 更新 (13 → 14 entries は次ステップで)
- [x] コミット: `✨ Add Hono GraphQL template (yoga + GraphiQL + Drizzle)`

## Step 8: Phase 1.6 + 2.2 — Monorepo 拡張 + Full-Stack 新規

- [x] MONOREPO: server に Drizzle + libsql + CRUD 追加
- [x] MONOREPO: client に form + fetch 追加
- [x] MONOREPO: shared/Api.res で Request/Response 型定義
- [x] FULL_STACK: 新規テンプレート完全実装
- [x] FullStackTemplateFilesTest.kt 新規追加
- [x] MonorepoTemplateFilesTest 更新
- [x] ProjectTemplateTest 更新 (12 → 14 entries)
- [x] コミット: `✨ Enhance Monorepo and add Full-Stack template (single package)`

## Step 9: README 大幅拡充

- [x] 各テンプレに `readmeExtended()` ヘルパー追加 (Architecture/Layout/Recipes/Troubleshooting/Resources)
- [x] Mermaid 図を含む
- [x] テスト追加: `*TemplateFilesTest` で README が必要なセクションを含むことを検証
- [x] コミット: `📝 Expand template READMEs with architecture, recipes, and troubleshooting`

## Step 10: sphinx-docs templates/ セクション

- [x] `sphinx-docs/user/templates/index.md` (カタログ + 比較表)
- [x] 14 個別テンプレページ (`basic.md` 〜 `full-stack.md`)
- [x] toctree 更新
- [x] コミット: `📝 Add per-template guide pages to sphinx-docs`

## Step 11: sphinx-docs recipes 拡充

- [x] `recipes/add-hono-endpoint.md`
- [x] `recipes/add-graphql-resolver.md`
- [x] `recipes/setup-drizzle.md`
- [x] `recipes/add-openapi-docs.md`
- [x] recipes/index.md と toctree 更新
- [x] コミット: `📝 Add four new recipes for Hono, GraphQL, Drizzle, and OpenAPI`

## Step 12: ドキュメント同期

- [x] CLAUDE.md レイヤー 3 のテンプレート数を 12 → 14 に更新、新機能を反映
- [x] README.md Features セクション更新
- [x] docs/product-requirements.md の Project Wizard 行を更新
- [x] sphinx-docs/user/features/advanced.md の Project Wizard セクション更新
- [x] コミット: `📝 Update top-level docs for 14 templates and richer template content`

## Step 13: 統合テスト拡張 + 検証

- [x] `TemplateIntegrationTest` で 14 テンプレ全件パスを確認
- [x] HONO / HONO_GRAPHQL / FULL_STACK で `drizzle-kit generate` 実行
- [x] HONO_GRAPHQL で `pnpm docs:graphql` 実行
- [x] `./gradlew ktlintCheck buildPlugin checkKdoc test` 成功
- [x] CI/integration-tests.yml は変更不要 (新規テンプレも自動カバー)

## Step 14: マージ

- [x] tasklist.md 全タスク [x] 確認
- [x] AskUserQuestion でマージ可否確認
- [x] worktree から main へマージ
- [x] worktree クリーンアップ + セッション終了

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
