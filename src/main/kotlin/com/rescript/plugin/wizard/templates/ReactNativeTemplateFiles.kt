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
    /**
     * Generates React Native template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
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
                    scripts =
                        linkedMapOf(
                            "start" to "expo start",
                            "android" to "expo start --android",
                            "ios" to "expo start --ios",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "app.json" to
                "{\n  \"expo\": {\n    \"name\": \"${ctx.projectName}\",\n" +
                "    \"slug\": \"${ctx.projectName}\",\n    \"version\": \"1.0.0\"\n  }\n}",
            "App.tsx" to "import App from \"./src/App.gen\";\n\nexport default App;",
            "src/App.res" to
                "@genType @react.component\nlet make = () => {\n  <ReactNative.View>\n" +
                "    <ReactNative.Text>\n" +
                "      {React.string(\"Hello, ${ctx.projectName}!\")}\n" +
                "    </ReactNative.Text>\n  </ReactNative.View>\n}",
            "src/ReactNative.res" to
                "module View = {\n  @module(\"react-native\") @react.component\n" +
                "  external make: (~children: React.element=?) => React.element = \"View\"\n}\n\n" +
                "module Text = {\n  @module(\"react-native\") @react.component\n" +
                "  external make: (~children: React.element=?) => React.element = \"Text\"\n}",
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "An Expo-based React Native app with ReScript components exposed via genType.",
                    scripts =
                        listOf(
                            "start" to "Start the Expo dev server",
                            "android" to "Build and launch on an Android emulator/device",
                            "ios" to "Build and launch on an iOS simulator/device",
                            "res:dev" to "Watch ReScript sources",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".expo/", "android/", "ios/", "*.tsbuildinfo")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
