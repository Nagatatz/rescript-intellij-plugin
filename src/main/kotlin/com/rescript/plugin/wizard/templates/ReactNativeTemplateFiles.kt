package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for an Expo-based React Native app written in ReScript.
 *
 * Pairs Expo CLI with ReScript via genType so the entry point can import compiled `.gen.tsx`
 * artifacts. Ships README, .gitignore, .editorconfig, and a CI workflow.
 */
internal object ReactNativeTemplateFiles {
    private const val RESOURCE_ROOT = "react-native"

    /**
     * Generates React Native template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
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
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "@rescript/react" to TemplateVersions.RESCRIPT_REACT,
                            "react" to TemplateVersions.REACT,
                            "react-native" to TemplateVersions.REACT_NATIVE,
                            "expo" to TemplateVersions.EXPO,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "expo start",
                            "android" to "expo start --android",
                            "ios" to "expo start --ios",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "app.json" to TemplateResourceLoader.load("$RESOURCE_ROOT/app.json", projectVars),
            "App.tsx" to TemplateResourceLoader.load("$RESOURCE_ROOT/App.tsx"),
            "src/App.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/App.res", projectVars),
            "src/ReactNative.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/ReactNative.res"),
            "src/__tests__/App.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "An Expo-based React Native app with ReScript components exposed via genType.",
                    scripts =
                        listOf(
                            "start" to "Start the Expo dev server",
                            "android" to "Build and launch on an Android emulator/device",
                            "ios" to "Build and launch on an iOS simulator/device",
                            "test" to "Run Vitest (source smoke test)",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Bindings" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/bindings.md"),
                            "Adding Screens" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/adding-screens.md"),
                            "Project Layout" to
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/project-layout.md"),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".expo/", "android/", "ios/", "*.tsbuildinfo")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
