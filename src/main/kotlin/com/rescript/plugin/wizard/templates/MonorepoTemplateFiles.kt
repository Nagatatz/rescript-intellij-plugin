package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.Database
import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a full-stack monorepo (Hono backend + Vite+ React frontend).
 *
 * Layout:
 * - `packages/shared` — shared ReScript types
 * - `packages/server` — Hono + Node.js HTTP server with runtime body validation
 * - `packages/client` — React frontend bundled by Vite+
 *
 * Workspace plumbing adapts to the selected package manager: pnpm gets a `pnpm-workspace.yaml`
 * file, while npm/yarn use the `workspaces` field in the root `package.json`. The server's
 * `Validation.res` is selected from variants/{zod,sury}/ via [TemplateContext.validationLibrary].
 *
 * Static file content lives under `src/main/resources/templates/monorepo/` and is
 * loaded via [TemplateResourceLoader]; PM-specific dispatch (workspace deps, per-workspace
 * commands, package.json composition) stays in Kotlin.
 */
internal object MonorepoTemplateFiles {
    /**
     * Generates Monorepo template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val name = ctx.projectName
        val pm = ctx.packageManager
        val devCommand = devScript(pm)
        val workspaceField = if (pm == PackageManager.PNPM) null else listOf("packages/*")
        val nameVar = mapOf("projectName" to name)
        val variantKey = ctx.validationLibrary.variantKey()

        return buildMap {
            put(
                "package.json",
                ProjectFileBuilders.packageJson(
                    name = name,
                    workspaces = workspaceField,
                    isPrivate = true,
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    scripts =
                        linkedMapOf(
                            "dev" to devCommand,
                            "dev:server" to perWorkspaceCmd(pm, "server", "dev"),
                            "dev:client" to perWorkspaceCmd(pm, "client", "dev"),
                            "build:client" to perWorkspaceCmd(pm, "client", "build"),
                            "test" to allWorkspacesTestCmd(pm),
                            "test:coverage" to allWorkspacesCoverageCmd(pm),
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies = linkedMapOf("concurrently" to TemplateVersions.CONCURRENTLY),
                ),
            )
            if (pm == PackageManager.PNPM) {
                put("pnpm-workspace.yaml", "packages:\n  - \"packages/*\"\n")
            }
            // shared
            put(
                "packages/shared/rescript.json",
                ProjectFileBuilders.rescriptJson(
                    name = "@$name/shared",
                    // Wrap shared's modules under a `Shared` namespace so consumers
                    // (server / client) can reference `Shared.Types.user` rather than
                    // colliding on flat module names like `Types` or `Api`.
                    namespace = "Shared",
                ),
            )
            put(
                "packages/shared/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$name/shared",
                    type = "module",
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "@rescript/runtime" to TemplateVersions.RESCRIPT_RUNTIME,
                        ),
                ),
            )
            put(
                "packages/shared/src/Types.res",
                TemplateResourceLoader.load("monorepo/packages/shared/src/Types.res"),
            )
            put(
                "packages/shared/src/Api.res",
                TemplateResourceLoader.load("monorepo/packages/shared/src/Api.res"),
            )
            // server
            put(
                "packages/server/rescript.json",
                ProjectFileBuilders.rescriptJson(
                    name = "@$name/server",
                    bsDependencies =
                        listOf("@rescript/core", "@$name/shared") + ctx.validationBsDeps(),
                ),
            )
            put(
                "packages/server/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$name/server",
                    type = "module",
                    dependencies = monorepoServerDependencies(ctx, name),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            // ServerMain.res calls `Server.start()`; importing
                            // Server.res alone has no side effects so vitest stays happy.
                            "start" to "node src/ServerMain.res.mjs",
                            // `dev` must run rescript -w alongside node --watch so that
                            // edits to .res files actually rebuild while the API restarts.
                            "dev" to
                                "concurrently \"npm:res:dev\" \"node --watch src/ServerMain.res.mjs\"",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "db:generate" to "drizzle-kit generate",
                            "db:migrate" to "drizzle-kit migrate",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            )
            put("packages/server/src/Hono.res", ProjectFileBuilders.honoBindings())
            put("packages/server/src/HonoNodeServer.res", ProjectFileBuilders.honoNodeServerBindings())
            put(
                "packages/server/src/Schema.res",
                TemplateResourceLoader.load(
                    when (ctx.database) {
                        Database.LIBSQL -> "monorepo/packages/server/src/Schema.res"
                        Database.POSTGRES -> "monorepo/variants/postgres/packages/server/src/Schema.res"
                        Database.MYSQL -> "monorepo/variants/mysql/packages/server/src/Schema.res"
                    },
                ),
            )
            put(
                "packages/server/src/Db.res",
                TemplateResourceLoader.load(CommonFiles.sharedDbResPath(ctx.database)),
            )
            put(
                "packages/server/src/Validation.res",
                TemplateResourceLoader.load(
                    "monorepo/variants/$variantKey/packages/server/src/Validation.res",
                ),
            )
            put(
                "packages/server/src/Server.res",
                TemplateResourceLoader.load("monorepo/packages/server/src/Server.res", nameVar),
            )
            put(
                "packages/server/src/ServerMain.res",
                TemplateResourceLoader.load("monorepo/packages/server/src/ServerMain.res"),
            )
            put(
                "packages/server/src/__tests__/Server.test.mjs",
                TemplateResourceLoader.load("monorepo/packages/server/src/__tests__/Server.test.mjs"),
            )
            put(
                "packages/server/vitest.config.mjs",
                TemplateResourceLoader.load("monorepo/packages/server/vitest.config.mjs"),
            )
            put(
                "packages/server/vitest.setup.mjs",
                TemplateResourceLoader.load("monorepo/packages/server/vitest.setup.mjs"),
            )
            put(
                "packages/server/drizzle.config.ts",
                TemplateResourceLoader.load(
                    when (ctx.database) {
                        Database.LIBSQL -> "monorepo/packages/server/drizzle.config.ts"
                        Database.POSTGRES -> "monorepo/variants/postgres/packages/server/drizzle.config.ts"
                        Database.MYSQL -> "monorepo/variants/mysql/packages/server/drizzle.config.ts"
                    },
                ),
            )
            put(
                "packages/server/.env.example",
                CommonFiles.envExample(
                    listOf(
                        when (ctx.database) {
                            Database.LIBSQL -> {
                                "Local SQLite file (default) or a Turso libsql:// URL"
                            }

                            Database.POSTGRES -> {
                                "Postgres connection string; matches the credentials in compose.yaml"
                            }

                            Database.MYSQL -> {
                                "MySQL connection string; matches the credentials in compose.yaml"
                            }
                        } to CommonFiles.defaultDatabaseUrl(ctx.database),
                    ),
                ),
            )
            // client (Vite+)
            put(
                "packages/client/rescript.json",
                ProjectFileBuilders.rescriptJson(
                    name = "@$name/client",
                    bsDependencies = listOf("@rescript/core", "@rescript/react", "@$name/shared"),
                    includeJsx = true,
                ),
            )
            put(
                "packages/client/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$name/client",
                    type = "module",
                    isPrivate = true,
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "@rescript/runtime" to TemplateVersions.RESCRIPT_RUNTIME,
                            "@rescript/react" to TemplateVersions.RESCRIPT_REACT,
                            "@$name/shared" to workspaceDep(pm),
                            "react" to TemplateVersions.REACT,
                            "react-dom" to TemplateVersions.REACT_DOM,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                            "vite" to TemplateVersions.VITE,
                            "vite-plus" to TemplateVersions.VITE_PLUS,
                            "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            // Pair vp dev with rescript -w so edits to .res files
                            // recompile and HMR picks up the new .res.mjs.
                            "dev" to "concurrently \"npm:res:dev\" \"vp dev\"",
                            "build" to "vp build",
                            "preview" to "vp preview",
                            "test" to "vp test",
                            "test:coverage" to "vp test --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            )
            put(
                "packages/client/index.html",
                TemplateResourceLoader.load("monorepo/packages/client/index.html", nameVar),
            )
            put(
                "packages/client/vite.config.mjs",
                ProjectFileBuilders.viteConfigWithProxy(
                    imports =
                        listOf(
                            """import { defineConfig } from "vite-plus";""",
                            """import react from "@vitejs/plugin-react";""",
                        ),
                ),
            )
            put(
                "packages/client/src/App.res",
                TemplateResourceLoader.load("monorepo/packages/client/src/App.res", nameVar),
            )
            put(
                "packages/client/src/ApiClient.res",
                TemplateResourceLoader.load("monorepo/packages/client/src/ApiClient.res"),
            )
            put(
                "packages/client/src/Main.res",
                TemplateResourceLoader.load("monorepo/packages/client/src/Main.res"),
            )
            put(
                "packages/client/src/__tests__/ApiClient.test.mjs",
                TemplateResourceLoader.load("monorepo/packages/client/src/__tests__/ApiClient.test.mjs"),
            )
            put(
                "README.md",
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A full-stack monorepo with a Hono server and a Vite+/React client written in ReScript.",
                    scripts =
                        listOf(
                            "dev" to "Run server and client concurrently",
                            "dev:server" to "Run only the Hono server",
                            "dev:client" to "Run only the Vite+ client",
                            "build:client" to "Build the client for production",
                            "test" to "Run tests across all workspaces",
                        ),
                    extraSections =
                        listOf(
                            "Workspaces" to workspacesNote(pm),
                            "Database" to
                                TemplateResourceLoader.load(
                                    "monorepo/readme/database.md",
                                    mapOf(
                                        "cmdDbGenerate" to perWorkspaceCmd(pm, "server", "db:generate"),
                                        "cmdDbMigrate" to perWorkspaceCmd(pm, "server", "db:migrate"),
                                    ),
                                ),
                            "About Vite+" to TemplateResourceLoader.load("monorepo/readme/vite-plus.md"),
                            "Networking" to TemplateResourceLoader.load("monorepo/readme/networking.md"),
                        ),
                ),
            )
            put(".nvmrc", CommonFiles.nvmrc(ctx))
            put("LICENSE", CommonFiles.mitLicense(ctx, holder = name))
            put(".github/dependabot.yml", CommonFiles.dependabotYaml())
            put(
                ".gitignore",
                CommonFiles.gitignore(
                    extra = listOf("dist/", ".vite/", "packages/*/dist/", "packages/*/data/", ".env"),
                ),
            )
            put(".editorconfig", CommonFiles.editorconfig())
            put(".github/workflows/ci.yml", CommonFiles.ciWorkflow(ctx, hasBuild = false, hasTest = true))
            CommonFiles.composeYaml(ctx.database)?.let { put("compose.yaml", it) }
        }
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun monorepoServerDependencies(
        ctx: TemplateContext,
        name: String,
    ): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@$name/shared"] = workspaceDep(ctx.packageManager)
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

    private fun devScript(pm: PackageManager): String {
        val client = perWorkspaceCmd(pm, "client", "dev")
        val server = perWorkspaceCmd(pm, "server", "dev")
        return "concurrently \\\"$server\\\" \\\"$client\\\""
    }

    /**
     * Returns the dependency-version string used to refer to a sibling workspace package.
     *
     * pnpm and Yarn 3+ recognize `workspace:*`; npm interprets the same dependency as a
     * regular semver range, so we fall back to `*` for npm to keep `npm install` happy.
     */
    private fun workspaceDep(pm: PackageManager): String =
        when (pm) {
            PackageManager.PNPM -> "workspace:*"
            PackageManager.YARN -> "workspace:*"
            PackageManager.BUN -> "workspace:*"
            PackageManager.NPM -> "*"
        }

