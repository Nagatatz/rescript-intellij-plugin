package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

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
        val variantKey = ctx.validationLibrary.variantKey()
        return mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core") + ctx.validationBsDeps(),
                    includeGenType = true,
                ),
            "package.json" to
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
                    dependencies = npmLibraryDependencies(ctx.validationLibrary),
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
                ),
            "tsconfig.json" to TemplateResourceLoader.load("$RESOURCE_ROOT/tsconfig.json"),
            "src/Index.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Index.res", projectVars),
            "src/ListUtils.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/ListUtils.res"),
            "src/Fetcher.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Fetcher.res"),
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/__tests__/Index.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Index.test.mjs"),
            "src/__tests__/ListUtils.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/ListUtils.test.mjs"),
            "src/__tests__/Fetcher.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Fetcher.test.mjs"),
            "README.md" to
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
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun npmLibraryDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
