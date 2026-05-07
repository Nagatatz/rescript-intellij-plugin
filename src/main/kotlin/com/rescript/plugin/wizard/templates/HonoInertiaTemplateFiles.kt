package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.Database
import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a Hono backend paired with Inertia.js + React (CSR).
 *
 * The template scaffolds a server-driven SPA where Hono routes call `c.render(component, props)`
 * via the `@hono/inertia` middleware, and `@inertiajs/react` v3 mounts the matching React page
 * on the client. Vite+ (`vite-plus`) is used as the unified toolchain (`vp dev` / `vp build` /
 * `vp test` / `vp check`); SSR is intentionally out of scope for the initial template.
 *
 * ReScript 12's `Js.import` resolves statically, so Inertia's name-based page resolver is wired
 * through a small JS shim (`src/client/pages.js`) that uses `import.meta.glob`. Validation-library
 * specific files (`Validation.res`) live under `variants/<key>/` and are selected via
 * [TemplateContext.validationLibrary].
 *
 * Static file content lives under `src/main/resources/templates/hono-inertia/` and is loaded via
 * [TemplateResourceLoader]; only the dynamic composition (package.json, README assembly, CI) is
 * synthesized in Kotlin.
 */
internal object HonoInertiaTemplateFiles {
    private const val RESOURCE_ROOT = "hono-inertia"

