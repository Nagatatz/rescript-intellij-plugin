package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a Next.js application powered by ReScript via genType.
 *
 * The template wires `rescript -w` and `next dev` together via concurrently and emits a
 * minimal app router page, README, .gitignore, .editorconfig, and CI workflow. The
 * `/api/greet` Route Handler is fully ReScript: a minimal `NextServer.res` binding wraps
 * `next/server`, `GreetRoute.res` hosts the handler body, and `route.ts` is reduced to a
 * one-line re-export shim. Runtime body validation is provided by `Validation.res` (zod
 * or sury, selected via [TemplateContext.validationLibrary]).
 */
internal object NextjsTemplateFiles {
    private const val RESOURCE_ROOT = "nextjs"

    /**
     * Generates Next.js template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val variantKey = ctx.validationLibrary.variantKey()
        return mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                    includeGenType = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = nextjsDependencies(ctx.validationLibrary),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"rescript -w\" \"next dev\"",
                            "build" to "rescript && next build",
                            "start" to "next start",
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
            "next.config.mjs" to TemplateResourceLoader.load("$RESOURCE_ROOT/next.config.mjs"),
            "tsconfig.json" to TemplateResourceLoader.load("$RESOURCE_ROOT/tsconfig.json"),
            "rescript-modules.d.ts" to TemplateResourceLoader.load("$RESOURCE_ROOT/rescript-modules.d.ts"),
            "src/app/page.tsx" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/app/page.tsx"),
            "src/app/loading.tsx" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/app/loading.tsx"),
            "src/app/client/GreetForm.tsx" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/app/client/GreetForm.tsx"),
            "src/app/api/greet/route.ts" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/app/api/greet/route.ts"),
            "src/app/api/greet/GreetRoute.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/app/api/greet/GreetRoute.res"),
            "src/app/api/greet/Validation.res" to
                TemplateResourceLoader.load(
                    "$RESOURCE_ROOT/variants/$variantKey/src/app/api/greet/Validation.res",
                ),
            "src/NextServer.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/NextServer.res"),
            "src/App.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/App.res", projectVars),
            "src/GreetForm.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/GreetForm.res"),
            "src/Fetch.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Fetch.res"),
            "src/__tests__/App.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A Next.js app with ReScript components exposed via genType.",
                    scripts =
                        listOf(
                            "dev" to "Start Next.js dev server with ReScript watcher",
                            "build" to "Compile ReScript and build Next.js for production",
                            "start" to "Run the production Next.js server",
                            "test" to "Run Vitest",
                        ),
                    extraSections =
                        listOf(
                            "Server vs Client Components" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/server-vs-client.md"),
                            "Route Handlers" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/route-handler.md"),
                            "Project Layout" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/project-layout.md"),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".next/", "out/", ".env*.local")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun nextjsDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["next"] = TemplateVersions.NEXTJS
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