    private fun perWorkspaceCmd(
        pm: PackageManager,
        pkg: String,
        script: String,
    ): String =
        when (pm) {
            PackageManager.PNPM -> "pnpm --filter ./packages/$pkg $script"
            PackageManager.YARN -> "yarn workspace ./packages/$pkg run $script"
            PackageManager.NPM -> "npm --workspace packages/$pkg run $script"
            PackageManager.BUN -> "bun --filter ./packages/$pkg $script"
        }

    /**
     * Returns the root-level test command that fans out to every workspace that defines
     * its own `test` script. Each package manager has a different invocation style.
     */
    private fun allWorkspacesTestCmd(pm: PackageManager): String =
        when (pm) {
            PackageManager.PNPM -> "pnpm -r run test"
            PackageManager.YARN -> "yarn workspaces foreach -A run test"
            PackageManager.NPM -> "npm --workspaces run test --if-present"
            PackageManager.BUN -> "bun --filter '*' run test"
        }

    /**
     * Root-level `test:coverage` that fans out to every workspace exposing the same script.
     */
    private fun allWorkspacesCoverageCmd(pm: PackageManager): String =
        when (pm) {
            PackageManager.PNPM -> "pnpm -r run test:coverage"
            PackageManager.YARN -> "yarn workspaces foreach -A run test:coverage"
            PackageManager.NPM -> "npm --workspaces run test:coverage --if-present"
            PackageManager.BUN -> "bun --filter '*' run test:coverage"
        }

    private fun workspacesNote(pm: PackageManager): String =
        when (pm) {
            PackageManager.PNPM -> "This project uses pnpm workspaces (see `pnpm-workspace.yaml`)."
            PackageManager.YARN -> "This project uses Yarn workspaces (see the `workspaces` field in `package.json`)."
            PackageManager.NPM -> "This project uses npm workspaces (see the `workspaces` field in `package.json`)."
            PackageManager.BUN -> "This project uses Bun workspaces (see the `workspaces` field in `package.json`)."
        }
}
