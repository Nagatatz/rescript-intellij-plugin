package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a TanStack Start application.
 *
 * The template wires `@tanstack/react-start` (Vite-based full-stack React) with
 * `@tanstack/react-router` for file-based routing, and exposes a ReScript Greeting
 * component to the home route. A sample Server Function in `src/server/Greet.res`
 * demonstrates how ReScript code can be invoked from TanStack's RPC layer. The
 * validation library combo is hidden in the wizard step because TanStack Start ships
 * its own data flow primitives.
 */
internal object TanstackStartTemplateFiles {
    private const val RESOURCE_ROOT = "tanstack-start"

    /**
     * Generates TanStack Start template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val readme =
            CommonFiles.readme(
                ctx = ctx,
                description =
                    "A TanStack Start app with file-based routing, a sample Server Function, " +
                        "and ReScript components consumed from TSX routes.",
                scripts =
                    listOf(
                        "dev" to "Start Vite dev server with the ReScript watcher",
                        "build" to "Compile ReScript and build the app for production",
                        "start" to "Run the production server bundle",
                        "test" to "Run Vitest",
                    ),
                extraSections =
                    listOf(
                        "Server Functions" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/server-functions.md"),
                        "File-based Routing" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/file-routing.md"),
                    ),
            )
        return mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = tanstackDependencies(),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"rescript -w\" \"vite dev\"",
                            "build" to "rescript && vite build",
                            "start" to "node .output/server/index.mjs",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "vite" to TemplateVersions.VITE,
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                            "@tanstack/router-plugin" to TemplateVersions.TANSTACK_ROUTER_PLUGIN,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                            "typescript" to TemplateVersions.TYPESCRIPT,
                            "@types/react" to TemplateVersions.REACT_TYPES,
                            "@types/react-dom" to TemplateVersions.REACT_DOM_TYPES,
                            "@types/node" to TemplateVersions.NODE_TYPES,
                        ),
                ),
        ) +
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf(
                    "vite.config.ts",
                    "tsconfig.json",
                    "rescript-modules.d.ts",
                    "src/router.tsx",
                    "src/routes/__root.tsx",
                ),
            ) +
            mapOf(
                "src/routes/index.tsx" to
                    TemplateResourceLoader.load("$RESOURCE_ROOT/src/routes/index.tsx", projectVars),
            ) +
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf(
                    "src/components/Greeting.res",
                    "src/server/Greet.res",
                    "src/__tests__/Greeting.test.mjs",
                ),
            ) +
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra =
                    listOf(
                        ".tanstack/",
                        ".output/",
                        ".nitro/",
                        "dist/",
                        ".env*.local",
                    ),
                ciHasBuild = true,
                ciHasTest = true,
            )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun tanstackDependencies(): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["@tanstack/react-start"] = TemplateVersions.TANSTACK_REACT_START
        deps["@tanstack/react-router"] = TemplateVersions.TANSTACK_REACT_ROUTER
        return deps
    }
}
