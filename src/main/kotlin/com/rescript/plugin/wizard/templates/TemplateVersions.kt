package com.rescript.plugin.wizard.templates

/**
 * Centralized version constants for dependencies and tooling referenced by project templates.
 *
 * Having a single source of truth simplifies coordinated version bumps, makes upgrades
 * reviewable in a single diff, and prevents drift between templates that share a dependency.
 * When bumping a constant here, re-run the template integration tests to ensure the new
 * version still installs and builds.
 */
object TemplateVersions {
    // ReScript core
    val RESCRIPT = "^12.3.0"
    val RESCRIPT_CORE = "^1.6.1"
    val RESCRIPT_REACT = "^0.15.0"

    // Compiled `.res.mjs` output imports `@rescript/runtime/lib/es6/...` (e.g.
    // `Stdlib_JsExn.js`) at runtime. The package is a transitive dependency
    // of the `rescript` compiler, but pnpm's strict layout hides transitive
    // deps from user code — so every template declares it as a direct
    // dependency to keep the runtime accessible to vitest / node / browsers.
    val RESCRIPT_RUNTIME = "^12.3.0"

    // Build tools
    // Raw Vite pin used when templates need a direct `vite` dep (fallback away
    // from Vite+). Vite+ bundles its own vite under the hood.
    val VITE = "^8.0.12"

    // Vite+ (vite-plus) is pre-1.0; templates pin to the 0.1.x range until a stable release.
    val VITE_PLUS = "^0.1.21"
    val VITE_PLUS_CORE = "^0.1.21"
    val VITEJS_PLUGIN_REACT = "^6.0.1"
    val VITEST = "^4.1.6"

    // React
    val REACT = "^19.2.6"
    val REACT_DOM = "^19.2.6"
    val REACT_TYPES = "^19.2.14"
    val REACT_DOM_TYPES = "^19.2.3"

    // Backend
    val HONO = "^4.12.15"
    val HONO_NODE_SERVER = "^2.0.0"
    val NODE_TYPES = "^25.6.0"

    // Hono + Inertia (server-driven SPA bridge). The 0.x range is alpha; bump together with INERTIA_REACT.
    val HONO_INERTIA = "^0.2.0"
    val INERTIA_REACT = "^3.0.3"

    // Next.js
    val NEXTJS = "^16.2.9"

    // Electron
    val ELECTRON = "^42.0.1"
    val ELECTRON_BUILDER = "^26.8.1"

    // Expo / React Native
    val EXPO = "^56.0.11"
    val REACT_NATIVE = "^0.85.2"

    // React Native Community CLI (bare workflow)
    val RN_COMMUNITY_CLI = "^20.1.3"
    val RN_METRO_CONFIG = "^0.85.2"
    val RN_BABEL_PRESET = "^0.85.2"
    val RN_TYPESCRIPT_CONFIG = "^0.85.2"

    // Cloudflare Workers
    val WRANGLER = "^4.90.0"
    val CF_WORKERS_TYPES = "^4.20260426.1"

    // AWS Lambda
    val AWS_LAMBDA_TYPES = "^8.10.161"
    val ESBUILD = "^0.28.0"

    // Monorepo tooling
    val CONCURRENTLY = "^10.0.3"

    // TypeScript (used as devDependency where interop is needed)
    val TYPESCRIPT = "^6.0.3"

    // Package manager versions published via Corepack (`packageManager` field in package.json)
    val PNPM = "11.1.0"
    val NPM = "11.14.1"
    val YARN = "4.14.1"

    // Bun runtime + package manager. v1.2+ introduces the text-based `bun.lock`
    // lockfile and the pnpm-style `bun --filter` monorepo selector; the template
    // generators assume both. Floor pinned to the latest stable so corepack
    // resolves a recent runtime when scaffolding new projects.
    val BUN = "1.3.13"

    // Database (libsql client + Drizzle ORM)
    val LIBSQL_CLIENT = "^0.17.3"
    val DRIZZLE_ORM = "^0.45.2"
    val DRIZZLE_KIT = "^0.31.10"

    // PostgreSQL: postgres-js (lighter than node-postgres, drizzle-recommended).
    // Bumped together with DRIZZLE_ORM since the dialect adapter ships in drizzle-orm.
    val POSTGRES_JS = "^3.4.5"

    // MySQL: mysql2 driver (drizzle-recommended).
    val MYSQL2 = "^3.22.3"

    // Container image tags used by templates' compose.yaml for local Postgres / MySQL.
    // Pinned to the current LTS image major; bump intentionally.
    val POSTGRES_DOCKER_IMAGE = "postgres:18-alpine"
    val MYSQL_DOCKER_IMAGE = "mysql:8.4"

    // Validation
    val ZOD = "^4.4.3"
    val SURY = "^10.0.4"

