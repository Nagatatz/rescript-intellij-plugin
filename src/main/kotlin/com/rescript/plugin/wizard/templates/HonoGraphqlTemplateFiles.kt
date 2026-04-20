package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Hono + GraphQL Yoga + Drizzle SQLite project.
 *
 * Ships a ready-to-extend GraphQL API:
 * - GraphQL Yoga mounted on Hono at `/graphql` (with GraphiQL at the same URL)
 * - SQLite via libsql + Drizzle ORM (schema, queries, drizzle-kit migrations)
 * - Users type with query/mutation resolvers (users / user(id) / createUser / deleteUser)
 * - `docs:graphql` script runs graphql-markdown against the schema for human-readable docs
 *
 * The generated project answers the common day-two question "how do I add a new GraphQL
 * type + resolver?" by showing an already-wired users type alongside the Drizzle table.
 *
 * Static file content lives under `src/main/resources/templates/hono-graphql/` and is
 * loaded via [TemplateResourceLoader]; only dynamic composition stays in Kotlin.
 */
internal object HonoGraphqlTemplateFiles {
    /**
     * Generates Hono GraphQL template files using the supplied [TemplateContext].
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
                            "graphql" to TemplateVersions.GRAPHQL,
                            "graphql-yoga" to TemplateVersions.GRAPHQL_YOGA,
                            "@libsql/client" to TemplateVersions.LIBSQL_CLIENT,
                            "drizzle-orm" to TemplateVersions.DRIZZLE_ORM,
                        ),
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
            "src/Db.res" to TemplateResourceLoader.load("hono-graphql/src/Db.res"),
            "src/Yoga.res" to TemplateResourceLoader.load("hono-graphql/src/Yoga.res"),
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
                        listOf(
                            "Try It" to TemplateResourceLoader.load("hono-graphql/readme/try-it.md"),
                            "Schema" to TemplateResourceLoader.load("hono-graphql/readme/schema.md"),
                            "Database" to
                                TemplateResourceLoader.load(
                                    "hono-graphql/readme/database.md",
                                    mapOf(
                                        "cmdDbGenerate" to ctx.runCmd("db:generate"),
                                        "cmdDbMigrate" to ctx.runCmd("db:migrate"),
                                    ),
                                ),
                            "Project Layout" to
                                TemplateResourceLoader.load("hono-graphql/readme/project-layout.md"),
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
                CommonFiles.gitignore(extra = listOf("data/", "docs/schema.md", "drizzle/", ".env")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
