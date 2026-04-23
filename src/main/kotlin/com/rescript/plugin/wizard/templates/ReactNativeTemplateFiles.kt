package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for an Expo-based React Native app written in ReScript.
 *
 * Pairs Expo CLI with ReScript via genType so the entry point can import compiled `.gen.tsx`
 * artifacts. The draft todo input is validated through a `Validation.res` whose backing
 * library (zod or sury) is selected via [TemplateContext.validationLibrary], so blank or
 * oversized entries never reach the list state.
 */
internal object ReactNativeTemplateFiles {
    private const val RESOURCE_ROOT = "react-native"

    /**
     * Generates React Native template files using the supplied [TemplateContext].
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
                    dependencies = reactNativeDependencies(ctx.validationLibrary),
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
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/__tests__/App.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "An Expo-based React Native app with ReScript components exposed via genType. " +
                            "Draft todo input is validated by ${ctx.validationLibrary.displayName}.",
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
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
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

    private fun reactNativeDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-native"] = TemplateVersions.REACT_NATIVE
        deps["expo"] = TemplateVersions.EXPO
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