    // OpenAPI / Hono ecosystem
    // @hono/zod-openapi v1 requires zod v4; they must be bumped together.
    val HONO_ZOD_OPENAPI = "^1.3.0"
    val SCALAR_HONO_API_REFERENCE = "^0.10.10"

    // GraphQL (Hono GraphQL template)
    val GRAPHQL = "^16.13.2"
    val GRAPHQL_YOGA = "^5.21.0"
    val GRAPHQL_MARKDOWN = "^0.7.1"

    // rescript-relay (FULL_STACK GraphQL variant client). 4.x targets ReScript 12
    // and bundles Relay compiler 20.1.1 internally; relay-compiler must match.
    val RESCRIPT_RELAY = "^4.4.1"
    val RELAY_COMPILER = "^20.1.1"

    // res-x (Bun + Vite + HTMX framework) and its Bun peer dependency
    val RESCRIPT_X = "^1.4.0"
    val RESCRIPT_BUN = "^2.1.0"

    // HTMX CDN version used by the res-x template's Layout.res
    val HTMX_CDN = "2.0.10"

    // AWS Lambda runtime types (separate from @types/aws-lambda)
    val AWS_LAMBDA = "^1.0.7"

    // Node.js engine range used by templates
    val NODE_ENGINE = ">=24"

    // Node.js major version used by `.nvmrc` / `.node-version` (pairs with NODE_ENGINE)
    val NODE_MAJOR = "24"

    // Vitest coverage provider used by `test:coverage` scripts
    val VITEST_COVERAGE_V8 = "^4.1.6"

    // TanStack Start (full-stack React, Vite-based) and its router peer
    val TANSTACK_REACT_START = "^1.167.65"
    val TANSTACK_REACT_ROUTER = "^1.169.2"
    val TANSTACK_ROUTER_PLUGIN = "^1.167.35"

    // React Router v7 (Framework mode — what was previously Remix)
    val REACT_ROUTER = "^7.15.0"
    val REACT_ROUTER_DEV = "^7.15.0"
    val REACT_ROUTER_NODE = "^7.15.0"
    val REACT_ROUTER_SERVE = "^7.15.0"

    // Bot detection used by React Router v7's SSR entry. The dev plugin auto-
    // injects this dependency on first run; pre-pinning it keeps `pnpm install
    // --frozen-lockfile` valid for the generated project.
    val ISBOT = "^5.1.0"

    // Astro and the React integration. astro 6.x is the active major;
    // @astrojs/react 5.x and @astrojs/node 10.x are the matching adapters.
    val ASTRO = "^6.3.1"
    val ASTROJS_REACT = "^5.0.0"
    val ASTROJS_NODE = "^10.0.0"

    // Waku (RSC-first React framework by Daishi Kato). 1.0.0 is currently in
    // alpha — pin to a known alpha to keep installs reproducible until 1.0
    // ships. Bump together with the template README's RSC walkthrough.
    val WAKU = "1.0.0-alpha.10"

    // Tauri 2.x desktop runtime. @rescript-tauri/core is a 0.x package, so we
    // pin to ^0.1.x to follow patch updates without auto-adopting breaking
    // 0.minor bumps. @tauri-apps/api / @tauri-apps/cli track Tauri 2.x.
    val RESCRIPT_TAURI_CORE = "^0.1.0"
    val TAURI_APPS_API = "^2.11.0"
    val TAURI_APPS_CLI = "^2.11.1"

    // rescript-tauri plugin bindings — one matching package per
    // `tauri-plugin-*` Rust crate the template registers. All currently
    // ship at 0.1.x; bump them together as the rescript-tauri monorepo
    // progresses.
    val RESCRIPT_TAURI_PLUGIN_OS = "^0.1.0"
    val RESCRIPT_TAURI_PLUGIN_FS = "^0.1.0"
    val RESCRIPT_TAURI_PLUGIN_DIALOG = "^0.1.0"
    val RESCRIPT_TAURI_PLUGIN_CLIPBOARD_MANAGER = "^0.1.0"
    val RESCRIPT_TAURI_PLUGIN_NOTIFICATION = "^0.1.0"
    val RESCRIPT_TAURI_PLUGIN_LOG = "^0.1.0"

    // @tauri-apps/plugin-* JS SDKs (peer deps of the rescript-tauri
    // bindings above). The bindings declare 2.x peer ranges, so we pin
    // to the latest minor of each plugin's 2.x line.
    val TAURI_APPS_PLUGIN_OS = "^2.3.2"
    val TAURI_APPS_PLUGIN_FS = "^2.5.1"
    val TAURI_APPS_PLUGIN_DIALOG = "^2.7.1"
    val TAURI_APPS_PLUGIN_CLIPBOARD_MANAGER = "^2.3.2"
    val TAURI_APPS_PLUGIN_NOTIFICATION = "^2.3.3"
    val TAURI_APPS_PLUGIN_LOG = "^2.8.0"
}
