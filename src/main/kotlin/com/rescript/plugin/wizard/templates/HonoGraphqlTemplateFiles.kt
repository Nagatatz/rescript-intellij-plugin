package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.Database
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
        val (schemaPath, drizzleConfigPath) =
            when (ctx.database) {
                Database.LIBSQL -> {
                    "hono-graphql/src/Schema.res" to "hono-graphql/drizzle.config.ts"
                }

                Database.POSTGRES -> {
                    "hono-graphql/variants/postgres/src/Schema.res" to
                        "hono-graphql/variants/postgres/drizzle.config.ts"
                }

                Database.MYSQL -> {
                    "hono-graphql/variants/mysql/src/Schema.res" to
                        "hono-graphql/variants/mysql/drizzle.config.ts"
                }
            }
        val databaseReadmePath =
            when (ctx.database) {
                Database.LIBSQL -> "hono-graphql/readme/database.md"
                Database.POSTGRES -> "hono-graphql/variants/postgres/readme/database.md"
                Database.MYSQL -> "hono-graphql/variants/mysql/readme/database.md"
            }
        val envComment =
            when (ctx.database) {
                Database.LIBSQL -> "Local SQLite file (default) or a Turso libsql:// URL"
                Database.POSTGRES -> "Postgres connection string; matches the credentials in compose.yaml"
                Database.MYSQL -> "MySQL connection string; matches the credentials in compose.yaml"
            }
        val files =
            linkedMapOf<String, String>()
        files.putAll(
            mapOf(
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
                        dependencies = honoGraphqlDependencies(ctx),
                        devDependencies =
                            linkedMapOf(
                                "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                                "@graphql-markdown/cli" to TemplateVersions.GRAPHQL_MARKDOWN,
                                "vitest" to TemplateVersions.VITEST,
                                "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                            ),
                        scripts =
                            linkedMapOf(
                                // ServerMain.res calls `Server.start()`; importing
                                // Server.res alone has no side effects so vitest stays happy.
                                "start" to "node src/ServerMain.res.mjs",
                                "dev" to "node --watch src/ServerMain.res.mjs",
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
                "src/Schema.res" to TemplateResourceLoader.load(schemaPath),
                "src/Validation.res" to
                    TemplateResourceLoader.load("hono-graphql/variants/$variantKey/src/Validation.res"),
                "src/Db.res" to TemplateResourceLoader.load(CommonFiles.sharedDbResPath(ctx.database)),
                "src/Yoga.res" to TemplateResourceLoader.load("common/graphql/Yoga.res"),
                "src/GraphqlSchema.res" to TemplateResourceLoader.load("hono-graphql/src/GraphqlSchema.res"),
                "src/Resolvers.res" to TemplateResourceLoader.load("hono-graphql/src/Resolvers.res"),
                "src/Server.res" to TemplateResourceLoader.load("hono-graphql/src/Server.res"),
                "src/ServerMain.res" to TemplateResourceLoader.load("hono-graphql/src/ServerMain.res"),
                "src/schema.graphql" to TemplateResourceLoader.load("hono-graphql/src/schema.graphql"),
                "drizzle.config.ts" to TemplateResourceLoader.load(drizzleConfigPath),
                "src/__tests__/Server.test.mjs" to
                    TemplateResourceLoader.load("hono-graphql/src/__tests__/Server.test.mjs"),
                "vitest.config.mjs" to TemplateResourceLoader.load("hono-graphql/vitest.config.mjs"),
                "vitest.setup.mjs" to TemplateResourceLoader.load("hono-graphql/vitest.setup.mjs"),
                "README.md" to
                    CommonFiles.readme(
                        ctx = ctx,
                        description = honoGraphqlDescription(ctx),
                        scripts =
                            listOf(
                                "dev" to "Run the GraphQL server with file watching",
                                "start" to "Run the server once",
                                "test" to "Run Vitest",
                                "docs:graphql" to "Generate docs/schema.md from src/schema.graphql",
                                "db:generate" to "Generate Drizzle migration SQL",
                                "db:migrate" to
                                    when (ctx.database) {
                                        Database.LIBSQL -> "Apply pending migrations to the SQLite file"
                                        Database.POSTGRES -> "Apply pending migrations to the PostgreSQL database"
                                        Database.MYSQL -> "Apply pending migrations to the MySQL database"
                                    },
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
                                        TemplateResourceLoader.load(databaseReadmePath, dbVars),
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
                        listOf(envComment to CommonFiles.defaultDatabaseUrl(ctx.database)),
                    ),
                ".gitignore" to
                    CommonFiles.gitignore(extra = listOf("data/", "docs/schema.md", "drizzle/", ".env")),
                ".editorconfig" to CommonFiles.editorconfig(),
                ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
                "Dockerfile" to CommonFiles.serverDockerfile(ctx, port = 4000),
                ".dockerignore" to CommonFiles.dockerignore(),
            ),
        )
        CommonFiles.composeYaml(ctx.database)?.let { files["compose.yaml"] = it }
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun honoGraphqlDescription(ctx: TemplateContext): String {
        val dbBlurb =
            when (ctx.database) {
                Database.LIBSQL -> "libSQL / SQLite (Drizzle)"
                Database.POSTGRES -> "PostgreSQL (Drizzle, postgres-js)"
                Database.MYSQL -> "MySQL (Drizzle, mysql2)"
            }
        return "A GraphQL API on Hono using graphql-yoga, backed by $dbBlurb. " +
            "Comes with GraphiQL at /graphql and human-readable docs generation."
    }

    private fun honoGraphqlDependencies(ctx: TemplateContext): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        deps["graphql"] = TemplateVersions.GRAPHQL
        deps["graphql-yoga"] = TemplateVersions.GRAPHQL_YOGA
        when (ctx.validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        val (driverPkg, driverVer) = CommonFiles.databaseDriver(ctx.database)
        deps[driverPkg] = driverVer
        deps["drizzle-orm"] = TemplateVersions.DRIZZLE_ORM
        return deps
    }
}