    /**
     * Generates Hono + Inertia template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val variantKey = ctx.validationLibrary.variantKey()
        val (schemaPath, drizzleConfigPath) =
            when (ctx.database) {
                Database.LIBSQL -> {
                    "$RESOURCE_ROOT/src/Schema.res" to "$RESOURCE_ROOT/drizzle.config.ts"
                }

                Database.POSTGRES -> {
                    "$RESOURCE_ROOT/variants/postgres/src/Schema.res" to
                        "$RESOURCE_ROOT/variants/postgres/drizzle.config.ts"
                }

                Database.MYSQL -> {
                    "$RESOURCE_ROOT/variants/mysql/src/Schema.res" to
                        "$RESOURCE_ROOT/variants/mysql/drizzle.config.ts"
                }
            }
        val files =
            linkedMapOf(
                "rescript.json" to
                    ProjectFileBuilders.rescriptJson(
                        name = ctx.projectName,
                        bsDependencies =
                            listOf("@rescript/core", "@rescript/react") + ctx.validationBsDeps(),
                        includeJsx = true,
                    ),
                "package.json" to
                    ProjectFileBuilders.packageJson(
                        name = ctx.projectName,
                        isPrivate = true,
                        type = "module",
                        packageManager = ctx.packageManagerSpec(),
                        engines = mapOf("node" to ctx.nodeEngine),
                        dependencies = honoInertiaDependencies(ctx),
                        devDependencies =
                            linkedMapOf(
                                "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                                // Direct vite dep mirrors the documented Vite+ → Vite fallback.
                                "vite" to TemplateVersions.VITE,
                                "vite-plus" to TemplateVersions.VITE_PLUS,
                                "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
                                "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                            ),
                        scripts =
                            linkedMapOf(
                                "dev" to "vp dev",
                                "build" to "vp build",
                                "preview" to "vp preview",
                                "test" to "vp test",
                                "test:coverage" to "vp test --coverage",
                                "check" to "vp check",
                                "db:generate" to "drizzle-kit generate",
                                "db:migrate" to "drizzle-kit migrate",
                                "res:build" to "rescript",
                                "res:clean" to "rescript clean",
                                "res:dev" to "rescript -w",
                            ),
                    ),
                "vite.config.mjs" to TemplateResourceLoader.load("$RESOURCE_ROOT/vite.config.mjs"),
                "drizzle.config.ts" to TemplateResourceLoader.load(drizzleConfigPath),
                "src/Hono.res" to ProjectFileBuilders.honoBindings(),
                "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
                "src/HonoInertia.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/HonoInertia.res"),
                "src/InertiaBindings.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/InertiaBindings.res"),
                "src/Logger.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Logger.res"),
                "src/Schema.res" to TemplateResourceLoader.load(schemaPath),
                "src/Db.res" to TemplateResourceLoader.load(CommonFiles.sharedDbResPath(ctx.database)),
                "src/Validation.res" to
                    TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
                "src/Routes.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Routes.res"),
                "src/Server.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Server.res"),
                "src/ServerMain.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/ServerMain.res"),
                "src/client/Main.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/client/Main.res"),
                "src/client/pages.js" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/client/pages.js"),
                "src/client/MainLayout.res" to
                    TemplateResourceLoader.load("$RESOURCE_ROOT/src/client/MainLayout.res"),
                "src/client/Pages/Home.res" to
                    TemplateResourceLoader.load("$RESOURCE_ROOT/src/client/Pages/Home.res"),
                "src/client/Pages/About.res" to
                    TemplateResourceLoader.load("$RESOURCE_ROOT/src/client/Pages/About.res"),
                "src/__tests__/Server.test.mjs" to
                    TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Server.test.mjs"),
            )
        files["README.md"] =
            CommonFiles.readme(
                ctx = ctx,
                description = honoInertiaDescription(ctx),
                scripts =
                    listOf(
                        "dev" to "Run the Hono server and Vite+ dev server together",
                        "build" to "Produce a production bundle of the React client",
                        "test" to "Run the Vitest suite via Vite+",
                        "check" to "Lint, format-check, and typecheck via Vite+",
                        "db:generate" to "Generate Drizzle migration SQL from Schema.res",
                        "db:migrate" to
                            when (ctx.database) {
                                Database.LIBSQL -> "Apply pending migrations to the SQLite file"
                                Database.POSTGRES -> "Apply pending migrations to the PostgreSQL database"
                                Database.MYSQL -> "Apply pending migrations to the MySQL database"
                            },
                        "res:dev" to "Watch ReScript sources",
                    ),
                extraSections =
                    listOf(
                        "API" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/api.md"),
                        "Frontend" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/frontend.md"),
                        "Project Layout" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/project-layout.md"),
                        "About Vite+" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/viteplus.md"),
                    ),
            )
        files[".nvmrc"] = CommonFiles.nvmrc(ctx)
        files["LICENSE"] = CommonFiles.mitLicense(ctx, holder = ctx.projectName)
        files[".github/dependabot.yml"] = CommonFiles.dependabotYaml()
        val envComment =
            when (ctx.database) {
                Database.LIBSQL -> "Local SQLite file (default) or a Turso libsql:// URL"
                Database.POSTGRES -> "Postgres connection string; matches the credentials in compose.yaml"
                Database.MYSQL -> "MySQL connection string; matches the credentials in compose.yaml"
            }
        files[".env.example"] =
            CommonFiles.envExample(
                listOf(envComment to CommonFiles.defaultDatabaseUrl(ctx.database)),
            )
        files[".gitignore"] =
            CommonFiles.gitignore(extra = listOf("dist/", "data/", "drizzle/", ".env", ".vite/"))
        files[".editorconfig"] = CommonFiles.editorconfig()
        files[".github/workflows/ci.yml"] = CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true)
        CommonFiles.composeYaml(ctx.database)?.let { files["compose.yaml"] = it }
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun honoInertiaDependencies(ctx: TemplateContext): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["hono"] = TemplateVersions.HONO
        deps["@hono/node-server"] = TemplateVersions.HONO_NODE_SERVER
        deps["@hono/inertia"] = TemplateVersions.HONO_INERTIA
        deps["@inertiajs/react"] = TemplateVersions.INERTIA_REACT
        when (ctx.validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        val (driverPkg, driverVer) = CommonFiles.databaseDriver(ctx.database)
        deps[driverPkg] = driverVer
        deps["drizzle-orm"] = TemplateVersions.DRIZZLE_ORM
        return deps
    }

    private fun honoInertiaDescription(ctx: TemplateContext): String {
        val validationBlurb =
            when (ctx.validationLibrary) {
                ValidationLibrary.ZOD -> "zod validation"
                ValidationLibrary.SURY -> "sury validation"
            }
        val dbBlurb =
            when (ctx.database) {
                Database.LIBSQL -> "libSQL / SQLite"
                Database.POSTGRES -> "PostgreSQL"
                Database.MYSQL -> "MySQL"
            }
        return "A server-driven SPA: Hono routes render React pages directly through " +
            "@hono/inertia, with $validationBlurb and $dbBlurb (Drizzle) on the server " +
            "and Vite+ unifying dev / build / test / check."
    }
}
