package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for a React Native app using the Community CLI (bare workflow).
 *
 * Targets developers who need direct access to the native `android/` and `ios/` projects
 * (typically Android Studio / Xcode users). The template ships the JS/TS + ReScript surface
 * only; the native projects themselves are produced by running `npx @react-native-community/cli`
 * after the template is generated. Includes a Metro config that resolves `.res.mjs`, a bindings
 * example for custom native modules, Android Studio setup guidance in the README, and a
 * `Validation.res` (zod or sury, selected via [TemplateContext.validationLibrary]) that guards
 * the draft-todo input.
 *
 * Static file content lives under `src/main/resources/templates/react-native-cli/` and is
 * loaded via [TemplateResourceLoader]; `{{projectName}}` / PM-specific command placeholders
 * are resolved at load time.
 *
 * @see ReactNativeTemplateFiles for the Expo-managed counterpart
 */
internal object ReactNativeCliTemplateFiles {
    /**
     * Generates Community CLI template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val nameVar = mapOf("projectName" to ctx.projectName)
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
                    dependencies = reactNativeCliDependencies(ctx.validationLibrary),
                    devDependencies =
                        linkedMapOf(
                            "@react-native-community/cli" to TemplateVersions.RN_COMMUNITY_CLI,
                            "@react-native/babel-preset" to TemplateVersions.RN_BABEL_PRESET,
                            "@react-native/metro-config" to TemplateVersions.RN_METRO_CONFIG,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "react-native start",
                            "android" to "react-native run-android",
                            "ios" to "react-native run-ios",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "app.json" to TemplateResourceLoader.load("react-native-cli/app.json", nameVar),
            "index.js" to TemplateResourceLoader.load("react-native-cli/index.js"),
            "App.tsx" to "import App from \"./src/App.gen\";\n\nexport default App;\n",
            "metro.config.js" to TemplateResourceLoader.load("react-native-cli/metro.config.js"),
            "babel.config.js" to TemplateResourceLoader.load("react-native-cli/babel.config.js"),
            "src/App.res" to TemplateResourceLoader.load("react-native-cli/src/App.res", nameVar),
            "src/ReactNative.res" to TemplateResourceLoader.load("react-native-cli/src/ReactNative.res"),
            "src/NativeGreeting.res" to TemplateResourceLoader.load("react-native-cli/src/NativeGreeting.res"),
            "src/Validation.res" to
                TemplateResourceLoader.load("react-native-cli/variants/$variantKey/src/Validation.res"),
            "src/__tests__/App.test.mjs" to
                TemplateResourceLoader.load("react-native-cli/src/__tests__/App.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A React Native app scaffolded with the Community CLI (bare workflow), " +
                            "with ReScript components exposed via genType. Designed for direct native " +
                            "project access in Android Studio / Xcode.",
                    scripts =
                        listOf(
                            "start" to "Start the Metro bundler",
                            "android" to "Build and launch on an Android emulator/device",
                            "ios" to "Build and launch on an iOS simulator/device",
                            "test" to "Run Vitest (source smoke test)",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Community CLI Setup" to
                                TemplateResourceLoader.load(
                                    "react-native-cli/readme/community-cli.md",
                                    mapOf(
                                        "installCmd" to ctx.installCmd(),
                                        "execReactNative" to ctx.execCmd("react-native"),
                                    ),
                                ),
                            "Running on Android" to
                                TemplateResourceLoader.load(
                                    "react-native-cli/readme/running-on-android.md",
                                    mapOf(
                                        "cmdResDev" to ctx.runCmd("res:dev"),
                                        "cmdStart" to ctx.runCmd("start"),
                                        "cmdAndroid" to ctx.runCmd("android"),
                                    ),
                                ),
                            "Opening in Android Studio" to
                                TemplateResourceLoader.load("react-native-cli/readme/android-studio.md"),
                            "Native Modules" to
                                TemplateResourceLoader.load("react-native-cli/readme/native-modules.md"),
                            "Troubleshooting" to
                                TemplateResourceLoader.load(
                                    "react-native-cli/readme/troubleshooting.md",
                                    mapOf("cmdStart" to ctx.runCmd("start")),
                                ),
                            "Fallback" to TemplateResourceLoader.load("react-native-cli/readme/fallback.md"),
                            "Project Layout" to
                                TemplateResourceLoader.load("react-native-cli/readme/project-layout.md"),
                        ),
                ),
            ".gitignore" to
                CommonFiles.gitignore(
                    extra =
                        listOf(
                            "android/build/",
                            "android/app/build/",
                            "android/.gradle/",
                            "android/local.properties",
                            "ios/Pods/",
                            "ios/build/",
                            "ios/.xcode.env.local",
                            "*.hbc",
                            "*.keystore",
                            "*.tsbuildinfo",
                        ),
                ),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun reactNativeCliDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-native"] = TemplateVersions.REACT_NATIVE
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
