# 要求内容: AWS Lambda テンプレートにローカル実行サポートを追加

## 背景

現在 Project Wizard の AWS Lambda テンプレート（`AwsLambdaTemplateFiles`）は `dev` script を持たず、`build` (rescript + esbuild) しか提供していない。一方で他の web 系テンプレート（Vite+React / Next.js / Hono / ResX / Cloudflare Workers / Full-Stack 等）はホットリロード付きの `dev` script を備えており、AWS Lambda だけが「ローカルで動かせない」状態になっている。

## 採用方針

ユーザー選択肢として 3 案（Hono を Node ローカル起動 / SAM CLI / Lambda RIE）を提示し、**1 案（Hono を `@hono/node-server` で Node サーバー起動）** が採用された。理由は最軽量で、テンプレートが既に Hono ベースであるため Lambda 固有の event/context を要さない API なら本番との差分が少ないこと。

## 受け入れ条件

- [ ] AWS Lambda テンプレートに `dev` script が追加され、`node --watch` で Hono アプリをローカル起動できる
- [ ] 既存 Lambda ハンドラ (`Server.res` の `let handler = HonoLambda.handle(app)`) は Lambda デプロイ用にそのまま温存される
- [ ] ローカル起動用エントリポイント（`src/Local.res`）は Server.res から `app` を再利用し、デプロイ用 esbuild バンドルには含まれない
- [ ] `@hono/node-server` は devDependency として追加される（Lambda バンドルに混入させない）
- [ ] README に「Local development」セクションが追加され、`dev` と `res:dev` を 2 ターミナルで起動する手順が示される
- [ ] 既存テスト（`AwsLambdaTemplateFilesTest`）は通り、新規機能に対するテストが追加される
- [ ] ktlint / buildPlugin / test がすべて通る
