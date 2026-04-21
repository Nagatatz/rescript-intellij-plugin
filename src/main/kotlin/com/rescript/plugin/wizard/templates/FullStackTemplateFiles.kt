package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a single-package full-stack ReScript app.
 *
 * Pairs a Hono + Drizzle (SQLite) backend with a Vite+/React frontend in one
 * `package.json`. Types are shared through `src/shared/` rather than through a
 * workspace, which keeps the project simpler than [MonorepoTemplateFiles] while
 * still demonstrating the end-to-end loop (fetch → API → DB → response).
 *
 * The generated project answers the common day-two question "how do I share
 * types between server and client?" by shipping a `src/shared/Api.res` module
 * imported by both sides. HTTP body validation lives in `src/server/Validation.res`
 * (zod or sury, selected via [TemplateContext.validationLibrary]).
 *
 * Static file content lives under `src/main/resources/templates/full-stack/` and is
 * loaded via [TemplateResourceLoader]; dynamic composition (package.json, README
 * scripts table, CI) stays in Kotlin.
 */
internal object FullStackTemplateFiles {
    /**
     * Generates Full-Stack template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val nameVar = mapOf("projectName" to ctx.projectName)
        val variantKey = ctx.validationLibrary.variantKey()
        return mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                    sources =
                        "  \"sources\": [\n" +
                            "    {\"dir\": \"src/shared\", \"subdirs\": true},\n" +
                            "    {\"dir\": \"src/server\", \"subdirs\": true},\n" +
                            "    {\"dir\": \"src/client\", \"subdirs\": true}\n" +
                            "  ],",
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies = fullStackDependencies(ctx.validationLibrary),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                            "vite" to TemplateVersions.VITE,
                            "vite-plus" to TemplateVersions.VITE_PLUS,
                            "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"npm:dev:server\" \"npm:dev:client\"",
                            "dev:server" to "node --watch src/server/ServerMain.res.mjs",
                            "dev:client" to "vp dev",
                            "build" to "vp build",
                            "preview" to "vp preview",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "db:generate" to "drizzle-kit generate",
                            "db:migrate" to "drizzle-kit migrate",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "index.html" to TemplateResourceLoader.load("full-stack/index.html", nameVar),
            "vite.config.mjs" to
                ProjectFileBuilders.viteConfigWithProxy(
                    imports =
                        listOf(
                            """import { defineConfig } from "vite-plus";""",
                            """import react from "@vitejs/plugin-react";""",
                        ),
                ),
            "drizzle.config.ts" to TemplateResourceLoader.load("full-stack/drizzle.config.ts"),
            "src/shared/Shared.res" to TemplateResourceLoader.load("full-stack/src/shared/Shared.res"),
            "src/server/ServerMain.res" to TemplateResourceLoader.load("full-stack/src/server/ServerMain.res"),
            "src/server/Server.res" to TemplateResourceLoader.load("full-stack/src/server/Server.res"),
            "src/server/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/server/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/server/Schema.res" to TemplateResourceLoader.load("full-stack/src/server/Schema.res"),
            "src/server/Validation.res" to
                TemplateResourceLoader.load("full-stack/variants/$variantKey/src/server/Validation.res"),
            "src/server/Db.res" to TemplateResourceLoader.load("common/db/Db.res"),
            "src/server/Routes.res" to TemplateResourceLoader.load("full-stack/src/server/Routes.res"),
            "src/server/__tests__/Server.test.mjs" to
                TemplateResourceLoader.load("full-stack/src/server/__tests__/Server.test.mjs"),
            "src/client/ClientMain.res" to TemplateResourceLoader.load("full-stack/src/client/ClientMain.res"),
            "src/client/App.res" to TemplateResourceLoader.load("full-stack/src/client/App.res", nameVar),
            "src/client/ApiClient.res" to TemplateResourceLoader.load("full-stack/src/client/ApiClient.res"),
            "src/client/__tests__/Api.test.mjs" to
                TemplateResourceLoader.load("full-stack/src/client/__tests__/Api.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A single-package full-stack ReScript app: Hono + Drizzle (SQLite) on " +
                            "the server, Vite+/React on the client, with shared types in `src/shared/`.",
                    scripts =
                        listOf(
                            "dev" to "Run server and client together (via concurrently)",
                            "dev:server" to "Run only the Hono backend (with --watch)",
                            "dev:client" to "Run only the Vite+ client dev server",
                            "build" to "Bundle the client for production",
                            "test" to "Run Vitest (covers both server and client smoke tests)",
                            "db:generate" to "Generate Drizzle migration SQL",
                            "db:migrate" to "Apply pending migrations to the SQLite file",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Architecture" to TemplateResourceLoader.load("full-stack/readme/architecture.md"),
                            "Shared Types" to TemplateResourceLoader.load("full-stack/readme/shared-types.md"),
                            "Database" to
                                TemplateResourceLoader.load(
                                    "full-stack/readme/database.md",
                                    mapOf(
                                        "cmdDbGenerate" to ctx.runCmd("db:generate"),
                                        "cmdDbMigrate" to ctx.runCmd("db:migrate"),
                                    ),
                                ),
                            "Project Layout" to
                                TemplateResourceLoader.load("full-stack/readme/project-layout.md"),
                            "About Vite+" to TemplateResourceLoader.load("full-stack/readme/vite-plus.md"),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".env.example" to
                CommonFiles.envExample(
                    listOf(
                        "Local SQLite file (default) or a Turso libsql:// URL" to
                            "DATABASE_URL=file:./data/app.db",
                    ),
                ),
            ".gitignore" to
                CommonFiles.gitignore(
                    extra = listOf("data/", "dist/", ".vite/", "drizzle/", ".env"),
                ),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = false, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun fullStackDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        deps["@libsql/client"] = TemplateVersions.LIBSQL_CLIENT
        deps["drizzle-orm"] = TemplateVersions.DRIZZLE_ORM
        return deps
    }
}
