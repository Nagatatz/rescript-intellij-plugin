package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

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
    private const val RESOURCE_ROOT = "react-native-cli"

    /**
     * Generates Community CLI template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val nameVar = mapOf("projectName" to ctx.projectName)
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["@rescript/react"] = TemplateVersions.RESCRIPT_REACT
        deps["react"] = TemplateVersions.REACT
        deps["react-native"] = TemplateVersions.REACT_NATIVE
        val (validationName, validationVersion) = TemplateScaffold.validationDependency(ctx)
        deps[validationName] = validationVersion
        val readme =
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
                                "$RESOURCE_ROOT/readme/community-cli.md",
                                mapOf(
                                    "installCmd" to ctx.installCmd(),
                                    "execReactNative" to ctx.execCmd("react-native"),
                                ),
                            ),
                        "Running on Android" to
                            TemplateResourceLoader.load(
                                "$RESOURCE_ROOT/readme/running-on-android.md",
                                mapOf(
                                    "cmdResDev" to ctx.runCmd("res:dev"),
                                    "cmdStart" to ctx.runCmd("start"),
                                    "cmdAndroid" to ctx.runCmd("android"),
                                ),
                            ),
                        "Opening in Android Studio" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/android-studio.md"),
                        "Native Modules" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/native-modules.md"),
                        "Troubleshooting" to
                            TemplateResourceLoader.load(
                                "$RESOURCE_ROOT/readme/troubleshooting.md",
                                mapOf("cmdStart" to ctx.runCmd("start")),
                            ),
                        "Fallback" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/fallback.md"),
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
                devDependencies =
                    linkedMapOf(
                        "@react-native-community/cli" to TemplateVersions.RN_COMMUNITY_CLI,
                        "@react-native/babel-preset" to TemplateVersions.RN_BABEL_PRESET,
                        "@react-native/metro-config" to TemplateVersions.RN_METRO_CONFIG,
                        "@react-native/typescript-config" to TemplateVersions.RN_TYPESCRIPT_CONFIG,
                        "vitest" to TemplateVersions.VITEST,
                        "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        "typescript" to TemplateVersions.TYPESCRIPT,
                        "@types/react" to TemplateVersions.REACT_TYPES,
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
            )
        files["app.json"] = TemplateResourceLoader.load("$RESOURCE_ROOT/app.json", nameVar)
        files.putAll(TemplateScaffold.resourceFiles(RESOURCE_ROOT, listOf("tsconfig.json", "index.js")))
        files["App.tsx"] = "import App from \"./src/App.gen\";\n\nexport default App;\n"
        files.putAll(
            TemplateScaffold.resourceFiles(RESOURCE_ROOT, listOf("metro.config.js", "babel.config.js")),
        )
        files["src/App.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/App.res", nameVar)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf("src/ReactNative.res", "src/NativeGreeting.res"),
            ),
        )
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files["src/__tests__/App.test.mjs"] =
            TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs")
        files.putAll(
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra =
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
                        "*.apk",
                        "*.aab",
                        "*.ipa",
                    ),
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
