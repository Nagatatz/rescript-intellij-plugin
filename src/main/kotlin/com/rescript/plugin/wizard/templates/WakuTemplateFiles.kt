package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Waku (RSC-first React framework) app.
 *
 * The template demonstrates the React Server Components boundary by shipping a
 * Server Component (`Greet.res`, used directly from the home page) alongside a
 * Client Component (`Counter.res`, wrapped by a thin TSX file declaring
 * `"use client"`). The validation library combo is hidden because RSC boundaries
 * typically guard their own inputs with whichever validator the developer prefers.
 */
internal object WakuTemplateFiles {
    private const val RESOURCE_ROOT = "waku"

    /**
     * Generates Waku template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
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
                    dependencies = wakuDependencies(),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"rescript -w\" \"waku dev\"",
                            "build" to "rescript && waku build",
                            "start" to "waku start",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                            "typescript" to TemplateVersions.TYPESCRIPT,
                            "@types/react" to TemplateVersions.REACT_TYPES,
                            "@types/react-dom" to TemplateVersions.REACT_DOM_TYPES,
                            "@types/node" to TemplateVersions.NODE_TYPES,
                        ),
                ),
            "tsconfig.json" to TemplateResourceLoader.load("$RESOURCE_ROOT/tsconfig.json"),
            "rescript-modules.d.ts" to TemplateResourceLoader.load("$RESOURCE_ROOT/rescript-modules.d.ts"),
            "src/pages/index.tsx" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/pages/index.tsx", projectVars),
            "src/components/Greet.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/components/Greet.res"),
            "src/components/Counter.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/components/Counter.res"),
            "src/components/CounterClient.tsx" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/components/CounterClient.tsx"),
            "src/__tests__/Greet.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Greet.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A Waku app demonstrating the React Server Components boundary with " +
                            "ReScript on both sides — a Greet Server Component plus a Counter " +
                            "Client Component wrapped by a thin \"use client\" TSX file.",
                    scripts =
                        listOf(
                            "dev" to "Start Waku dev server with the ReScript watcher",
                            "build" to "Compile ReScript and run waku build",
                            "start" to "Run the production server",
                            "test" to "Run Vitest",
                        ),
                    extraSections =
                        listOf(
                            "Server vs Client Components" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/server-vs-client.md"),
                            "RSC Basics" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/rsc-basics.md"),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to
                CommonFiles.gitignore(
                    extra =
                        listOf(
                            ".waku/",
                            "dist/",
                            ".env*.local",
                        ),
                ),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun wakuDependencies(): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["waku"] = TemplateVersions.WAKU
        return deps
    }
}
