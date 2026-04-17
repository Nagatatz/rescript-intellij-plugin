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
    const val RESCRIPT = "^12.0.0"
    const val RESCRIPT_CORE = "^1.0.0"
    const val RESCRIPT_REACT = "^0.15.0"

    // Build tools
    // Vite+ (vite-plus) is pre-1.0; templates pin to the 0.1.x range until a stable release.
    const val VITE_PLUS = "^0.1.0"
    const val VITE_PLUS_CORE = "^0.1.0"
    const val VITEJS_PLUGIN_REACT = "^6.0.0"
    const val VITEST = "^4.1.0"

    // React
    const val REACT = "^19.2.5"
    const val REACT_DOM = "^19.2.5"
    const val REACT_TYPES = "^19.2.0"

    // Backend
    const val HONO = "^4.12.0"
    const val HONO_NODE_SERVER = "^1.19.0"
    const val NODE_TYPES = "^25.0.0"

    // Next.js
    const val NEXTJS = "^16.2.0"

    // Electron
    const val ELECTRON = "^41.0.0"
    const val ELECTRON_BUILDER = "^26.0.0"

    // Expo / React Native
    const val EXPO = "^55.0.0"
    const val REACT_NATIVE = "^0.85.0"

    // React Native Community CLI (bare workflow)
    const val RN_COMMUNITY_CLI = "^20.0.0"
    const val RN_METRO_CONFIG = "^0.85.0"
    const val RN_BABEL_PRESET = "^0.85.0"

    // Cloudflare Workers
    const val WRANGLER = "^4.83.0"
    const val CF_WORKERS_TYPES = "^4.20260417.0"

    // AWS Lambda
    const val AWS_LAMBDA_TYPES = "^8.10.0"
    const val ESBUILD = "^0.27.0"

    // Monorepo tooling
    const val CONCURRENTLY = "^9.2.0"

    // TypeScript (used as devDependency where interop is needed)
    const val TYPESCRIPT = "^6.0.0"

    // Package manager versions published via Corepack (`packageManager` field in package.json)
    const val PNPM = "10.33.0"
    const val NPM = "11.12.0"
    const val YARN = "4.5.0"

    // Database (libsql client + Drizzle ORM)
    const val LIBSQL_CLIENT = "^0.17.0"
    const val DRIZZLE_ORM = "^0.45.0"
    const val DRIZZLE_KIT = "^0.31.0"

    // Validation
    const val ZOD = "^4.3.0"

    // OpenAPI / Hono ecosystem
    // @hono/zod-openapi v1 requires zod v4; they must be bumped together.
    const val HONO_ZOD_OPENAPI = "^1.3.0"
    const val SCALAR_HONO_API_REFERENCE = "^0.5.0"

    // GraphQL (Hono GraphQL template)
    const val GRAPHQL = "^16.13.0"
    const val GRAPHQL_YOGA = "^5.21.0"
    const val GRAPHQL_MARKDOWN = "^7.3.0"

    // AWS Lambda runtime types (separate from @types/aws-lambda)
    const val AWS_LAMBDA = "^1.0.0"

    // Node.js engine range used by templates
    const val NODE_ENGINE = ">=22"

    // Node.js major version used by `.nvmrc` / `.node-version` (pairs with NODE_ENGINE)
    const val NODE_MAJOR = "22"

    // Vitest coverage provider used by `test:coverage` scripts
    const val VITEST_COVERAGE_V8 = "^4.1.0"
}
