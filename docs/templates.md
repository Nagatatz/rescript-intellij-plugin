# Project Wizard テンプレート一覧

Project Wizard が提供する 21 種類のプロジェクトテンプレートの開発者向け参照資料。ユーザー向けのスクリーンショット・カード表示・ガイドは [sphinx-docs/user/templates/index.md](../sphinx-docs/user/templates/index.md) を参照。

## テンプレート一覧

| # | 表示名 | カテゴリ | 用途 | 主な同梱要素 |
|---|---|---|---|---|
| 1 | Basic | Basic | Node.js 向け最小構成 | `Args`（argv parse）、`Files`（`node:fs/promises`） |
| 2 | npm Library | Library | npm 公開ライブラリ | Vitest、`fetchWithTimeout`、`pnpm publish` 対応 |
| 3 | CLI Tool | Tool | CLI ツール | サブコマンドディスパッチ（`greet`/`init`）、`Commands/` 構成 |
| 4 | Vite + React | Frontend | React SPA | Vite+、`useState`、`fetch('/api/greet')`、`src/Api.res` |
| 5 | Next.js | Frontend | SSR / App Router | Server Component + Client Component + Route Handler、`genType` |
| 6 | Electron | Desktop | デスクトップアプリ | `preload.cjs` + `contextBridge` + `ipcMain.handle` |
| 7 | React Native (Expo) | Mobile | Expo ベース RN アプリ | TODO リスト（`useState` + `FlatList` + `TextInput` + `Button`） |
| 8 | React Native (Community CLI) | Mobile | bare workflow RN | `android/` `ios/` ディレクトリ同梱、ネイティブコード直接編集可 |
| 9 | Hono (Node.js) | Backend | REST API | Drizzle(SQLite) + Zod + `@hono/zod-openapi` + Scalar UI at `/docs` |
| 10 | Hono GraphQL | Backend | GraphQL API | `graphql-yoga` + GraphiQL + Drizzle + `@graphql-markdown/cli` |
| 11 | Hono + Inertia (React) | Full Stack | server-driven SPA | `@hono/inertia` + `@inertiajs/react` v3 + Vite+ (`vp dev` / `vp build` / `vp test` / `vp check`)、サンプル Home / About、JS シム経由の page resolver |
| 12 | Cloudflare Workers | Serverless | エッジ関数 | `wrangler.jsonc`、KV バインディング（`src/Kv.res`） |
| 13 | AWS Lambda | Serverless | Lambda 関数 | API Gateway ハンドラ、DynamoDB レシピ同梱 |
| 14 | Google Cloud Run | Serverless | コンテナサービス | `Dockerfile`、Cloud SQL レシピ同梱 |
| 15 | Monorepo | Full Stack | pnpm/npm/yarn workspace | `packages/{shared,server,client}`、workspace protocol 型共有 |
| 16 | Full-Stack | Full Stack | 単一パッケージの統合構成 | `src/{shared,server,client}`、Hono + Drizzle + Vite+ React |
| 17 | res-x (HTMX on Bun) | Full Stack | Bun + Vite + HTMX サーバドリブン SPA | `rescript-x` + `rescript-bun`、Counter + Todo フォーム、`Validation.res` (zod/sury) |
| 18 | TanStack Start | Frontend | 型志向フルスタック React | `@tanstack/react-start` + `@tanstack/react-router`、Server Function サンプル、`app/` ソース |
| 19 | Remix / React Router v7 | Frontend | SSR + loader/action モデル | `@react-router/dev` Vite プラグイン、ReScript loader、`app/` ソース |
| 20 | Astro | Frontend | コンテンツ志向 SSG/SSR + React Islands | `@astrojs/react`、Static + Counter Island、`@astrojs/node` adapter |
| 21 | Waku | Frontend | RSC-first React フレームワーク | Server Component + `"use client"` TSX ラッパー越しの Client Component |

テンプレート定義は `src/main/kotlin/com/rescript/plugin/wizard/ProjectTemplate.kt`、ファイル生成は `wizard/templates/` 配下。テンプレート 18–21 はフレームワーク自体がデータレイヤーを持つため Validation library 選択 UI を表示せず (`supportsValidationSelection = false`)、`Validation.res` も生成しない。

## 全テンプレート共通の同梱要素

| 要素 | 内容 |
|---|---|
| パッケージマネージャ | npm / pnpm / yarn から選択可（デフォルト **pnpm**）。`package.json` の `packageManager` フィールドと README コマンドに反映 |
| テスト | Vitest スモークテスト（`test` + `test:coverage` スクリプト、`__tests__/*.test.mjs`） |
| ライセンス | `LICENSE`（MIT） |
| Node.js バージョン | `.nvmrc` |
| GitHub Actions | `.github/dependabot.yml`、CI ワークフロー |

## グループ別の追加同梱要素

| グループ | 追加要素 |
|---|---|
| Hono 系 テンプレ（9, 10, 11, 12, 15 backend, 16 backend） | `app.onError` グローバルエラーハンドラ、`app.request()` ベースのルートテスト |
| DB/PORT を使う テンプレ（9, 10, 11, 15, 16） | `.env.example` |
| Monorepo | workspace 横断テスト (`pnpm -r run test` 等) |
| React Native 2 テンプレ（7, 8） | filesystem smoke test（JS ランタイム非依存） |
| Hono + Inertia (11) | `@hono/inertia` ミドルウェア配線、`InertiaBindings.res` (Link/usePage/createInertiaApp)、`pages.js` シム (`import.meta.glob`) |
| res-x（17） | `jsx.module=Hjsx` / `-open ResX.Globals` を `rescript.json` にプリ配線、HTMX CDN スクリプト |

## バージョン管理

依存パッケージバージョンは `src/main/kotlin/com/rescript/plugin/wizard/templates/TemplateVersions.kt` で一元管理する。テンプレート側で個別にバージョンをハードコードしないこと。

## 統合テスト

- `./gradlew integrationTest` — 全テンプレートを生成 → `pnpm install` → `rescript build` で動作確認
- `.github/workflows/integration-tests.yml` — nightly + manual で実行

## Vite+ (pre-1.0) 採用部分の注意点

Vite+ ベースのテンプレート（4 Vite + React、11 Hono + Inertia、15 Monorepo の client、16 Full-Stack の client）は `vite-plus` と `@voidzero-dev/vite-plus-core` を pre-1.0 バージョンでピン留めしている。アップグレードでコマンドが壊れた場合は、`vite.config.mjs` で `vite-plus` を `vite` に差し替え、`vp` スクリプトを `vite` に置換することでクラシック Vite にフォールバックできる（README にも記載）。テンプレート 18 (TanStack Start) と 19 (Remix RR v7) は標準の `vite` を直接利用し、Vite+ には依存しない。

## 新規テンプレート追加手順

1. `ProjectTemplate.kt` に enum エントリを追加
2. `wizard/templates/<Name>TemplateFiles.kt` を作成しファイル生成を実装
3. `TemplateVersions.kt` に必要な依存バージョンを集約
4. 本ドキュメント・`sphinx-docs/user/templates/index.md`・`CLAUDE.md` のテンプレート一覧を更新
5. `integrationTest` を追加し nightly CI で動作確認
