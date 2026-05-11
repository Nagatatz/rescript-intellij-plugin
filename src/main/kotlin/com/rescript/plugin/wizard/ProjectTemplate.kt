package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.AstroTemplateFiles
import com.rescript.plugin.wizard.templates.AwsLambdaTemplateFiles
import com.rescript.plugin.wizard.templates.BasicTemplateFiles
import com.rescript.plugin.wizard.templates.CliToolTemplateFiles
import com.rescript.plugin.wizard.templates.CloudflareWorkersTemplateFiles
import com.rescript.plugin.wizard.templates.ElectronTemplateFiles
import com.rescript.plugin.wizard.templates.FullStackTemplateFiles
import com.rescript.plugin.wizard.templates.GoogleCloudRunTemplateFiles
import com.rescript.plugin.wizard.templates.HonoGraphqlTemplateFiles
import com.rescript.plugin.wizard.templates.HonoInertiaTemplateFiles
import com.rescript.plugin.wizard.templates.HonoTemplateFiles
import com.rescript.plugin.wizard.templates.MonorepoTemplateFiles
import com.rescript.plugin.wizard.templates.NextjsTemplateFiles
import com.rescript.plugin.wizard.templates.NpmLibraryTemplateFiles
import com.rescript.plugin.wizard.templates.ReactNativeCliTemplateFiles
import com.rescript.plugin.wizard.templates.ReactNativeTemplateFiles
import com.rescript.plugin.wizard.templates.RemixV7TemplateFiles
import com.rescript.plugin.wizard.templates.ResXTemplateFiles
import com.rescript.plugin.wizard.templates.TanstackStartTemplateFiles
import com.rescript.plugin.wizard.templates.TauriTemplateFiles
import com.rescript.plugin.wizard.templates.TemplateContext
import com.rescript.plugin.wizard.templates.ViteReactTemplateFiles
import com.rescript.plugin.wizard.templates.WakuTemplateFiles

/**
 * Categories for grouping project templates in the wizard UI.
 */
enum class TemplateCategory(
    val displayName: String,
) {
    BASIC("Basic"),
    FRONTEND("Frontend"),
    DESKTOP("Desktop"),
    BACKEND("Backend"),
    SERVERLESS("Serverless"),
    MOBILE("Mobile"),
    LIBRARY("Library"),
    TOOL("Tool"),
    FULL_STACK("Full Stack"),
}

/**
 * Available project templates for the ReScript project wizard.
 *
 * Each entry defines a template with display metadata and delegates file generation
 * to a corresponding object in the `templates` package. The [generateFiles] method
 * returns a map of relative file paths to their content strings.
 *
 * @see ProjectFileBuilders for shared file generation utilities
 * @see RescriptModuleBuilder for wizard integration
 */
