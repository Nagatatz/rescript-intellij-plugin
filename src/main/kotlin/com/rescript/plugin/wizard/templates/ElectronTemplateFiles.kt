package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for an Electron desktop application powered by ReScript and Vite+.
 *
 * Pairs Electron's main process (CommonJS) with a Vite+ renderer that bundles the React UI written
 * in ReScript. The template ships a ready-to-run dev workflow plus README, gitignore, editorconfig,
 * and a CI workflow. The renderer validates IPC responses through `Validation.res` — either zod or
 * sury, selected via [TemplateContext.validationLibrary] — so malformed main-process payloads
 * surface as UI errors instead of silent type crashes.
 */
internal object ElectronTemplateFiles {
    private const val RESOURCE_ROOT = "electron"

    /**
     * Generates Electron template files using the supplied [TemplateContext].
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
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = electronDependencies(ctx.validationLibrary),
                    devDependencies =
                        linkedMapOf(
                            "electron" to TemplateVersions.ELECTRON,
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
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
                            "electron" to "electron .",
                            "start" to "vp build && electron .",
                            "test" to "vp test",
                            "test:coverage" to "vp test --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "main.cjs" to TemplateResourceLoader.load("$RESOURCE_ROOT/main.cjs", projectVars),
            "index.html" to TemplateResourceLoader.load("$RESOURCE_ROOT/index.html", projectVars),
            "vite.config.mjs" to TemplateResourceLoader.load("$RESOURCE_ROOT/vite.config.mjs"),
            "preload.cjs" to TemplateResourceLoader.load("$RESOURCE_ROOT/preload.cjs"),
            "src/App.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/App.res"),
            "src/Electron.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Electron.res"),
            "src/Main.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Main.res"),
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/__tests__/App.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "An Electron desktop app with a ReScript + React renderer bundled by Vite+. " +
                            "IPC payloads are validated in the renderer via ${ctx.validationLibrary.displayName} " +
                            "before they reach UI code.",
                    scripts =
                        listOf(
                            "dev" to "Start the Vite+ dev server for the renderer",
                            "build" to "Bundle the renderer for production",
                            "start" to "Bundle and launch the Electron app",
                            "test" to "Run Vitest via Vite+",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "About Vite+" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/about-vite-plus.md"),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "out/", ".vite/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun electronDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
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
