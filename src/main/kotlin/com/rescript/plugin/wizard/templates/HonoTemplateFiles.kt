package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Hono web service running on Node.js.
 *
 * Ships a ready-to-extend REST API:
 * - SQLite via libsql + Drizzle ORM (schema, queries, drizzle-kit migrations)
 * - Zod schemas for POST/PUT body validation
 * - Logger middleware + structured error handling
 * - OpenAPI 3 spec auto-generated via `@hono/zod-openapi`
 * - Scalar UI mounted at `/docs` + raw spec at `/openapi.json`
 *
 * The generated project is intended to demonstrate how the common day-two concerns
 * (persistence, validation, documentation) fit together without users needing to
 * reverse-engineer three different framework docs.
 *
 * Static file content lives under `src/main/resources/templates/hono/` and is
 * loaded via [TemplateResourceLoader]; only dynamic composition (package.json,
 * README assembly, CI) is synthesized in Kotlin.
 */
internal object HonoTemplateFiles {
    /**
     * Generates Hono template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "hono" to TemplateVersions.HONO,
                            "@hono/node-server" to TemplateVersions.HONO_NODE_SERVER,
                            "@hono/zod-openapi" to TemplateVersions.HONO_ZOD_OPENAPI,
                            "@scalar/hono-api-reference" to TemplateVersions.SCALAR_HONO_API_REFERENCE,
                            "zod" to TemplateVersions.ZOD,
                            "@libsql/client" to TemplateVersions.LIBSQL_CLIENT,
                            "drizzle-orm" to TemplateVersions.DRIZZLE_ORM,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "db:generate" to "drizzle-kit generate",
                            "db:migrate" to "drizzle-kit migrate",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Logger.res" to TemplateResourceLoader.load("hono/src/Logger.res"),
            "src/Schema.res" to TemplateResourceLoader.load("hono/src/Schema.res"),
            "src/Db.res" to TemplateResourceLoader.load("hono/src/Db.res"),
            "src/ZodOpenapi.res" to TemplateResourceLoader.load("hono/src/ZodOpenapi.res"),
            "src/Scalar.res" to TemplateResourceLoader.load("hono/src/Scalar.res"),
            "src/Routes/Users.res" to TemplateResourceLoader.load("hono/src/Routes/Users.res"),
            "src/Server.res" to TemplateResourceLoader.load("hono/src/Server.res"),
            "drizzle.config.ts" to TemplateResourceLoader.load("hono/drizzle.config.ts"),
            "src/__tests__/Server.test.mjs" to TemplateResourceLoader.load("hono/src/__tests__/Server.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A Hono REST API with SQLite (Drizzle), Zod validation, structured logging, " +
                            "and auto-generated OpenAPI 3 docs served at /docs via Scalar UI.",
                    scripts =
                        listOf(
                            "dev" to "Run the server with file watching",
                            "start" to "Run the server once",
                            "test" to "Run Vitest",
                            "db:generate" to "Generate Drizzle migration SQL from Schema.res",
                            "db:migrate" to "Apply pending migrations to the SQLite file",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "API" to TemplateResourceLoader.load("hono/readme/api.md"),
                            "Database" to
                                TemplateResourceLoader.load(
                                    "hono/readme/database.md",
                                    mapOf(
                                        "cmdDbGenerate" to ctx.runCmd("db:generate"),
                                        "cmdDbMigrate" to ctx.runCmd("db:migrate"),
                                    ),
                                ),
                            "OpenAPI Docs" to TemplateResourceLoader.load("hono/readme/openapi.md"),
                            "Project Layout" to TemplateResourceLoader.load("hono/readme/project-layout.md"),
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
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "data/", "drizzle/", ".env")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