enum class ProjectTemplate(
    val displayName: String,
    val description: String,
    val category: TemplateCategory,
    val sourceRoots: List<String> = listOf("src"),
    val supportsValidationSelection: Boolean = true,
    val supportsDatabaseSelection: Boolean = false,
) {
    BASIC(
        "Basic",
        """
        A minimal ReScript starter that goes beyond "Hello World": demonstrates CLI
        argument parsing, file I/O via Node's fs/promises, and a small app entry point
        that ties them together.

        Includes:
        • @rescript/core standard library
        • Vitest + @vitest/coverage-v8 for tests
        • GitHub Actions CI workflow and Dependabot config
        • .nvmrc, .editorconfig, MIT LICENSE

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.BASIC,
    ),
    VITE_REACT(
        "Vite + React",
        """
        A React single-page application built with ReScript and the Vite+ toolchain —
        a unified wrapper over Vite, Vitest, Oxlint, Oxfmt, and Rolldown.

        Includes:
        • @rescript/react + React 19 with JSX enabled
        • Vite+ (vite-plus) with a documented fallback to plain Vite
        • @vitejs/plugin-react, Vitest + coverage
        • Sample App / Main / Api modules and a component test

        Requires: Node.js 24+. Vite+ is pre-1.0; the README shows how to fall back
        to plain Vite if needed.
        """.trimIndent(),
        TemplateCategory.FRONTEND,
    ),
    NEXTJS(
        "Next.js",
        """
        A Next.js 16 App Router project that exposes ReScript components to
        TSX/route-handler code via genType.

        Includes:
        • Next.js 16 + React 19 + @rescript/react
        • genType interop (ReScript ↔ TypeScript)
        • `concurrently` wiring `rescript -w` with `next dev`
        • Sample App / GreetForm / Fetch modules + API route handler
        • Vitest + coverage

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FRONTEND,
    ),
    ELECTRON(
        "Electron",
        """
        A cross-platform desktop application pairing an Electron main process with a
        Vite+ renderer written in ReScript + React.

        Includes:
        • Electron 41 (main) + @rescript/react + React 19 (renderer)
        • Vite+ bundling for the renderer, preload.cjs for context isolation
        • `npm start` builds and launches Electron in one command
        • Vitest + coverage

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.DESKTOP,
    ),
    TAURI(
        "Tauri",
        """
        A Tauri 2.x desktop application pairing a Rust process with a Vite+ renderer
        written in ReScript + React. IPC is wired through @rescript-tauri/core.

        Includes:
        • Tauri 2.x (Rust `src-tauri/`) + @rescript/react + React 19 (renderer)
        • @rescript-tauri/core bindings over `Core.Raw.invoke`
        • Vite+ bundling for the renderer, snake_case `#[tauri::command]` examples
        • IPC response validation via zod or sury (renderer side)
        • Vitest + coverage
        • CSP, allowlist, and "add a new command" walkthrough in the README

        Requires: Node.js 24+, Rust toolchain (`rustup default stable`), and platform
        build deps — see https://v2.tauri.app/start/prerequisites/.
        """.trimIndent(),
        TemplateCategory.DESKTOP,
    ),
    HONO(
        "Hono (Node.js)",
        """
        A production-shape REST API on Hono + Node.js covering persistence, validation,
        and API documentation out of the box.

        Includes:
        • Hono + @hono/node-server
        • Drizzle ORM with libSQL / SQLite (default), PostgreSQL, or MySQL — selectable
        • Zod schemas for POST/PUT body validation
        • @hono/zod-openapi auto-generated OpenAPI 3 spec
        • Scalar UI mounted at /docs, raw spec at /openapi.json
        • Logger middleware + structured error handling
        • Multi-stage Dockerfile + .dockerignore for container deploys
        • compose.yaml when PostgreSQL / MySQL is selected

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.BACKEND,
        supportsDatabaseSelection = true,
    ),
    HONO_GRAPHQL(
        "Hono GraphQL",
        """
        A GraphQL API on Hono + graphql-yoga with a users type wired end-to-end
        (query, mutation, Drizzle table) so "add a new GraphQL type" is a copy/paste
        exercise.

        Includes:
        • Hono + @hono/node-server with graphql-yoga mounted at /graphql
        • GraphiQL playground at the same URL
        • Drizzle ORM with libSQL / SQLite (default), PostgreSQL, or MySQL — selectable
        • users query/mutation resolvers (users / user(id) / createUser / deleteUser)
        • `docs:graphql` script runs graphql-markdown against the schema
        • Vitest + coverage
        • Multi-stage Dockerfile + .dockerignore for container deploys
        • compose.yaml when PostgreSQL / MySQL is selected

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.BACKEND,
        supportsDatabaseSelection = true,
    ),
    HONO_INERTIA(
        "Hono + Inertia (React)",
        """
        A server-driven SPA pairing a Hono backend with @inertiajs/react v3 — the
        controller renders React pages directly with c.render(component, props),
        so there is no separate REST/GraphQL layer to maintain.

        Includes:
        • Hono + @hono/inertia middleware (inertiaPages Vite plugin)
        • @inertiajs/react v3 + React 19 + @rescript/react
        • Vite+ unified toolchain (vp dev / build / test / check)
        • Sample Home / About pages and a shared MainLayout
        • Drizzle ORM with libSQL / SQLite (default), PostgreSQL, or MySQL — selectable
        • zod or sury validation
        • CSR only — server-side rendering is intentionally out of scope

        Requires: Node.js 24+ (or Bun 1.3+ if Bun is selected).
        """.trimIndent(),
        TemplateCategory.FULL_STACK,
        sourceRoots = listOf("src", "src/client"),
        supportsDatabaseSelection = true,
    ),
    CLOUDFLARE_WORKERS(
        "Cloudflare Workers",
        """
        A serverless Hono service on Cloudflare Workers with a working Workers KV
        example (POST/GET greetings) so `wrangler dev` boots with a real local store.

        Includes:
        • Hono on Cloudflare Workers runtime
        • Wrangler 4 (`dev` / `deploy`) with KV binding pre-declared in wrangler.jsonc
        • @cloudflare/workers-types
        • Vitest + coverage
        • README sections for API, KV setup, and deploy

        Requires: Node.js 24+ for local tooling, Cloudflare account for deploy.
        """.trimIndent(),
        TemplateCategory.SERVERLESS,
    ),
    AWS_LAMBDA(
        "AWS Lambda",
        """
        A Hono API on AWS Lambda bundled to a single ESM file via esbuild. Ships
        POST/GET endpoints plus a DynamoDB integration recipe.

        Includes:
        • Hono with AWS Lambda event adapter
        • esbuild bundling ReScript output to dist/index.mjs
        • @types/aws-lambda for handler typing
        • Vitest + coverage
        • README sections for API, deploy, and DynamoDB integration

        Requires: Node.js 24+, AWS account + IAM credentials for deploy.
        """.trimIndent(),
        TemplateCategory.SERVERLESS,
    ),
    GOOGLE_CLOUD_RUN(
        "Google Cloud Run",
        """
        A container-based Hono service deployable to Google Cloud Run, with a
        multi-stage Dockerfile that honors the selected package manager.

        Includes:
        • Hono + @hono/node-server
        • Multi-stage Dockerfile (pnpm / npm / yarn aware) + .dockerignore
        • POST/GET endpoints with environment variable reading
        • Vitest + coverage
        • README sections for API, environment, deploy, and Cloud SQL recipe

        Requires: Node.js 24+, Docker for image builds, gcloud CLI for deploy.
        """.trimIndent(),
        TemplateCategory.SERVERLESS,
    ),
    REACT_NATIVE(
        "React Native (Expo)",
        """
        An Expo-managed React Native app with ReScript components exposed to App.tsx
        via genType — no native tooling required to get started.

        Includes:
        • Expo 55 + React Native + React 19
        • @rescript/react with genType for .gen.tsx interop
        • Sample App + ReactNative bindings module
        • Vitest source smoke test
        • `expo start` / `expo start --android` / `expo start --ios`

        Requires: Node.js 24+; Android emulator or iOS simulator for device runs.
        """.trimIndent(),
        TemplateCategory.MOBILE,
    ),
    REACT_NATIVE_CLI(
        "React Native (Community CLI)",
        """
        A bare-workflow React Native app using the Community CLI. Ships only the
        JS/TS + ReScript surface; run `npx @react-native-community/cli` afterwards
        to scaffold the native android/ and ios/ projects.

        Includes:
        • React Native + @react-native-community/cli
        • @react-native/metro-config + babel-preset
        • Metro resolver configured for .res.mjs output
        • Sample NativeGreeting binding for custom native modules
        • Vitest

        Requires: Node.js 24+, Android Studio / Xcode for native builds.
        """.trimIndent(),
        TemplateCategory.MOBILE,
    ),
    NPM_LIBRARY(
        "npm Library",
        """
        A publishable npm package written in ReScript with TypeScript types generated
        via genType. Exercises the common day-two patterns library authors hit.

        Includes:
        • @rescript/core + genType for .d.ts emission
        • TypeScript devDependency for type consumers
        • Sample API: sync greet, async fetchWithTimeout, list helpers
        • Vitest + coverage across three test files
        • `prepare` hook so `npm install` from git builds the package

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.LIBRARY,
    ),
    CLI_TOOL(
        "CLI Tool",
        """
        A command-line tool with a subcommand dispatcher (`greet`, `init`) and a
        flag-parsing helper — mirrors the shape of git / docker / rescript.

        Includes:
        • @rescript/core + `bin` entry pointing at Cli.res.mjs
        • Args / Commands modules with tests
        • Vitest + coverage
        • README sections for usage, project layout, and local install

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.TOOL,
    ),
    MONOREPO(
        "Monorepo (Hono + React)",
        """
        A full-stack workspace monorepo splitting shared types, a Hono + Drizzle
        backend, and a Vite+ React frontend into three packages. Workspace plumbing
        adapts to the selected package manager.

        Includes:
        • packages/shared — shared ReScript types
        • packages/server — Hono + @hono/node-server + Drizzle (libSQL / Postgres / MySQL)
        • packages/client — React 19 + Vite+
        • pnpm-workspace.yaml (pnpm) or `workspaces` field (npm / yarn)
        • `concurrently` dev loop across packages
        • Multi-stage Dockerfile + compose.yaml (when Postgres / MySQL is selected)

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FULL_STACK,
        sourceRoots = listOf("packages/shared/src", "packages/server/src", "packages/client/src"),
        supportsDatabaseSelection = true,
    ),
    FULL_STACK(
        "Full-Stack (single package)",
        """
        A single-package full-stack app pairing a Hono + Drizzle backend with a
        Vite+/React frontend. Types are shared through src/shared/Api.res — simpler
        than a workspace monorepo while still demonstrating the fetch → API → DB loop.

        Includes:
        • Hono + @hono/node-server + Drizzle ORM
        • Database choice: libSQL / SQLite (default), PostgreSQL, or MySQL
        • React 19 + @rescript/react + Vite+
        • drizzle-kit migrations (db:generate / db:migrate)
        • `concurrently` dev loop (server watch + vp dev)
        • Multi-stage Dockerfile + compose.yaml (when Postgres / MySQL is selected)
        • Vitest + coverage

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FULL_STACK,
        sourceRoots = listOf("src/shared", "src/server", "src/client"),
        supportsDatabaseSelection = true,
    ),
    RES_X(
        "res-x (HTMX on Bun)",
        """
        A server-driven web application built with res-x: JSX renders HTML on
        Bun and HTMX drives client interactivity — no client-side framework.

        Includes:
        • rescript-x (Hjsx) + rescript-bun on Bun's native serve API
        • Counter component with hx-post increment/decrement endpoints
        • Todo form that validates input with zod or sury (selectable in Wizard)
        • Vite + the res-x Vite plugin, HTMX loaded from CDN
        • Vitest + coverage smoke test

        Requires: Node.js 24+ for tooling, Bun 1.3+ (https://bun.sh) for running the server.
        """.trimIndent(),
        TemplateCategory.FULL_STACK,
    ),
    TANSTACK_START(
        "TanStack Start",
        """
        A type-safe full-stack React app powered by TanStack Start (Vite-based) with
        ReScript components and a sample Server Function. File-based routing is wired
        via @tanstack/react-router; the ReScript Greeting module is consumed from the
        index route through genType-friendly interop.

        Includes:
        • @tanstack/react-start + @tanstack/react-router (file-based routing)
        • Vite + @vitejs/plugin-react, vite.config.ts wires the TanStack plugin
        • @rescript/react + React 19 with JSX enabled
        • Sample Server Function reachable from the home route
        • Vitest + @vitest/coverage-v8

        Validation library selection is disabled — TanStack Start ships its own data
        flow primitives, and consumers can layer zod or sury on top as needed.

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FRONTEND,
        supportsValidationSelection = false,
    ),
    REMIX_RR_V7(
        "Remix / React Router v7",
        """
        A React Router v7 app in Framework mode (the next iteration of Remix). Loaders
        and components are organised under `app/`, ReScript provides the Greet component
        and a typed loader, and @react-router/dev wires the SSR pipeline through Vite.

        Includes:
        • react-router + @react-router/dev (Framework mode, file-based routes)
        • @rescript/react + React 19 with JSX enabled
        • Sample loader written in ReScript and consumed from a TSX route
        • @react-router/node + @react-router/serve for production
        • Vitest + @vitest/coverage-v8

        Validation library selection is disabled — React Router uses standard Web
        FormData/Request primitives; layer zod, valibot, or sury where you need it.

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FRONTEND,
        sourceRoots = listOf("app"),
        supportsValidationSelection = false,
    ),
    ASTRO(
        "Astro",
        """
        An Astro content site that mixes static markup with React Islands. Astro pages
        are authored in `.astro`; interactive React components live alongside them as
        ReScript modules and are hydrated on demand via `client:load` / `client:visible`.

        Includes:
        • astro + @astrojs/react integration + @astrojs/node adapter
        • @rescript/react + React 19 with JSX enabled
        • Static greeting + interactive Counter Island, both written in ReScript
        • Vitest + @vitest/coverage-v8

        Validation library selection is disabled — Astro pages typically validate input
        per Action / endpoint with zod or sury; add the dependency where it's needed.

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FRONTEND,
        supportsValidationSelection = false,
    ),
    WAKU(
        "Waku",
        """
        A minimal React Server Components app powered by Waku (by Daishi Kato). Server
        Components render on the server with zero client-side JS; interactive Client
        Components opt in via a `"use client"` boundary, and ReScript provides both.

        Includes:
        • waku (RSC-first React framework)
        • @rescript/react + React 19 with JSX enabled
        • Server Component (Greet.res) + Client Component (Counter.res via a thin
          `"use client"` TSX wrapper)
        • Vitest + @vitest/coverage-v8

        Validation library selection is disabled — RSC boundaries typically validate
        their own inputs; add zod or sury per server function as needed.

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FRONTEND,
        supportsValidationSelection = false,
    ),
    ;

    /**
     * Generates all project files for this template using the full template context.
     *
     * Templates that have migrated to the context-aware generator receive [ctx] directly;
     * those still on the legacy signature fall back to [generateFiles(String)] which is
     * wired to dispatch to the same per-template objects.
     *
     * @param ctx the template context (project name + selected package manager)
     * @return a map of relative file paths to their string content
     */
    fun generateFiles(ctx: TemplateContext): Map<String, String> =
        when (this) {
            BASIC -> BasicTemplateFiles.generate(ctx)
            VITE_REACT -> ViteReactTemplateFiles.generate(ctx)
            NEXTJS -> NextjsTemplateFiles.generate(ctx)
            ELECTRON -> ElectronTemplateFiles.generate(ctx)
            TAURI -> TauriTemplateFiles.generate(ctx)
            HONO -> HonoTemplateFiles.generate(ctx)
            HONO_GRAPHQL -> HonoGraphqlTemplateFiles.generate(ctx)
            HONO_INERTIA -> HonoInertiaTemplateFiles.generate(ctx)
            CLOUDFLARE_WORKERS -> CloudflareWorkersTemplateFiles.generate(ctx)
            AWS_LAMBDA -> AwsLambdaTemplateFiles.generate(ctx)
            GOOGLE_CLOUD_RUN -> GoogleCloudRunTemplateFiles.generate(ctx)
            REACT_NATIVE -> ReactNativeTemplateFiles.generate(ctx)
            REACT_NATIVE_CLI -> ReactNativeCliTemplateFiles.generate(ctx)
            NPM_LIBRARY -> NpmLibraryTemplateFiles.generate(ctx)
            CLI_TOOL -> CliToolTemplateFiles.generate(ctx)
            MONOREPO -> MonorepoTemplateFiles.generate(ctx)
            FULL_STACK -> FullStackTemplateFiles.generate(ctx)
            RES_X -> ResXTemplateFiles.generate(ctx)
            TANSTACK_START -> TanstackStartTemplateFiles.generate(ctx)
            REMIX_RR_V7 -> RemixV7TemplateFiles.generate(ctx)
            ASTRO -> AstroTemplateFiles.generate(ctx)
            WAKU -> WakuTemplateFiles.generate(ctx)
        }

    /**
     * Back-compatible entry point that defaults to pnpm when no package manager is specified.
     *
     * @param projectName the name of the project being created
     */
    fun generateFiles(projectName: String): Map<String, String> =
        generateFiles(TemplateContext(projectName, PackageManager.PNPM))
}
