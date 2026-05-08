# 設計: AWS Lambda テンプレート ローカル実行

## アーキテクチャ概要

Lambda 用の `Server.res` は今のまま「Hono app + Lambda handler の export」を担う。新規に `src/Local.res` を追加し、`Server.app` を `@hono/node-server` で port 3000 にバインドするローカル専用エントリポイントとする。esbuild の bundle 対象は `src/Server.res.mjs` のままなので Local.res は本番アーティファクトに含まれない。

```
src/
├── Server.res        # Hono app + Lambda handler (本番 entry, esbuild bundle 対象)
├── HonoLambda.res    # hono/aws-lambda 用 binding
├── HonoNodeServer.res  # ★新規: @hono/node-server 用 binding (Hono テンプレートと同じ生成器を再利用)
├── Local.res         # ★新規: ローカル起動 entry (node --watch 対象)
└── Validation.res
```

## ファイル変更内容

### 1. `AwsLambdaTemplateFiles.kt`

- `awsLambdaDependencies` の dev 候補を分けるため、`@hono/node-server` を `devDependencies` に追加
- `scripts` に追加:
  - `dev`: `node --watch src/Local.res.mjs` （Hono テンプレートと同パターン）
  - `start`: `node src/Local.res.mjs` （単発起動・Hono と同じく追加）
- `files` map に追加:
  - `src/HonoNodeServer.res` → `ProjectFileBuilders.honoNodeServerBindings()` （既存ヘルパー再利用）
  - `src/Local.res` → 新規リソース `aws-lambda/src/Local.res`
- README:
  - `scripts` 列に `dev` / `start` の説明を追加
  - `extraSections` に `"Local development"` を追加（新規 `aws-lambda/readme/local.md`）

### 2. 新規リソース `src/main/resources/templates/aws-lambda/src/Local.res`

```rescript
// Local development entry point. Starts a Node HTTP server using
// `@hono/node-server` so the same `Server.app` can be exercised without
// deploying to Lambda. Unused by the esbuild bundle that ships to AWS —
// only `Server.res.mjs` is bundled, so this file (and its dev-only
// dependency `@hono/node-server`) never reach the Lambda artifact.
let port = 3000
HonoNodeServer.serve({fetch: Server.app->HonoNodeServer.honoFetch, port})
Console.log("Local Lambda preview on http://localhost:" ++ Int.toString(port))
```

### 3. 新規リソース `src/main/resources/templates/aws-lambda/readme/local.md`

`dev` / `res:dev` を 2 ターミナルで動かす手順、`http://localhost:3000` のエンドポイント例、Lambda 固有の event/context（`requestContext` 等）はローカル時には未提供である旨を記載する。

### 4. `AwsLambdaTemplateFilesTest.kt`

追加するテスト:
- `dev` script が存在し `node --watch src/Local.res.mjs` を含む
- `start` script が存在する
- `@hono/node-server` が devDependencies に含まれ、依存（`dependencies`）には含まれない（バンドルに入らないことを保証）
- `src/Local.res` と `src/HonoNodeServer.res` がファイル一覧に存在する
- `Local.res` が `Server.app` を参照し `HonoNodeServer.serve` を呼ぶ
- README に "Local development" セクションが含まれる

## 設計判断の根拠

### Server.res は変更しない
既存 `Server.res` は `let app = ...` と `let handler = HonoLambda.handle(app)` を export している。`Local.res` から `Server.app` をそのまま参照できるので変更不要。

### `@hono/node-server` を devDependency にする理由
esbuild の bundle 入口は `src/Server.res.mjs`。Server.res は HonoNodeServer を import しないため、Lambda バンドルには含まれない。ローカル起動でしか使わないので devDep が正しい分類。これにより Lambda アーティファクトサイズに影響しない。

### `concurrently` を使わず `dev` と `res:dev` を分ける
Hono テンプレート（`HonoTemplateFiles`）の規約と一致させる。ResX 式の `concurrently` 同梱は依存を増やすので避け、ユーザーは 2 ターミナル運用にする（README で明示）。

### Lambda event は再現しない
`hono/aws-lambda` adapter は `event.requestContext` 等を Hono の `c.env` に流し込むが、Node ローカル起動ではこれらは undefined になる。アプリ層で `c.req`/`c.json` 中心の API を組んでいる限り差は出ない。差が問題になるユースケースは README で SAM CLI / RIE への誘導文を入れる。
