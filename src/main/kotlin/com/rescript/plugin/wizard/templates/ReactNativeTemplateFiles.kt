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
                    devDependencies = linkedMapOf("vitest" to TemplateVersions.VITEST),
                    scripts =
                        linkedMapOf(
                            "start" to "expo start",
                            "android" to "expo start --android",
                            "ios" to "expo start --ios",
                            "test" to "vitest run",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "app.json" to
                "{\n  \"expo\": {\n    \"name\": \"${ctx.projectName}\",\n" +
                "    \"slug\": \"${ctx.projectName}\",\n    \"version\": \"1.0.0\"\n  }\n}",
            "App.tsx" to "import App from \"./src/App.gen\";\n\nexport default App;",
            "src/App.res" to appRes(ctx.projectName),
            "src/ReactNative.res" to reactNativeBindings(),
            "src/__tests__/App.test.mjs" to appTest(),
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
                            "Bindings" to bindingsSection(),
                            "Adding Screens" to addScreensSection(),
                            "Project Layout" to rnLayoutSection(),
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".expo/", "android/", "ios/", "*.tsbuildinfo")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun appTest(): String =
        buildString {
            // Source-level smoke test: importing App.res.mjs at runtime would try to load
            // react-native which is not available under Node. We instead verify that the
            // compiled module file exists, which catches build-time regressions.
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("import { existsSync } from \"node:fs\";")
            appendLine("")
            appendLine("describe(\"App module\", () => {")
            appendLine("  it(\"compiles to a .res.mjs file\", () => {")
            appendLine("    expect(existsSync(\"src/App.res.mjs\")).toBe(true);")
            appendLine("  });")
            appendLine("});")
        }

    private fun appRes(projectName: String): String {
        val dollar = '$'
        return buildString {
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
            appendLine(
                "        todos->Array.reduce(0, (max, t) => t.id > max ? t.id : max) + 1",
            )
            appendLine(
                "      setTodos(prev => prev->Array.concat([{id: nextId, text: draft}]))",
            )
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
    }

    private fun bindingsSection(): String =
        """
        `src/ReactNative.res` wraps the core components used by `App.res`: `View`, `Text`,
        `TextInput`, `Button`, and `FlatList`. To add more (e.g. `ScrollView`, `Image`,
        `Pressable`), follow the same `@module("react-native") @react.component` pattern.
        For third-party modules (e.g. `react-native-reanimated`), bind against the package
        name instead of `"react-native"`.
        """.trimIndent()

    private fun addScreensSection(): String =
        """
        The template ships as a single screen. For navigation, install `expo-router` or
        `@react-navigation/native` and add screens as additional ReScript components
        annotated with `@genType @react.component`. Keep shared types in a `Shared.res`
        module so the navigator type-checks against the screen's props.
        """.trimIndent()

    private fun rnLayoutSection(): String =
        buildString {
            appendLine("| File | Purpose |")
            appendLine("| --- | --- |")
            appendLine("| `App.tsx` | Expo entry point re-exporting ReScript App |")
            appendLine("| `src/App.res` | Root screen (todo list demo) |")
            appendLine("| `src/ReactNative.res` | Bindings for core RN components |")
            append("| `app.json` | Expo config |")
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
}
