package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.AwsLambdaTemplateFiles
import com.rescript.plugin.wizard.templates.BasicTemplateFiles
import com.rescript.plugin.wizard.templates.CliToolTemplateFiles
import com.rescript.plugin.wizard.templates.CloudflareWorkersTemplateFiles
import com.rescript.plugin.wizard.templates.ElectronTemplateFiles
import com.rescript.plugin.wizard.templates.FullStackTemplateFiles
import com.rescript.plugin.wizard.templates.GoogleCloudRunTemplateFiles
import com.rescript.plugin.wizard.templates.HonoGraphqlTemplateFiles
import com.rescript.plugin.wizard.templates.HonoTemplateFiles
import com.rescript.plugin.wizard.templates.MonorepoTemplateFiles
import com.rescript.plugin.wizard.templates.NextjsTemplateFiles
import com.rescript.plugin.wizard.templates.NpmLibraryTemplateFiles
import com.rescript.plugin.wizard.templates.ReactNativeCliTemplateFiles
import com.rescript.plugin.wizard.templates.ReactNativeTemplateFiles
import com.rescript.plugin.wizard.templates.ResXTemplateFiles
import com.rescript.plugin.wizard.templates.TemplateContext
import com.rescript.plugin.wizard.templates.ViteReactTemplateFiles

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
    HONO(
        "Hono (Node.js)",
        """
        A production-shape REST API on Hono + Node.js covering persistence, validation,
        and API documentation out of the box.

        Includes:
        • Hono + @hono/node-server
        • SQLite via libsql + Drizzle ORM (schema, queries, drizzle-kit migrations)
        • Zod schemas for POST/PUT body validation
        • @hono/zod-openapi auto-generated OpenAPI 3 spec
        • Scalar UI mounted at /docs, raw spec at /openapi.json
        • Logger middleware + structured error handling

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.BACKEND,
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
        • SQLite via libsql + Drizzle ORM
        • users query/mutation resolvers (users / user(id) / createUser / deleteUser)
        • `docs:graphql` script runs graphql-markdown against the schema
        • Vitest + coverage

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.BACKEND,
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
        • packages/server — Hono + @hono/node-server + Drizzle SQLite
        • packages/client — React 19 + Vite+
        • pnpm-workspace.yaml (pnpm) or `workspaces` field (npm / yarn)
        • `concurrently` dev loop across packages

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FULL_STACK,
        sourceRoots = listOf("packages/shared/src", "packages/server/src", "packages/client/src"),
    ),
    FULL_STACK(
        "Full-Stack (single package)",
        """
        A single-package full-stack app pairing a Hono + Drizzle backend with a
        Vite+/React frontend. Types are shared through src/shared/Api.res — simpler
        than a workspace monorepo while still demonstrating the fetch → API → DB loop.

        Includes:
        • Hono + @hono/node-server + libsql + Drizzle ORM (SQLite)
        • React 19 + @rescript/react + Vite+
        • drizzle-kit migrations (db:generate / db:migrate)
        • `concurrently` dev loop (server watch + vp dev)
        • Vitest + coverage

        Requires: Node.js 24+.
        """.trimIndent(),
        TemplateCategory.FULL_STACK,
        sourceRoots = listOf("src/shared", "src/server", "src/client"),
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
            HONO -> HonoTemplateFiles.generate(ctx)
            HONO_GRAPHQL -> HonoGraphqlTemplateFiles.generate(ctx)
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
        }

    /**
     * Back-compatible entry point that defaults to pnpm when no package manager is specified.
     *
     * @param projectName the name of the project being created
     */
    fun generateFiles(projectName: String): Map<String, String> =
        generateFiles(TemplateContext(projectName, PackageManager.PNPM))
}
