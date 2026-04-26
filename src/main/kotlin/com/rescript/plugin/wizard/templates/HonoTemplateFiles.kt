package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a Hono web service running on Node.js.
 *
 * Ships a ready-to-extend REST API:
 * - SQLite via libsql + Drizzle ORM (schema, queries, drizzle-kit migrations)
 * - Runtime HTTP body validation via `Validation.res` (zod or sury, selected in Wizard)
 * - Logger middleware + structured error handling
 * - Static OpenAPI 3.1 `/openapi.json` + Scalar UI mounted at `/docs`
 *
 * The generated project is intended to demonstrate how the common day-two concerns
 * (persistence, validation, documentation) fit together without users needing to
 * reverse-engineer three different framework docs.
 *
 * Static file content lives under `src/main/resources/templates/hono/` and is
 * loaded via [TemplateResourceLoader]; only dynamic composition (package.json,
 * README assembly, CI) is synthesized in Kotlin. Validation-library-specific files
 * (`Validation.res`, `ZodOpenapi.res`) live under `variants/<key>/` and are
 * selected by [TemplateContext.validationLibrary].
 */
internal object HonoTemplateFiles {
    /**
     * Generates Hono template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val variantKey = ctx.validationLibrary.variantKey()
        val files =
            linkedMapOf(
                "rescript.json" to
                    ProjectFileBuilders.rescriptJson(
                        name = ctx.projectName,
                        bsDependencies = listOf("@rescript/core") + ctx.validationBsDeps(),
                    ),
                "package.json" to
                    ProjectFileBuilders.packageJson(
                        name = ctx.projectName,
                        isPrivate = true,
                        type = "module",
                        packageManager = ctx.packageManagerSpec(),
                        engines = mapOf("node" to ctx.nodeEngine),
                        dependencies = honoDependencies(ctx.validationLibrary),
                        devDependencies =
                            linkedMapOf(
                                "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                                "vitest" to TemplateVersions.VITEST,
                                "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                            ),
                        scripts =
                            linkedMapOf(
                                // ServerMain.res is the entry point that calls
                                // `Server.start()`; importing Server.res itself does
                                // not bind a port (so vitest can `import` it freely).
                                "start" to "node src/ServerMain.res.mjs",
                                "dev" to "node --watch src/ServerMain.res.mjs",
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
                "src/Validation.res" to TemplateResourceLoader.load("hono/variants/$variantKey/src/Validation.res"),
                "src/Db.res" to TemplateResourceLoader.load("common/db/Db.res"),
                "src/Scalar.res" to TemplateResourceLoader.load("hono/src/Scalar.res"),
                "src/Routes.res" to TemplateResourceLoader.load("hono/src/Routes.res"),
                "src/Server.res" to TemplateResourceLoader.load("hono/src/Server.res"),
                "src/ServerMain.res" to TemplateResourceLoader.load("hono/src/ServerMain.res"),
                "drizzle.config.ts" to TemplateResourceLoader.load("hono/drizzle.config.ts"),
                "src/__tests__/Server.test.mjs" to TemplateResourceLoader.load("hono/src/__tests__/Server.test.mjs"),
                "vitest.config.mjs" to TemplateResourceLoader.load("hono/vitest.config.mjs"),
                "vitest.setup.mjs" to TemplateResourceLoader.load("hono/vitest.setup.mjs"),
            )
        if (ctx.validationLibrary == ValidationLibrary.ZOD) {
            files["src/ZodOpenapi.res"] = TemplateResourceLoader.load("hono/variants/zod/src/ZodOpenapi.res")
        }
        files["README.md"] =
            CommonFiles.readme(
                ctx = ctx,
                description = honoDescription(ctx.validationLibrary),
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
            )
        files[".nvmrc"] = CommonFiles.nvmrc(ctx)
        files["LICENSE"] = CommonFiles.mitLicense(ctx, holder = ctx.projectName)
        files[".github/dependabot.yml"] = CommonFiles.dependabotYaml()
        files[".env.example"] =
            CommonFiles.envExample(
                listOf(
                    "Local SQLite file (default) or a Turso libsql:// URL" to
                        "DATABASE_URL=file:./data/app.db",
                ),
            )
        files[".gitignore"] = CommonFiles.gitignore(extra = listOf("dist/", "data/", "drizzle/", ".env"))
        files[".editorconfig"] = CommonFiles.editorconfig()
        files[".github/workflows/ci.yml"] = CommonFiles.ciWorkflow(ctx, hasTest = true)
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun honoDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        if (validationLibrary == ValidationLibrary.ZOD) {
            deps["@hono/zod-openapi"] = TemplateVersions.HONO_ZOD_OPENAPI
        }
        deps["@scalar/hono-api-reference"] = TemplateVersions.SCALAR_HONO_API_REFERENCE
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        deps["@libsql/client"] = TemplateVersions.LIBSQL_CLIENT
        deps["drizzle-orm"] = TemplateVersions.DRIZZLE_ORM
        return deps
    }

    private fun honoDescription(validationLibrary: ValidationLibrary): String {
        val validationBlurb =
            when (validationLibrary) {
                ValidationLibrary.ZOD -> "zod validation"
                ValidationLibrary.SURY -> "sury validation"
            }
        return "A Hono REST API with SQLite (Drizzle), $validationBlurb, structured logging, " +
            "and OpenAPI 3 docs served at /docs via Scalar UI."
    }
}
