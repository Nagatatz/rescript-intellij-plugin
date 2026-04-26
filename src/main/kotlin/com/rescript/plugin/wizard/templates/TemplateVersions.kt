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
    const val RESCRIPT = "^12.2.0"
    const val RESCRIPT_CORE = "^1.6.1"
    const val RESCRIPT_REACT = "^0.15.0"

    // Compiled `.res.mjs` output imports `@rescript/runtime/lib/es6/...` (e.g.
    // `Stdlib_JsExn.js`) at runtime. The package is a transitive dependency
    // of the `rescript` compiler, but pnpm's strict layout hides transitive
    // deps from user code — so every template declares it as a direct
    // dependency to keep the runtime accessible to vitest / node / browsers.
    const val RESCRIPT_RUNTIME = "^12.2.0"

    // Build tools
    // Raw Vite pin used when templates need a direct `vite` dep (fallback away
    // from Vite+). Vite+ bundles its own vite under the hood.
    const val VITE = "^8.0.9"

    // Vite+ (vite-plus) is pre-1.0; templates pin to the 0.1.x range until a stable release.
    const val VITE_PLUS = "^0.1.18"
    const val VITE_PLUS_CORE = "^0.1.18"
    const val VITEJS_PLUGIN_REACT = "^6.0.1"
    const val VITEST = "^4.1.4"

    // React
    const val REACT = "^19.2.5"
    const val REACT_DOM = "^19.2.5"
    const val REACT_TYPES = "^19.2.14"
    const val REACT_DOM_TYPES = "^19.2.3"

    // Backend
    const val HONO = "^4.12.14"
    const val HONO_NODE_SERVER = "^2.0.0"
    const val NODE_TYPES = "^25.6.0"

    // Next.js
    const val NEXTJS = "^16.2.4"

    // Electron
    const val ELECTRON = "^41.2.1"
    const val ELECTRON_BUILDER = "^26.8.1"

    // Expo / React Native
    const val EXPO = "^55.0.15"
    const val REACT_NATIVE = "^0.85.1"

    // React Native Community CLI (bare workflow)
    const val RN_COMMUNITY_CLI = "^20.1.3"
    const val RN_METRO_CONFIG = "^0.85.1"
    const val RN_BABEL_PRESET = "^0.85.1"
    const val RN_TYPESCRIPT_CONFIG = "^0.85.1"

    // Cloudflare Workers
    const val WRANGLER = "^4.83.0"
    const val CF_WORKERS_TYPES = "^4.20260420.1"

    // AWS Lambda
    const val AWS_LAMBDA_TYPES = "^8.10.161"
    const val ESBUILD = "^0.28.0"

    // Monorepo tooling
    const val CONCURRENTLY = "^9.2.1"

    // TypeScript (used as devDependency where interop is needed)
    const val TYPESCRIPT = "^6.0.3"

    // Package manager versions published via Corepack (`packageManager` field in package.json)
    const val PNPM = "10.33.0"
    const val NPM = "11.12.1"
    const val YARN = "4.14.1"

    // Bun runtime + package manager. v1.2+ introduces the text-based `bun.lock`
    // lockfile and the pnpm-style `bun --filter` monorepo selector; the template
    // generators assume both. Floor pinned to the latest stable so corepack
    // resolves a recent runtime when scaffolding new projects.
    const val BUN = "1.3.13"

    // Database (libsql client + Drizzle ORM)
    const val LIBSQL_CLIENT = "^0.17.2"
    const val DRIZZLE_ORM = "^0.45.2"
    const val DRIZZLE_KIT = "^0.31.10"

    // Validation
    const val ZOD = "^4.3.6"
    const val SURY = "^10.0.0"

    // OpenAPI / Hono ecosystem
    // @hono/zod-openapi v1 requires zod v4; they must be bumped together.
    const val HONO_ZOD_OPENAPI = "^1.3.0"
    const val SCALAR_HONO_API_REFERENCE = "^0.10.9"

    // GraphQL (Hono GraphQL template)
    const val GRAPHQL = "^16.13.2"
    const val GRAPHQL_YOGA = "^5.21.0"
    const val GRAPHQL_MARKDOWN = "^0.7.1"

    // rescript-relay (FULL_STACK GraphQL variant client). 4.x targets ReScript 12
    // and bundles Relay compiler 20.1.1 internally; relay-compiler must match.
    const val RESCRIPT_RELAY = "^4.4.1"
    const val RELAY_COMPILER = "^20.1.1"

    // res-x (Bun + Vite + HTMX framework) and its Bun peer dependency
    const val RESCRIPT_X = "^1.4.0"
    const val RESCRIPT_BUN = "^2.1.0"

    // HTMX CDN version used by the res-x template's Layout.res
    const val HTMX_CDN = "2.0.7"

    // AWS Lambda runtime types (separate from @types/aws-lambda)
    const val AWS_LAMBDA = "^1.0.7"

    // Node.js engine range used by templates
    const val NODE_ENGINE = ">=24"

    // Node.js major version used by `.nvmrc` / `.node-version` (pairs with NODE_ENGINE)
    const val NODE_MAJOR = "24"

    // Vitest coverage provider used by `test:coverage` scripts
    const val VITEST_COVERAGE_V8 = "^4.1.4"
}
