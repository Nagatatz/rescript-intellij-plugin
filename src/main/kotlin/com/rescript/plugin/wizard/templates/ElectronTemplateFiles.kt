package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

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
                        "Renderer ↔ Main IPC" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/ipc.md"),
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
            )
        files["main.cjs"] = TemplateResourceLoader.load("$RESOURCE_ROOT/main.cjs", projectVars)
        files["index.html"] = TemplateResourceLoader.load("$RESOURCE_ROOT/index.html", projectVars)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf("vite.config.mjs", "preload.cjs", "src/App.res", "src/Electron.res", "src/Main.res"),
            ),
        )
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files["src/__tests__/App.test.mjs"] =
            TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs")
        files.putAll(
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra = listOf("dist/", "out/", ".vite/"),
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
