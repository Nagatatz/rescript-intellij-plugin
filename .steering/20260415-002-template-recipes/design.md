# 設計: テンプレート実用度向上 + ドキュメント拡充

## 全体方針

20260415-001 で確立した基盤 (`TemplateContext`, `TemplateVersions`, `CommonFiles`) はそのまま活用。新規依存バージョンは `TemplateVersions.kt` に追加し、各テンプレートの `generate(ctx)` を実用例付きに書き換える。新規 2 テンプレート (Hono GraphQL / Full-Stack) は既存ファイル構造に従って `*TemplateFiles.kt` を追加する。README は各テンプレ内で関数化して長文を抽出。sphinx-docs は新規 `templates/` セクションを追加。

## 1. TemplateVersions.kt 拡張

新規定数追加:

```kotlin
// Database
const val LIBSQL_CLIENT = "^0.14.0"
const val DRIZZLE_ORM = "^0.36.0"
const val DRIZZLE_KIT = "^0.28.0"

// Validation
const val ZOD = "^3.23.0"

// OpenAPI / Hono RPC ecosystem
const val HONO_ZOD_OPENAPI = "^0.18.0"
const val SCALAR_HONO_API_REFERENCE = "^0.5.0"

// GraphQL
const val GRAPHQL = "^16.9.0"
const val GRAPHQL_YOGA = "^5.7.0"
const val GRAPHQL_MARKDOWN = "^7.0.0"

// Cloudflare Workers KV / D1 (ドキュメント目的、依存追加なし)

// AWS Lambda types
const val AWS_LAMBDA = "^1.0.0" // (npm: aws-lambda)
```

## 2. ProjectTemplate enum 拡張

```kotlin
enum class TemplateCategory(...) {
    BASIC, FRONTEND, DESKTOP, BACKEND, SERVERLESS, MOBILE, LIBRARY, TOOL, FULL_STACK,
}

enum class ProjectTemplate(...) {
    BASIC,
    VITE_REACT,
    NEXTJS,
    ELECTRON,
    HONO,             // 拡張: SQLite + Drizzle + OpenAPI
    HONO_GRAPHQL,     // NEW: yoga + GraphiQL + Drizzle
    CLOUDFLARE_WORKERS,
    AWS_LAMBDA,
    GOOGLE_CLOUD_RUN,
    REACT_NATIVE,
    NPM_LIBRARY,
    CLI_TOOL,
    MONOREPO,
    FULL_STACK,       // NEW: 単一パッケージ Hono + Vite+React + Drizzle
}
```

カテゴリ `BACKEND` に Hono GraphQL を追加、`FULL_STACK` に新規テンプレを追加 (Monorepo は既に同カテゴリ)。

## 3. 共通: SharedHonoBindings の拡張

`ProjectFileBuilders.honoBindings()` を強化し、POST body 受信・パラメータ・ミドルウェアを含むフルセットを提供:

```rescript
// Hono.res (生成例)
type app
type context
type request
type next = unit => promise<unit>
type middleware = (context, next) => promise<unit>

@module("hono") @new external createApp: unit => app = "Hono"

// Routing
@send external get: (app, string, context => 'a) => unit = "get"
@send external post: (app, string, context => 'a) => unit = "post"
@send external put: (app, string, context => 'a) => unit = "put"
@send external delete: (app, string, context => 'a) => unit = "delete"

// Middleware
@send external use: (app, middleware) => unit = "use"

// Request
@get external req: context => request = "req"
@send external paramAt: (request, string) => string = "param"
@send external jsonBody: request => promise<'a> = "json"
@send external query: (request, string) => option<string> = "query"

// Response
@send external text: (context, string) => 'a = "text"
@send external json: (context, 'a) => 'b = "json"
@send external statusCode: (context, int) => context = "status"
```

ミドルウェア例 (logger):

```rescript
// Logger.res
@module("hono/logger") external logger: unit => Hono.middleware = "logger"
```

## 4. Phase 1 個別テンプレ: 設計詳細

### Basic
追加: `src/Files.res` で `fs/promises` バインド、`src/Cli.res` で CLI 引数受け取り、ファイル read/write デモ。

### Vite + React
```
src/
├── App.res          ← useState + form + fetch
├── Main.res
├── Api.res          ← @val external fetch + 型注釈
└── __tests__/App.test.mjs
```
form: name 入力 → submit → mock API (`/api/echo`) を fetch (template 単体では echo を mock)。

