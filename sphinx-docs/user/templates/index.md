---
myst:
  html_meta:
    "keywords": "templates, project wizard, scaffolding, hono, graphql, react, drizzle, monorepo, full-stack"
---

# Project Templates

The plugin ships 15 project templates that scaffold production-shaped ReScript apps — not just "Hello World". Each template is a working, buildable project with modern tooling (pnpm/npm/yarn support, ESM, Vite+ where applicable, GitHub Actions CI) and documentation that answers the common day-two question: *"How do I add the next thing?"*

## Opening the Wizard

1. **File → New → Project…** in IntelliJ IDEA
2. Select **ReScript** from the list on the left
3. Pick a template and a package manager
4. Click **Create**

You can also launch `npm create rescript@latest` from the terminal to get the same set of templates without the IDE.

## Category: Basic

::::{grid} 1 1 2 2
:gutter: 3

:::{grid-item-card} Basic
Minimal ReScript + Node.js starter with `Args` (argv parsing) and `Files` (`node:fs/promises`) modules. Useful as the smallest working reference for a non-browser ReScript project.

{bdg-info}`Node.js`
:::

:::{grid-item-card} npm Library
Split-module library (`Index`, `ListUtils`, `Fetcher`) with per-module Vitest suites and `fetchWithTimeout` (AbortController). Publish-ready to npm via `pnpm publish`.

{bdg-info}`Node.js` {bdg-success}`Testing`
:::

:::{grid-item-card} CLI Tool
Subcommand dispatcher (`greet`, `init`) with its own `Args` parser and a `Commands/` directory. Demonstrates how to grow a CLI without a heavyweight framework.

{bdg-info}`Node.js`
:::
::::

## Category: Frontend

::::{grid} 1 1 2 2
:gutter: 3

:::{grid-item-card} Vite+ + React
Single-page React app using [Vite+](https://vite.plus) with a form, `useState`, and a `fetch('/api/greet')` round-trip (offline fallback included). Includes `src/Api.res` as a reusable fetch wrapper.

{bdg-warning}`Vite+ pre-1.0`
:::

:::{grid-item-card} Next.js
App router with a Server Component (`app/page.tsx`), a Client Component (`app/client/GreetForm.tsx`), and a Route Handler (`app/api/greet/route.ts`). ReScript components are exposed via `genType`.

{bdg-primary}`genType`
:::

:::{grid-item-card} Electron
Desktop app with `preload.cjs` + `contextBridge` + `ipcMain.handle`. `src/Electron.res` binds the exposed `electronAPI` so renderer-process ReScript code can query system info.

{bdg-info}`Desktop`
:::

:::{grid-item-card} React Native
Expo-based app with an interactive todo list (`useState` + `TextInput` + `Button` + `FlatList`). `src/ReactNative.res` wraps the core RN component set. Extend the bindings to add more.

{bdg-info}`Mobile`
:::
::::

## Category: Backend

::::{grid} 1 1 2 2
:gutter: 3

:::{grid-item-card} Hono (REST)
:link: ../recipes/add-hono-endpoint
:link-type: doc
Hono + Drizzle (SQLite) + Zod + `@hono/zod-openapi` with Scalar UI at `/docs`. Ships a complete users CRUD (`src/Routes/Users.res`), migrations via `drizzle-kit`, and an `/openapi.json` spec.

{bdg-success}`REST` {bdg-primary}`OpenAPI` {bdg-info}`SQLite`
:::

:::{grid-item-card} Hono + GraphQL
:link: ../recipes/add-graphql-resolver
:link-type: doc
Hono hosting `graphql-yoga` at `/graphql` with GraphiQL built in. SDL lives in `src/schema.graphql`, resolvers in `src/Resolvers/Users.res`, storage in Drizzle. Run `pnpm docs:graphql` for human-readable docs.

{bdg-success}`GraphQL` {bdg-info}`SQLite`
:::
::::

## Category: Serverless

::::{grid} 1 1 2 2
:gutter: 3

:::{grid-item-card} Cloudflare Workers
`wrangler.jsonc` + a KV-backed greetings endpoint (POST/GET) using the `GREETINGS` binding. `src/Kv.res` binds the KV API so you can extend to Durable Objects or R2.

{bdg-info}`Edge`
:::

:::{grid-item-card} AWS Lambda
API Gateway-friendly handler with POST `/orders` (JSON body) and GET `/orders/:id` (path param). README includes a DynamoDB recipe using `@aws-sdk/lib-dynamodb`.

{bdg-info}`Serverless`
:::

:::{grid-item-card} Google Cloud Run
Containerized Hono service with `PORT` env reading, a POST `/echo` route, and a `Dockerfile`. README includes a Cloud SQL recipe via `@google-cloud/cloud-sql-connector`.

{bdg-info}`Containers`
:::
::::

## Category: Full-Stack

::::{grid} 1 1 2 2
:gutter: 3

:::{grid-item-card} Monorepo
pnpm/npm/yarn workspaces with `packages/shared`, `packages/server` (Hono + Drizzle), `packages/client` (Vite+/React). Types flow from `@<project>/shared` into both sides via the workspace protocol.

{bdg-success}`Workspaces` {bdg-info}`Shared types`
:::

:::{grid-item-card} Full-Stack
:link: ../recipes/setup-drizzle
:link-type: doc
Single-package alternative to Monorepo: one `package.json`, `src/{shared,server,client}`, Hono + Drizzle backend, Vite+/React client, `concurrently` for dev, Vite+ proxy for `/api/*`.

{bdg-success}`Single package` {bdg-info}`Shared types`
:::
::::

## Which Template Should I Choose?

| Situation | Template |
| --- | --- |
| I'm new to ReScript and just want to try it | Basic |
| I'm writing a reusable npm package | npm Library |
| I'm building a CLI tool | CLI Tool |
| I need a React SPA | Vite+ + React |
| I need SSR / Server Components / SEO | Next.js |
| I'm building a desktop app | Electron |
| I'm building a mobile app | React Native |
| I need a REST API with typed docs | Hono (REST) |
| I need a GraphQL API | Hono + GraphQL |
| I'm deploying to Cloudflare's edge | Cloudflare Workers |
| I'm deploying to AWS Lambda | AWS Lambda |
| I'm deploying a container to GCP | Google Cloud Run |
| I want multiple packages sharing types (scales to more) | Monorepo |
| I want one package with backend + frontend (simpler) | Full-Stack |

## Day-Two Recipes

When you need to extend a generated project, these recipes pick up where the template README leaves off:

- [Add a Hono endpoint](../recipes/add-hono-endpoint.md) — adding routes, validation, and shared types
- [Add a GraphQL resolver](../recipes/add-graphql-resolver.md) — expanding the schema and regenerating docs
- [Set up Drizzle](../recipes/setup-drizzle.md) — schema changes, migrations, and Turso
- [Add OpenAPI docs](../recipes/add-openapi-docs.md) — wiring Scalar UI and Zod schemas

## Notes on Vite+

The Vite+ based templates (Vite+ + React, Monorepo client, Full-Stack) pin `vite-plus` and `@voidzero-dev/vite-plus-core` at pre-1.0 versions. If a command breaks after an upgrade, replace `vite-plus` with `vite` in `vite.config.mjs` and swap the `vp` scripts for `vite` to fall back to classic Vite.

```{toctree}
:hidden:
:maxdepth: 1
```
