package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

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
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        deps["next"] = TemplateVersions.NEXTJS
        val (validationName, validationVersion) = TemplateScaffold.validationDependency(ctx)
        deps[validationName] = validationVersion
        val readme =
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
            )
        val files = linkedMapOf<String, String>()
        files["rescript.json"] =
            ProjectFileBuilders.rescriptJson(
                name = ctx.projectName,
                bsDependencies =
                    listOf("@rescript/core", "@rescript/react") + ctx.validationBsDeps(),
                includeJsx = true,
                includeGenType = true,
            )
        files["package.json"] =
            ProjectFileBuilders.packageJson(
                name = ctx.projectName,
                isPrivate = true,
                packageManager = ctx.packageManagerSpec(),
                engines = mapOf("node" to ctx.nodeEngine),
                dependencies = deps,
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
            )
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf(
                    "next.config.mjs",
                    "tsconfig.json",
                    "rescript-modules.d.ts",
                    "src/app/page.tsx",
                    "src/app/loading.tsx",
                    "src/app/client/GreetForm.tsx",
                    "src/app/api/greet/route.ts",
                    "src/app/api/greet/GreetRoute.res",
                ),
            ),
        )
        val (validationKey, validationContent) =
            TemplateScaffold.validationVariant(
                ctx,
                RESOURCE_ROOT,
                targetPath = "src/app/api/greet/Validation.res",
                sourcePath = "src/app/api/greet/Validation.res",
            )
        files[validationKey] = validationContent
        files["src/NextServer.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/NextServer.res")
        files["src/App.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/App.res", projectVars)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf("src/GreetForm.res", "src/Fetch.res", "src/__tests__/App.test.mjs"),
            ),
        )
        files.putAll(
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra = listOf(".next/", "out/", ".env*.local"),
                ciHasBuild = true,
                ciHasTest = true,
            ),
        )
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