### Next.js
```
src/app/
├── page.res         ← Server component (DB から users 取得)
├── client/
│   └── UserForm.res ← @react.component (Client component, useState + form)
└── api/users/route.res  ← Route handler (POST)
```

### Electron
```
main.cjs              ← BrowserWindow + IPC handler
preload.cjs           ← contextBridge.exposeInMainWorld
src/App.res           ← useState + window.electronAPI.ping() 呼び出し
src/Electron.res      ← preload で expose した API のバインディング
```

### Hono (拡張)
```
src/
├── Server.res             ← app セットアップ + ミドルウェア
├── Hono.res               ← Hono バインディング (拡張版)
├── HonoNodeServer.res
├── Logger.res             ← @hono/logger バインディング
├── ZodOpenapi.res         ← @hono/zod-openapi バインディング
├── Scalar.res             ← @scalar/hono-api-reference バインディング
├── Db.res                 ← drizzle + libsql バインディング、users テーブル schema
├── Schema.res             ← Zod スキーマ定義
└── Routes/
    ├── Users.res          ← CRUD ハンドラ (POST/GET/PUT/DELETE)
    └── Health.res         ← /health
drizzle.config.ts          ← drizzle-kit 設定
data/                      ← .gitignore に追加 (SQLite ファイル置き場)
```

OpenAPI/Scalar 統合:
```rescript
// Server.res
let app = ZodOpenapi.createApp()
app->Hono.use(Logger.logger())
app->Routes.Users.register
app->ZodOpenapi.docRoute("/openapi.json", {info: {title: "API", version: "0.1.0"}})
app->Hono.get("/docs", Scalar.apiReference({url: "/openapi.json"}))
```

### Hono GraphQL (新規)
```
src/
├── Server.res             ← Hono + yoga マウント
├── Hono.res
├── HonoNodeServer.res
├── Yoga.res               ← graphql-yoga バインディング
├── Schema.res             ← GraphQL schema (SDL を文字列リテラルで)
├── Resolvers/
│   └── Users.res          ← Query/Mutation resolvers
└── Db.res                 ← drizzle + libsql + users テーブル
drizzle.config.ts
scripts/generate-docs.mjs  ← graphql-markdown 実行スクリプト
docs/                      ← 自動生成された schema.md 置き場 (.gitignore で除外しない)
```

### Cloudflare Workers
```
src/
├── Server.res
├── Hono.res
└── Kv.res                 ← KV namespace バインディング (env.MY_KV)
wrangler.jsonc             ← KV namespace bindings 例 (コメントで)
```

### AWS Lambda
```
src/
├── Server.res
├── Hono.res
├── HonoLambda.res
└── DynamoDb.res           ← DynamoDB クライアントバインディング (使い方 README に)
```

### Cloud Run
- 環境変数読み取り例 `Process.env`
- README に Cloud SQL 接続例

### React Native
```
src/
├── App.res                ← useState + FlatList + Button
├── ReactNative.res        ← View/Text/Button/FlatList バインディング
└── Todo.res               ← サンプル model
```

### npm Library
```
src/
├── Index.res              ← greet (sync) + greetAsync (async) + List ヘルパー
└── __tests__/
    ├── Index.test.mjs
    └── List.test.mjs
```

### CLI Tool
```
src/
├── Cli.res                ← サブコマンド dispatcher
├── Commands/
│   ├── Greet.res
│   └── Init.res
└── Args.res               ← フラグパース ヘルパー
```

### Monorepo (拡張)
- `packages/server/` に Drizzle + libsql + Routes/Users.res 追加
- `packages/client/` に form + fetch 例追加
- `packages/shared/Api.res` で Request/Response 型を定義
- `packages/server/drizzle.config.ts`

### Full-Stack (新規)
```
single-package/
├── package.json
├── rescript.json          ← sources: [src/server, src/client, src/shared]
├── vite.config.mjs        ← Vite+ + react()
├── drizzle.config.ts
├── data/                  ← SQLite ファイル
└── src/
    ├── shared/
    │   └── Api.res        ← Request/Response 型
    ├── server/
    │   ├── Main.res       ← サーバー起動エントリ
    │   ├── Server.res
    │   ├── Db.res
    │   └── Routes/
    │       └── Users.res
    └── client/
        ├── Main.res       ← React マウント
        ├── App.res        ← form + fetch
        └── Api.res        ← fetch wrapper (shared 型)
```

