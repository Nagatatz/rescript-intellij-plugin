package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a Hono + GraphQL Yoga + Drizzle SQLite project.
 *
 * Ships a ready-to-extend GraphQL API:
 * - GraphQL Yoga mounted on Hono at `/graphql` (with GraphiQL at the same URL)
 * - SQLite via libsql + Drizzle ORM (schema, queries, drizzle-kit migrations)
 * - Users type with query/mutation resolvers (users / user(id) / createUser / deleteUser)
 * - `docs:graphql` script runs graphql-markdown against the schema for human-readable docs
 * - Runtime mutation input validation via `Validation.res` (zod or sury, selected in Wizard)
 *
 * The generated project answers the common day-two question "how do I add a new GraphQL
 * type + resolver?" by showing an already-wired users type alongside the Drizzle table.
 *
 * Static file content lives under `src/main/resources/templates/hono-graphql/` and is
 * loaded via [TemplateResourceLoader]; only dynamic composition stays in Kotlin.
 * Validation-library-specific files live under `variants/<key>/` and are selected by
 * [TemplateContext.validationLibrary].
 */
internal object HonoGraphqlTemplateFiles {
    /**
     * Generates Hono GraphQL template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val variantKey = ctx.validationLibrary.variantKey()
        return mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = honoGraphqlDependencies(ctx.validationLibrary),
                    devDependencies =
                        linkedMapOf(
                            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                            "@graphql-markdown/cli" to TemplateVersions.GRAPHQL_MARKDOWN,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "docs:graphql" to
                                "graphql-markdown --schema=src/schema.graphql --base-url=./docs " +
                                "--root-path=./docs --group-by=kind",
                            "db:generate" to "drizzle-kit generate",
                            "db:migrate" to "drizzle-kit migrate",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Schema.res" to TemplateResourceLoader.load("hono-graphql/src/Schema.res"),
            "src/Validation.res" to
                TemplateResourceLoader.load("hono-graphql/variants/$variantKey/src/Validation.res"),
            "src/Db.res" to TemplateResourceLoader.load("common/db/Db.res"),
            "src/Yoga.res" to TemplateResourceLoader.load("common/graphql/Yoga.res"),
            "src/GraphqlSchema.res" to TemplateResourceLoader.load("hono-graphql/src/GraphqlSchema.res"),
            "src/Resolvers.res" to TemplateResourceLoader.load("hono-graphql/src/Resolvers.res"),
            "src/Server.res" to TemplateResourceLoader.load("hono-graphql/src/Server.res"),
            "src/schema.graphql" to TemplateResourceLoader.load("hono-graphql/src/schema.graphql"),
            "drizzle.config.ts" to TemplateResourceLoader.load("hono-graphql/drizzle.config.ts"),
            "src/__tests__/Server.test.mjs" to
                TemplateResourceLoader.load("hono-graphql/src/__tests__/Server.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A GraphQL API on Hono using graphql-yoga, backed by SQLite (Drizzle). " +
                            "Comes with GraphiQL at /graphql and human-readable docs generation.",
                    scripts =
                        listOf(
                            "dev" to "Run the GraphQL server with file watching",
                            "start" to "Run the server once",
                            "test" to "Run Vitest",
                            "docs:graphql" to "Generate docs/schema.md from src/schema.graphql",
                            "db:generate" to "Generate Drizzle migration SQL",
                            "db:migrate" to "Apply pending migrations to the SQLite file",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        run {
                            val dbVars =
                                mapOf(
                                    "cmdDbGenerate" to ctx.runCmd("db:generate"),
                                    "cmdDbMigrate" to ctx.runCmd("db:migrate"),
                                    "cmdDocsGraphql" to ctx.runCmd("docs:graphql"),
                                )
                            listOf(
                                "Try It" to TemplateResourceLoader.load("hono-graphql/readme/try-it.md"),
                                "Schema" to
                                    TemplateResourceLoader.load("hono-graphql/readme/schema.md", dbVars),
                                "Database" to
                                    TemplateResourceLoader.load("hono-graphql/readme/database.md", dbVars),
                                "Project Layout" to
                                    TemplateResourceLoader.load("hono-graphql/readme/project-layout.md"),
                            )
                        },
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".env.example" to
                CommonFiles.envExample(
                    listOf(
                        "Local SQLite file (default) or a Turso libsql:// URL" to
                            "DATABASE_URL=file:./data/app.db",
                    ),
                ),
            ".gitignore" to
                CommonFiles.gitignore(extra = listOf("data/", "docs/schema.md", "drizzle/", ".env")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun honoGraphqlDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        deps["graphql"] = TemplateVersions.GRAPHQL
        deps["graphql-yoga"] = TemplateVersions.GRAPHQL_YOGA
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        deps["@libsql/client"] = TemplateVersions.LIBSQL_CLIENT
        deps["drizzle-orm"] = TemplateVersions.DRIZZLE_ORM
        return deps
    }
}
