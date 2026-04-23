package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a React single-page application powered by ReScript and Vite+.
 *
 * Uses Vite+ (`vite-plus`) as the build/test toolchain. Vite+ is a unified wrapper over Vite,
 * Vitest, Oxlint, Oxfmt, and Rolldown. Because Vite+ is pre-1.0, the README warns users about
 * the early-access status and shows how to fall back to plain Vite. Form input is validated via
 * `Validation.res` before the network call; the backing library (zod or sury) is selected through
 * [TemplateContext.validationLibrary] in the Wizard.
 */
internal object ViteReactTemplateFiles {
    private const val RESOURCE_ROOT = "vite-react"

    /**
     * Generates Vite + React template files using the supplied [TemplateContext].
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
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies = viteReactDependencies(ctx.validationLibrary),
                    devDependencies =
                        linkedMapOf(
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                            // Direct vite dep for the documented Vite+ → Vite fallback path.
                            "vite" to TemplateVersions.VITE,
                            "vite-plus" to TemplateVersions.VITE_PLUS,
                            "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "vp dev",
                            "build" to "vp build",
                            "preview" to "vp preview",
                            "test" to "vp test",
                            "test:coverage" to "vp test --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "index.html" to TemplateResourceLoader.load("$RESOURCE_ROOT/index.html", projectVars),
            "vite.config.mjs" to TemplateResourceLoader.load("$RESOURCE_ROOT/vite.config.mjs"),
            "src/App.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/App.res"),
            "src/Main.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Main.res"),
            "src/Api.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Api.res"),
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/__tests__/App.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A React single-page app built with ReScript and the Vite+ toolchain. " +
                            "The greet form validates input through ${ctx.validationLibrary.displayName}.",
                    scripts =
                        listOf(
                            "dev" to "Start the Vite+ dev server",
                            "build" to "Produce a production bundle",
                            "preview" to "Preview the production build locally",
                            "test" to "Run Vitest via Vite+",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "About Vite+" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/about-vite-plus.md"),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", ".vite/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun viteReactDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