`pnpm dev` は `concurrently "node --watch src/server/Main.res.mjs" "vp dev"` で server + client を同時起動。Vite+ の `proxy` で `/api/*` を server に転送。

## 5. README 拡充戦略

各テンプレに `readmeExtended()` プライベート関数を追加し、`CommonFiles.readme` の `extraSections` に渡す。標準セクション:

```
1. Architecture
   - Mermaid 図 (フォルダ構造 + データフロー)
2. Project Layout
   - 主要ファイル解説 (テーブル形式)
3. Recipes
   - "次にやりたいこと" 集 (3〜5 個)
4. Troubleshooting
   - よくあるエラーと解決策
5. Related Resources
   - 公式ドキュメントリンク
```

Mermaid 図は GitHub README で自動レンダリングされる。

## 6. sphinx-docs 構造

```
sphinx-docs/user/
├── templates/                       ← NEW
│   ├── index.md                     ← カタログ + 比較表
│   ├── basic.md
│   ├── vite-react.md
│   ├── nextjs.md
│   ├── electron.md
│   ├── hono.md
│   ├── hono-graphql.md              ← NEW
│   ├── cloudflare-workers.md
│   ├── aws-lambda.md
│   ├── google-cloud-run.md
│   ├── react-native.md
│   ├── npm-library.md
│   ├── cli-tool.md
│   ├── monorepo.md
│   └── full-stack.md                ← NEW
└── recipes/
    ├── add-hono-endpoint.md         ← NEW
    ├── add-graphql-resolver.md      ← NEW
    ├── setup-drizzle.md             ← NEW
    └── add-openapi-docs.md          ← NEW
```

`templates/index.md` には:
- 14 テンプレ一覧 (カテゴリ別)
- 比較表 (規模 / 推奨ユースケース / 含まれる機能)
- 「どれを選ぶか」フローチャート

各 `templates/<name>.md`:
- 概要
- アーキテクチャ
- 含まれるファイル
- セットアップ手順
- 主要なコード片の説明
- 関連レシピへのリンク

## 7. 統合テスト拡張

`TemplateIntegrationTest.kt`:
- 新規 2 テンプレ (HONO_GRAPHQL, FULL_STACK) も `EnumSource(ProjectTemplate::class)` で自動カバー
- HONO / HONO_GRAPHQL / FULL_STACK は `pnpm exec drizzle-kit generate` も実行 (マイグレーション生成テスト)
- HONO_GRAPHQL は `pnpm docs:graphql` も実行

## 8. 実装順序 (コミット粒度)

1. `TemplateVersions` 拡張 + `ProjectFileBuilders.honoBindings()` 拡張 + 新規ヘルパー
2. **Phase 1.1** Basic / Library / CLI 強化
3. **Phase 1.2** Vite+React / Electron 強化
4. **Phase 1.3** Next.js / React Native 強化
5. **Phase 1.4** Cloudflare Workers / AWS Lambda / Cloud Run 強化
6. **Phase 1.5** Hono REST 拡張 (SQLite + Drizzle + OpenAPI + Scalar)
7. **Phase 2.1** Hono GraphQL 新規追加
8. **Phase 1.6 + 2.2** Monorepo 強化 + Full-Stack 新規追加
9. **Phase 5.1** sphinx-docs templates/index.md + 14 個別ページ
10. **Phase 5.2** sphinx-docs recipes 4 ファイル追加
11. **Doc 更新** CLAUDE.md / README.md / product-requirements.md
12. **DoD 検証 + マージ確認**

計 12 コミット予定。

## 9. リスクと緩和策

| リスク | 緩和策 |
|--------|--------|
| Hono GraphQL の yoga バージョン互換性 | TemplateVersions に固定 + 統合テストで毎晩検証 |
| Drizzle migrations が CI で実行できない | `drizzle-kit generate` のみテスト (apply は除外) |
| Full-Stack の `vp dev` + `node --watch` 同時起動が CI で困難 | install + build まで検証、dev は手動確認 |
| README が長すぎる | 関数化で `*TemplateFiles.kt` のサイズを抑制、テストで構造を検証 |
| sphinx-docs ビルドが重くなる | 1 ファイル 200〜400 行を目安に |
| Vite+ pre-1.0 の継続的非互換 | Hono 系 / Library / CLI は影響なし。React 系のみ既存対応 (build スキップ) を継続 |
