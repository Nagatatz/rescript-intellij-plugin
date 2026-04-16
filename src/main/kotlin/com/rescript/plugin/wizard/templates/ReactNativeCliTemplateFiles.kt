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
 * example for custom native modules, and Android Studio setup guidance in the README.
 *
 * @see ReactNativeTemplateFiles for the Expo-managed counterpart
 */
internal object ReactNativeCliTemplateFiles {
    /**
     * Generates Community CLI template files using the supplied [TemplateContext].
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
                        ),
                    devDependencies =
                        linkedMapOf(
                            "@react-native-community/cli" to TemplateVersions.RN_COMMUNITY_CLI,
                            "@react-native/babel-preset" to TemplateVersions.RN_BABEL_PRESET,
                            "@react-native/metro-config" to TemplateVersions.RN_METRO_CONFIG,
                            "@types/react-native" to TemplateVersions.RN_TYPES,
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
            "app.json" to appJson(ctx.projectName),
            "index.js" to indexJs(),
            "App.tsx" to "import App from \"./src/App.gen\";\n\nexport default App;\n",
            "metro.config.js" to metroConfig(),
            "babel.config.js" to babelConfig(),
            "src/App.res" to appRes(ctx.projectName),
            "src/ReactNative.res" to reactNativeBindings(),
            "src/NativeGreeting.res" to nativeGreetingBindings(),
            "src/__tests__/App.test.mjs" to appTest(),
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
                            "Community CLI Setup" to communityCliSection(ctx),
                            "Running on Android" to runAndroidSection(ctx),
                            "Opening in Android Studio" to androidStudioSection(),
                            "Native Modules" to nativeModulesSection(),
                            "Troubleshooting" to troubleshootingSection(ctx),
                            "Fallback" to fallbackSection(),
                            "Project Layout" to layoutSection(),
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
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun appJson(projectName: String): String =
        buildString {
            appendLine("{")
            appendLine("  \"name\": \"$projectName\",")
            appendLine("  \"displayName\": \"$projectName\"")
            append("}")
        }

    private fun indexJs(): String =
        buildString {
            // React Native Community CLI uses AppRegistry.registerComponent as the entry point;
            // the component name must match `app.json`'s `"name"` field.
            appendLine("import { AppRegistry } from \"react-native\";")
            appendLine("import App from \"./App\";")
            appendLine("import { name as appName } from \"./app.json\";")
            appendLine("")
            append("AppRegistry.registerComponent(appName, () => App);")
            appendLine()
        }

    private fun metroConfig(): String =
        buildString {
            appendLine("const { getDefaultConfig, mergeConfig } = require(\"@react-native/metro-config\");")
            appendLine("")
            appendLine("const defaultConfig = getDefaultConfig(__dirname);")
            appendLine("")
            appendLine("// Extend the resolver so Metro picks up ReScript-compiled output (.res.mjs).")
            appendLine("const config = {")
            appendLine("  resolver: {")
            appendLine("    sourceExts: [...defaultConfig.resolver.sourceExts, \"mjs\"],")
            appendLine("  },")
            appendLine("};")
            appendLine("")
            append("module.exports = mergeConfig(defaultConfig, config);")
            appendLine()
        }

    private fun babelConfig(): String =
        buildString {
            appendLine("module.exports = {")
            appendLine("  presets: [\"module:@react-native/babel-preset\"],")
            append("};")
            appendLine()
        }

    private fun appTest(): String =
        buildString {
            // Source-level smoke test: importing App.res.mjs at runtime would pull in react-native
            // which is not available under Node. Verify the compiled module file exists instead
            // to catch build-time regressions.
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("import { existsSync } from \"node:fs\";")
            appendLine("")
            appendLine("describe(\"App module\", () => {")
            appendLine("  it(\"compiles to a .res.mjs file\", () => {")
            appendLine("    expect(existsSync(\"src/App.res.mjs\")).toBe(true);")
            appendLine("  });")
            appendLine("});")
        }

    private fun appRes(projectName: String): String =
        buildString {
            appendLine("// Interactive todo list: useState + FlatList + TextInput + Button.")
            appendLine("// Demonstrates the common pattern of managing a dynamic list on mobile.")
            appendLine("type todo = {id: int, text: string}")
            appendLine("")
            appendLine("@genType @react.component")
            appendLine("let make = () => {")
            appendLine("  let (todos, setTodos) = React.useState(() => [")
            appendLine("    {id: 1, text: \"Build $projectName\"},")
            appendLine("    {id: 2, text: \"Ship to the stores\"},")
            appendLine("  ])")
            appendLine("  let (draft, setDraft) = React.useState(() => \"\")")
            appendLine("")
            appendLine("  let addTodo = () => {")
            appendLine("    if draft != \"\" {")
            appendLine("      let nextId =")
            appendLine("        todos->Array.reduce(0, (max, t) => t.id > max ? t.id : max) + 1")
            appendLine("      setTodos(prev => prev->Array.concat([{id: nextId, text: draft}]))")
            appendLine("      setDraft(_ => \"\")")
            appendLine("    }")
            appendLine("  }")
            appendLine("")
            appendLine("  let renderItem = (item: todo) =>")
            appendLine("    <ReactNative.View")
            appendLine("      key={item.id->Int.toString}")
            appendLine("      style={ReactNative.Style.make(~padding=8, ())}>")
            appendLine("      <ReactNative.Text> {React.string(item.text)} </ReactNative.Text>")
            appendLine("    </ReactNative.View>")
            appendLine("")
            appendLine("  <ReactNative.View style={ReactNative.Style.make(~flex=1, ~padding=24, ())}>")
            appendLine("    <ReactNative.Text> {React.string(\"$projectName TODOs\")} </ReactNative.Text>")
            appendLine("    <ReactNative.TextInput")
            appendLine("      value={draft}")
            appendLine("      onChangeText={t => setDraft(_ => t)}")
            appendLine("      placeholder=\"New todo\"")
            appendLine("    />")
            appendLine("    <ReactNative.Button title=\"Add\" onPress={_ => addTodo()} />")
            appendLine("    <ReactNative.FlatList")
            appendLine("      data={todos}")
            appendLine("      keyExtractor={item => item.id->Int.toString}")
            appendLine("      renderItem={({item}) => renderItem(item)}")
            appendLine("    />")
            append("  </ReactNative.View>")
            appendLine()
            append("}")
        }

    private fun reactNativeBindings(): String =
        buildString {
            appendLine("// Minimal bindings over react-native's core components.")
            appendLine("module Style = {")
            appendLine("  type t")
            appendLine("  @obj external make: (")
            appendLine("    ~flex: int=?,")
            appendLine("    ~padding: int=?,")
            appendLine("    ~margin: int=?,")
            appendLine("    ~backgroundColor: string=?,")
            appendLine("    unit,")
            appendLine("  ) => t = \"\"")
            appendLine("}")
            appendLine("")
            appendLine("module View = {")
            appendLine("  @module(\"react-native\") @react.component")
            appendLine("  external make: (")
            appendLine("    ~style: Style.t=?,")
            appendLine("    ~children: React.element=?,")
            appendLine("  ) => React.element = \"View\"")
            appendLine("}")
            appendLine("")
            appendLine("module Text = {")
            appendLine("  @module(\"react-native\") @react.component")
            appendLine("  external make: (~children: React.element=?) => React.element = \"Text\"")
            appendLine("}")
            appendLine("")
            appendLine("module TextInput = {")
            appendLine("  @module(\"react-native\") @react.component")
            appendLine("  external make: (")
            appendLine("    ~value: string,")
            appendLine("    ~onChangeText: string => unit,")
            appendLine("    ~placeholder: string=?,")
            appendLine("  ) => React.element = \"TextInput\"")
            appendLine("}")
            appendLine("")
            appendLine("module Button = {")
            appendLine("  @module(\"react-native\") @react.component")
            appendLine("  external make: (")
            appendLine("    ~title: string,")
            appendLine("    ~onPress: ReactEvent.Synthetic.t => unit,")
            appendLine("  ) => React.element = \"Button\"")
            appendLine("}")
            appendLine("")
            appendLine("module FlatList = {")
            appendLine("  @module(\"react-native\") @react.component")
            appendLine("  external make: (")
            appendLine("    ~data: array<'a>,")
            appendLine("    ~keyExtractor: 'a => string,")
            appendLine("    ~renderItem: {\"item\": 'a} => React.element,")
            appendLine("  ) => React.element = \"FlatList\"")
            append("}")
        }

    private fun nativeGreetingBindings(): String =
        buildString {
            appendLine("// Bindings for a custom native module exposed from Android/iOS via")
            appendLine("// React Native's NativeModules bridge.")
            appendLine("//")
            appendLine("// Implement the matching Kotlin/Swift module following the official React")
            appendLine("// Native documentation, then call `NativeGreeting.greet(\"world\")` from your")
            appendLine("// ReScript code. This file contains *bindings only* — the native side must be")
            appendLine("// provided by you.")
            appendLine("//")
            appendLine("// Docs: https://reactnative.dev/docs/legacy/native-modules-android")
            appendLine("module NativeGreeting = {")
            appendLine("  @module(\"react-native\") @scope(\"NativeModules\")")
            appendLine("  external module_: {\"greet\": string => promise<string>} = \"NativeGreeting\"")
            appendLine("")
            appendLine("  let greet = (name: string): promise<string> => module_[\"greet\"](name)")
            append("}")
        }

    private fun communityCliSection(ctx: TemplateContext): String =
        """
        This template only ships the JavaScript/TypeScript + ReScript surface. The `android/`
        and `ios/` projects are generated on first run by the React Native Community CLI.

        ```bash
        ${ctx.installCmd()}
        ${ctx.execCmd("react-native")} init-android
        ${ctx.execCmd("react-native")} init-ios        # macOS only
        ```

        If `init-android` / `init-ios` are not available in your CLI version, use the latest
        upstream template instead:

        ```bash
        npx @react-native-community/cli@latest init tmp
        # then copy tmp/android (and tmp/ios) into this project
        ```

        Prerequisites for Android builds: JDK 21, Android Studio, Android SDK, NDK, watchman
        (macOS/Linux). For iOS builds: Xcode 16+ and CocoaPods on macOS.
        """.trimIndent()

    private fun runAndroidSection(ctx: TemplateContext): String =
        """
        Two terminals are required: one for Metro, one for the native build.

        ```bash
        # Terminal 1 — ReScript watcher
        ${ctx.runCmd("res:dev")}

        # Terminal 2 — Metro bundler
        ${ctx.runCmd("start")}

        # Terminal 3 — Native build + install on emulator/device
        ${ctx.runCmd("android")}
        ```

        The `metro.config.js` shipped with this template adds `mjs` to Metro's resolver so
        that `.res.mjs` artifacts from ReScript are picked up automatically.
        """.trimIndent()

    private fun androidStudioSection(): String =
        """
        Open the generated `android/` directory (not the project root) as a project in
        Android Studio. Gradle will sync on first open. Use `Run > Run 'app'` with an
        emulator or connected device — Metro must already be running (see above). Gradle
        JDK should be set to 21 under `File > Settings > Build, Execution, Deployment >
        Build Tools > Gradle`.
        """.trimIndent()

    private fun nativeModulesSection(): String =
        """
        `src/NativeGreeting.res` demonstrates how to call a Kotlin/Swift `NativeModule` from
        ReScript through `@module("react-native") @scope("NativeModules")`. The file contains
        *bindings only*; the native implementation must be added separately in `android/app/`
        or `ios/`. Follow the official React Native guides for the native side:

        - Android: https://reactnative.dev/docs/legacy/native-modules-android
        - iOS: https://reactnative.dev/docs/legacy/native-modules-ios
        """.trimIndent()

    private fun troubleshootingSection(ctx: TemplateContext): String =
        """
        - **Metro cannot reach the device:** run `adb reverse tcp:8081 tcp:8081` after
          connecting the device.
        - **Stale Metro cache:** restart Metro with `${ctx.runCmd("start")} --reset-cache`.
        - **Gradle errors after upgrading deps:** run `cd android && ./gradlew clean` and
          rebuild from Android Studio.
        - **Module not found for `.res.mjs`:** confirm `metro.config.js` still contains `mjs`
          in `resolver.sourceExts`.
        """.trimIndent()

    private fun fallbackSection(): String =
        """
        `@react-native-community/cli` and the surrounding tooling iterate quickly. If the
        commands above no longer match the current CLI, defer to the official documentation
        at https://reactnative.dev and adjust the scripts in `package.json` accordingly.
        The ReScript pieces (`rescript.json`, `src/*.res`, `metro.config.js`) remain valid
        across CLI revisions.
        """.trimIndent()

    private fun layoutSection(): String =
        buildString {
            appendLine("| File | Purpose |")
            appendLine("| --- | --- |")
            appendLine("| `index.js` | RN entry point registering the root component |")
            appendLine("| `App.tsx` | Re-exports the ReScript root component |")
            appendLine("| `src/App.res` | Root screen (todo list demo) |")
            appendLine("| `src/ReactNative.res` | Bindings for core RN components |")
            appendLine("| `src/NativeGreeting.res` | Example bindings for a custom native module |")
            appendLine("| `metro.config.js` | Metro bundler config with `.res.mjs` resolver |")
            appendLine("| `babel.config.js` | Babel preset for React Native |")
            append("| `app.json` | App registry name/displayName |")
        }
}
