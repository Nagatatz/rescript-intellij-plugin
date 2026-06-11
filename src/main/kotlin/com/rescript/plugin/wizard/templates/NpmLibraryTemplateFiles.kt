package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates files for the npm Library template.
 *
 * Produces a publishable npm package with ReScript and `@genType` for TypeScript consumers.
 * Ships a small API surface (sync greeting, async fetchWithTimeout, list helpers) that
 * exercises the common patterns library authors hit on day two: pure utilities, async I/O,
 * and generic helpers. Includes README, .gitignore, .editorconfig, CI workflow, and Vitest.
 *
 * The public `greetChecked` function validates an untyped JSON payload through a
 * `Validation.res` whose implementation is selected via [TemplateContext.validationLibrary]
 * (zod or sury), so libraries can defend themselves from malformed input from JS/TS callers.
 */
internal object NpmLibraryTemplateFiles {
    private const val RESOURCE_ROOT = "npm-library"

    /**
     * Generates npm Library template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val publishVars =
            mapOf(
                "cmdBuild" to ctx.runCmd("build"),
                "cmdTest" to ctx.runCmd("test"),
            )
        val readme =
            CommonFiles.readme(
                ctx = ctx,
                description =
                    "A publishable npm package written in ReScript with TypeScript bindings via genType. " +
                        "Ships a small utility API (sync greet, async fetch-with-timeout, list helpers) " +
                        "plus a ${ctx.validationLibrary.displayName}-backed input guard so untyped JS " +
                        "callers get readable errors instead of stack traces.",
                scripts =
                    listOf(
                        "build" to "Compile ReScript sources",
                        "test" to "Run Vitest",
                        "res:dev" to "Watch ReScript sources",
                    ),
                extraSections =
                    listOf(
                        "API Surface" to
                            TemplateResourceLoader.load(
                                "$RESOURCE_ROOT/readme/api-surface.md",
                                mapOf("validationLibrary" to ctx.validationLibrary.displayName),
                            ),
                        "Publish" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/publish.md", publishVars),
                    ),
            )
        val files = linkedMapOf<String, String>()
        files["rescript.json"] =
            ProjectFileBuilders.rescriptJson(
                name = ctx.projectName,
                bsDependencies = listOf("@rescript/core") + ctx.validationBsDeps(),
                includeGenType = true,
            )
        files["package.json"] =
            ProjectFileBuilders.packageJson(
                name = ctx.projectName,
                type = "module",
                packageManager = ctx.packageManagerSpec(),
                engines = mapOf("node" to ctx.nodeEngine),
                // Entry points surface the compiled ESM + genType .d.ts so consumers
                // can `import { greet } from "<pkg>"` with real types. `files`
                // restricts the published tarball to runtime / type artifacts.
                main = "./src/Index.res.mjs",
                types = "./src/Index.gen.d.ts",
                exports =
                    linkedMapOf(
                        "." to
                            linkedMapOf(
                                "types" to "./src/Index.gen.d.ts",
                                "import" to "./src/Index.res.mjs",
                            ),
                    ),
                files =
                    listOf(
                        "src/**/*.res.mjs",
                        "src/**/*.res",
                        "src/**/*.gen.d.ts",
                        "src/**/*.gen.tsx",
                    ),
                dependencies = TemplateScaffold.standardDependencies(ctx),
                devDependencies =
                    linkedMapOf(
                        "vitest" to TemplateVersions.VITEST,
                        "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        "typescript" to TemplateVersions.TYPESCRIPT,
                    ),
                scripts =
                    linkedMapOf(
                        "build" to "rescript",
                        "clean" to "rescript clean",
                        "test" to "vitest run",
                        "test:coverage" to "vitest run --coverage",
                        "prepare" to "rescript",
                        "res:build" to "rescript",
                        "res:clean" to "rescript clean",
                        "res:dev" to "rescript -w",
                    ),
            )
        files["tsconfig.json"] = TemplateResourceLoader.load("$RESOURCE_ROOT/tsconfig.json")
        files["src/Index.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/Index.res", projectVars)
        files.putAll(TemplateScaffold.resourceFiles(RESOURCE_ROOT, listOf("src/ListUtils.res", "src/Fetcher.res")))
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf(
                    "src/__tests__/Index.test.mjs",
                    "src/__tests__/ListUtils.test.mjs",
                    "src/__tests__/Fetcher.test.mjs",
                ),
            ),
        )
        files.putAll(TemplateScaffold.commonTail(ctx, readme = readme, ciHasBuild = true, ciHasTest = true))
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
