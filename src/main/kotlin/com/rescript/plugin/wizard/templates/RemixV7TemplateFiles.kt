package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a React Router v7 app in Framework mode
 * (the continuation of Remix).
 *
 * The template puts loaders, root layout, and components under `app/`, includes a
 * ReScript Greeting component plus a typed loader written in ReScript, and wires the
 * SSR pipeline via `@react-router/dev` Vite plugin. The validation library combo is
 * hidden because React Router relies on standard Web FormData / Request primitives;
 * users can layer zod, valibot, or sury on top as needed.
 */
internal object RemixV7TemplateFiles {
    private const val RESOURCE_ROOT = "remix-v7"

    /**
     * Generates React Router v7 (Framework mode) template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val readme =
            CommonFiles.readme(
                ctx = ctx,
                description =
                    "A React Router v7 (Framework mode) app with ReScript components and a " +
                        "ReScript loader, served through @react-router/dev's Vite SSR pipeline.",
                scripts =
                    listOf(
                        "dev" to "Start React Router dev server with the ReScript watcher",
                        "build" to "Compile ReScript and run react-router build",
                        "start" to "Run the production bundle via @react-router/serve",
                        "typecheck" to "Generate route types and run tsc --noEmit",
                        "test" to "Run Vitest",
                    ),
                extraSections =
                    listOf(
                        "Loaders and Actions" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/loaders-actions.md"),
                        "File-based Routing" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/file-routing.md"),
                    ),
            )
        return mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    sources = appSources(),
                    includeJsx = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = remixDependencies(),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"rescript -w\" \"react-router dev\"",
                            "build" to "rescript && react-router build",
                            "start" to "react-router-serve ./build/server/index.js",
                            "typecheck" to "react-router typegen && tsc --noEmit",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies =
                        linkedMapOf(
                            "@react-router/dev" to TemplateVersions.REACT_ROUTER_DEV,
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "vite" to TemplateVersions.VITE,
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
                    "react-router.config.ts",
                    "tsconfig.json",
                    "rescript-modules.d.ts",
                    "app/root.tsx",
                    "app/routes.ts",
                ),
            ) +
            mapOf(
                "app/routes/home.tsx" to
                    TemplateResourceLoader.load("$RESOURCE_ROOT/app/routes/home.tsx", projectVars),
            ) +
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf(
                    "app/components/Greet.res",
                    "app/loaders/HomeLoader.res",
                    "app/__tests__/Greet.test.mjs",
                ),
            ) +
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra =
                    listOf(
                        ".react-router/",
                        "build/",
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

    private fun appSources(): String =
        "  \"sources\": {\n" +
            "    \"dir\": \"app\",\n" +
            "    \"subdirs\": true\n" +
            "  },"

    private fun remixDependencies(): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["react-router"] = TemplateVersions.REACT_ROUTER
        deps["@react-router/node"] = TemplateVersions.REACT_ROUTER_NODE
        deps["@react-router/serve"] = TemplateVersions.REACT_ROUTER_SERVE
        deps["isbot"] = TemplateVersions.ISBOT
        return deps
    }
}
