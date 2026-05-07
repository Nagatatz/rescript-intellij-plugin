package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ApiStrategy
import com.rescript.plugin.wizard.Database
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
 * The generator branches on two orthogonal dimensions:
 * - [TemplateContext.validationLibrary] selects the server Validation.res variant
 *   (zod or sury); applies in both API strategies.
 * - [TemplateContext.apiStrategy] selects the server/client shape:
 *   REST emits handwritten Hono routes + a fetch client; GRAPHQL mounts
 *   graphql-yoga on Hono (matching the standalone HONO_GRAPHQL template) and
 *   wires rescript-relay on the client for end-to-end typed queries.
 *
 * Static file content lives under `src/main/resources/templates/full-stack/` and is
 * loaded via [TemplateResourceLoader]; dynamic composition (package.json, README
 * scripts table, CI, rescript.json ppx-flags) stays in Kotlin.
 */
internal object FullStackTemplateFiles {
    /**
     * Generates Full-Stack template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val variantKey = ctx.validationLibrary.variantKey()
        return when (ctx.apiStrategy) {
            ApiStrategy.REST -> restFiles(ctx, variantKey)
            ApiStrategy.GRAPHQL -> graphqlFiles(ctx, variantKey)
        }
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun restFiles(
        ctx: TemplateContext,
        variantKey: String,
    ): Map<String, String> {
        val nameVar = mapOf("projectName" to ctx.projectName)
        return commonFiles(ctx, variantKey) +
            mapOf(
                "rescript.json" to restRescriptJson(ctx),
                "package.json" to restPackageJson(ctx),
                "src/server/Server.res" to TemplateResourceLoader.load("full-stack/src/server/Server.res"),
                "src/server/Routes.res" to TemplateResourceLoader.load("full-stack/src/server/Routes.res"),
                "src/client/App.res" to TemplateResourceLoader.load("full-stack/src/client/App.res", nameVar),
                "src/client/ApiClient.res" to TemplateResourceLoader.load("full-stack/src/client/ApiClient.res"),
                "src/client/ClientMain.res" to TemplateResourceLoader.load("full-stack/src/client/ClientMain.res"),
                "src/client/__tests__/Api.test.mjs" to
                    TemplateResourceLoader.load("full-stack/src/client/__tests__/Api.test.mjs"),
                "README.md" to restReadme(ctx),
                ".gitignore" to
                    CommonFiles.gitignore(
                        extra = listOf("data/", "dist/", ".vite/", "drizzle/", ".env"),
                    ),
            )
    }

    private fun graphqlFiles(
        ctx: TemplateContext,
        variantKey: String,
    ): Map<String, String> =
        commonFiles(ctx, variantKey) +
            mapOf(
                "rescript.json" to graphqlRescriptJson(ctx),
                "package.json" to graphqlPackageJson(ctx),
                "src/server/Server.res" to
                    TemplateResourceLoader.load("full-stack/api/graphql/src/server/Server.res"),
                "src/server/Yoga.res" to TemplateResourceLoader.load("common/graphql/Yoga.res"),
                "src/server/GraphqlSchema.res" to
                    TemplateResourceLoader.load("full-stack/api/graphql/src/server/GraphqlSchema.res"),
                "src/server/Resolvers.res" to
                    TemplateResourceLoader.load("full-stack/api/graphql/src/server/Resolvers.res"),
                "src/server/schema.graphql" to
                    TemplateResourceLoader.load("full-stack/api/graphql/src/server/schema.graphql"),
                "src/client/RelayEnvironment.res" to
                    TemplateResourceLoader.load("full-stack/api/graphql/src/client/RelayEnvironment.res"),
                "src/client/UsersListQuery.res" to
                    TemplateResourceLoader.load("full-stack/api/graphql/src/client/UsersListQuery.res"),
                "src/client/App.res" to TemplateResourceLoader.load("full-stack/api/graphql/src/client/App.res"),
                "src/client/ClientMain.res" to
                    TemplateResourceLoader.load("full-stack/api/graphql/src/client/ClientMain.res"),
                "relay.config.js" to
                    TemplateResourceLoader.load("full-stack/api/graphql/relay.config.js"),
                "README.md" to graphqlReadme(ctx),
                ".gitignore" to
                    CommonFiles.gitignore(
                        extra =
                            listOf(
                                "data/",
                                "dist/",
                                ".vite/",
                                "drizzle/",
                                ".env",
                                "src/client/__generated__/",
                            ),
                    ),
            )

    /**
     * Files shared across both API strategies. These are the schema, Db, Hono bindings,
     * shared types, CI, license, and other infrastructure pieces that do not change
     * between REST and GraphQL.
     */
    private fun commonFiles(
        ctx: TemplateContext,
        variantKey: String,
    ): Map<String, String> {
        val nameVar = mapOf("projectName" to ctx.projectName)
        val (schemaPath, drizzleConfigPath) =
            when (ctx.database) {
                Database.LIBSQL -> {
                    "full-stack/src/server/Schema.res" to "full-stack/drizzle.config.ts"
                }

                Database.POSTGRES -> {
                    "full-stack/variants/postgres/src/server/Schema.res" to
                        "full-stack/variants/postgres/drizzle.config.ts"
                }

                Database.MYSQL -> {
                    "full-stack/variants/mysql/src/server/Schema.res" to
                        "full-stack/variants/mysql/drizzle.config.ts"
                }
            }
        val envComment =
            when (ctx.database) {
                Database.LIBSQL -> "Local SQLite file (default) or a Turso libsql:// URL"
                Database.POSTGRES -> "Postgres connection string; matches the credentials in compose.yaml"
                Database.MYSQL -> "MySQL connection string; matches the credentials in compose.yaml"
            }
        val files =
            linkedMapOf(
                "index.html" to TemplateResourceLoader.load("full-stack/index.html", nameVar),
                "vite.config.mjs" to
                    ProjectFileBuilders.viteConfigWithProxy(
                        imports =
                            listOf(
                                """import { defineConfig } from "vite-plus";""",
                                """import react from "@vitejs/plugin-react";""",
                            ),
                    ),
                "drizzle.config.ts" to TemplateResourceLoader.load(drizzleConfigPath),
                "src/shared/Shared.res" to TemplateResourceLoader.load("full-stack/src/shared/Shared.res"),
                "src/server/ServerMain.res" to TemplateResourceLoader.load("full-stack/src/server/ServerMain.res"),
                "src/server/Hono.res" to ProjectFileBuilders.honoBindings(),
                "src/server/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
                "src/server/Schema.res" to TemplateResourceLoader.load(schemaPath),
                "src/server/Validation.res" to
                    TemplateResourceLoader.load("full-stack/variants/$variantKey/src/server/Validation.res"),
                "src/server/Db.res" to TemplateResourceLoader.load(CommonFiles.sharedDbResPath(ctx.database)),
                "src/server/__tests__/Server.test.mjs" to
                    TemplateResourceLoader.load("full-stack/src/server/__tests__/Server.test.mjs"),
                "vitest.config.mjs" to TemplateResourceLoader.load("full-stack/vitest.config.mjs"),
                "vitest.setup.mjs" to TemplateResourceLoader.load("full-stack/vitest.setup.mjs"),
                ".nvmrc" to CommonFiles.nvmrc(ctx),
                "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
                ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
                ".env.example" to
                    CommonFiles.envExample(
                        listOf(envComment to CommonFiles.defaultDatabaseUrl(ctx.database)),
                    ),
                ".editorconfig" to CommonFiles.editorconfig(),
                ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = false, hasTest = true),
            )
        CommonFiles.composeYaml(ctx.database)?.let { files["compose.yaml"] = it }
        return files
    }

    private fun restRescriptJson(ctx: TemplateContext): String =
        ProjectFileBuilders.rescriptJson(
            name = ctx.projectName,
            bsDependencies =
                listOf("@rescript/core", "@rescript/react") + ctx.validationBsDeps(),
            includeJsx = true,
            sources = fullStackSources(),
        )

    private fun graphqlRescriptJson(ctx: TemplateContext): String =
        ProjectFileBuilders.rescriptJson(
            name = ctx.projectName,
            bsDependencies =
                listOf("@rescript/core", "@rescript/react", "rescript-relay") +
                    ctx.validationBsDeps(),
            includeJsx = true,
            sources = fullStackSources(),
            ppxFlags = listOf("rescript-relay/ppx"),
        )

    private fun fullStackSources(): String =
        "  \"sources\": [\n" +
            "    {\"dir\": \"src/shared\", \"subdirs\": true},\n" +
            "    {\"dir\": \"src/server\", \"subdirs\": true},\n" +
            "    {\"dir\": \"src/client\", \"subdirs\": true}\n" +
            "  ],"

    private fun restPackageJson(ctx: TemplateContext): String =
        ProjectFileBuilders.packageJson(
            name = ctx.projectName,
            isPrivate = true,
            type = "module",
            packageManager = ctx.packageManagerSpec(),
            engines = mapOf("node" to ctx.nodeEngine),
            dependencies = restDependencies(ctx),
            devDependencies = commonDevDependencies(),
            scripts =
                linkedMapOf(
                    // `npm:res:dev` runs `rescript -w` so edits to .res files actually
                    // recompile while `dev` is running. Without it, `dev:server` keeps
                    // serving stale `.res.mjs` and `dev:client` never sees client-side
                    // ReScript changes — a footgun previous versions hit.
                    "dev" to "concurrently \"npm:res:dev\" \"npm:dev:server\" \"npm:dev:client\"",
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
        )

    private fun graphqlPackageJson(ctx: TemplateContext): String =
        ProjectFileBuilders.packageJson(
            name = ctx.projectName,
            isPrivate = true,
            type = "module",
            packageManager = ctx.packageManagerSpec(),
            engines = mapOf("node" to ctx.nodeEngine),
            dependencies = graphqlDependencies(ctx),
            devDependencies =
                commonDevDependencies() +
                    linkedMapOf(
                        "relay-compiler" to TemplateVersions.RELAY_COMPILER,
                    ),
            scripts =
                linkedMapOf(
                    "dev" to
                        "concurrently \"npm:res:dev\" \"npm:dev:server\" \"npm:dev:client\" " +
                        "\"npm:relay:watch\"",
                    "dev:server" to "node --watch src/server/ServerMain.res.mjs",
                    "dev:client" to "vp dev",
                    "build" to "vp build",
                    "preview" to "vp preview",
                    "test" to "vitest run",
                    "test:coverage" to "vitest run --coverage",
                    "db:generate" to "drizzle-kit generate",
                    "db:migrate" to "drizzle-kit migrate",
                    "relay" to "relay-compiler",
                    "relay:watch" to "relay-compiler --watch",
                    "res:build" to "rescript",
                    "res:clean" to "rescript clean",
                    "res:dev" to "rescript -w",
                ),
        )

    private fun restDependencies(ctx: TemplateContext): LinkedHashMap<String, String> {
        val deps = baseServerDependencies(ctx)
        return deps
    }

    private fun graphqlDependencies(ctx: TemplateContext): LinkedHashMap<String, String> {
        val deps = baseServerDependencies(ctx)
        deps["graphql"] = TemplateVersions.GRAPHQL
        deps["graphql-yoga"] = TemplateVersions.GRAPHQL_YOGA
        deps["rescript-relay"] = TemplateVersions.RESCRIPT_RELAY
        return deps
    }

    private fun baseServerDependencies(ctx: TemplateContext): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        when (ctx.validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        val (driverPkg, driverVer) = CommonFiles.databaseDriver(ctx.database)
        deps[driverPkg] = driverVer
        deps["drizzle-orm"] = TemplateVersions.DRIZZLE_ORM
        return deps
    }

    private fun commonDevDependencies(): LinkedHashMap<String, String> =
        linkedMapOf(
            "concurrently" to TemplateVersions.CONCURRENTLY,
            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
            "vite" to TemplateVersions.VITE,
            "vite-plus" to TemplateVersions.VITE_PLUS,
            "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
            "vitest" to TemplateVersions.VITEST,
            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
        )

    private fun restReadme(ctx: TemplateContext): String =
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
        )

    private fun graphqlReadme(ctx: TemplateContext): String =
        CommonFiles.readme(
            ctx = ctx,
            description =
                "A single-package full-stack ReScript app: Hono + graphql-yoga + Drizzle " +
                    "(SQLite) on the server, Vite+/React with rescript-relay on the client, " +
                    "with shared types in `src/shared/`.",
            scripts =
                listOf(
                    "dev" to "Run server, client, and relay-compiler --watch together",
                    "dev:server" to "Run only the Hono backend (with --watch)",
                    "dev:client" to "Run only the Vite+ client dev server",
                    "relay" to "Run the Relay compiler once (emits typed %relay artifacts)",
                    "relay:watch" to "Run the Relay compiler in watch mode",
                    "build" to "Bundle the client for production",
                    "test" to "Run Vitest",
                    "db:generate" to "Generate Drizzle migration SQL",
                    "db:migrate" to "Apply pending migrations to the SQLite file",
                    "res:dev" to "Watch ReScript sources",
                ),
            extraSections =
                listOf(
                    "Architecture" to
                        TemplateResourceLoader.load("full-stack/api/graphql/readme/architecture.md"),
                    "Shared Types" to TemplateResourceLoader.load("full-stack/readme/shared-types.md"),
                    "GraphQL" to
                        TemplateResourceLoader.load("full-stack/api/graphql/readme/graphql.md"),
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
        )
}
