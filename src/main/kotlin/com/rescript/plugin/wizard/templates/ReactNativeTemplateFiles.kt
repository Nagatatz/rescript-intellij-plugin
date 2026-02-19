package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

internal object ReactNativeTemplateFiles {
    fun generate(projectName: String): Map<String, String> =
        mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                    includeGenType = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = projectName,
                    dependencies =
                        linkedMapOf(
                            "rescript" to "^12.0.0",
                            "@rescript/core" to "^1.0.0",
                            "@rescript/react" to "^0.14.0",
                            "react" to "^19.0.0",
                            "react-native" to "^0.76.0",
                            "expo" to "^52.0.0",
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
                "{\n  \"expo\": {\n    \"name\": \"$projectName\",\n    \"slug\": \"$projectName\",\n    \"version\": \"1.0.0\"\n  }\n}",
            "App.tsx" to "import App from \"./src/App.gen\";\n\nexport default App;",
            "src/App.res" to
                "@genType @react.component\nlet make = () => {\n  <ReactNative.View>\n    <ReactNative.Text>\n      {React.string(\"Hello, $projectName!\")}\n    </ReactNative.Text>\n  </ReactNative.View>\n}",
            "src/ReactNative.res" to
                "module View = {\n  @module(\"react-native\") @react.component\n  external make: (~children: React.element=?) => React.element = \"View\"\n}\n\nmodule Text = {\n  @module(\"react-native\") @react.component\n  external make: (~children: React.element=?) => React.element = \"Text\"\n}",
        )
}
