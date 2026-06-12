package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

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
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-dom"] = TemplateVersions.REACT_DOM
        val (validationName, validationVersion) = TemplateScaffold.validationDependency(ctx)
        deps[validationName] = validationVersion
        val readme =
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
            )
        val files = linkedMapOf<String, String>()
        files["rescript.json"] =
            ProjectFileBuilders.rescriptJson(
                name = ctx.projectName,
                bsDependencies =
                    listOf("@rescript/core", "@rescript/react") + ctx.validationBsDeps(),
                includeJsx = true,
            )
        files["package.json"] =
            ProjectFileBuilders.packageJson(
                name = ctx.projectName,
                isPrivate = true,
                type = "module",
                packageManager = ctx.packageManagerSpec(),
                engines = mapOf("node" to ctx.nodeEngine),
                dependencies = deps,
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
            )
        files["index.html"] = TemplateResourceLoader.load("$RESOURCE_ROOT/index.html", projectVars)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf("vite.config.mjs", "src/App.res", "src/Main.res", "src/Api.res"),
            ),
        )
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files["src/__tests__/App.test.mjs"] =
            TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs")
        files.putAll(
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra = listOf("dist/", ".vite/"),
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
