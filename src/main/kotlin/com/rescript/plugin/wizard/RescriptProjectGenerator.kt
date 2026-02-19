package com.rescript.plugin.wizard

enum class PackageManager(
    val command: String,
) {
    NPM("npm"),
    PNPM("pnpm"),
    YARN("yarn"),
}

object RescriptProjectGenerator {
    fun generateRescriptJson(
        name: String,
        includeReact: Boolean,
    ): String {
        val bsDeps =
            buildString {
                append("\"@rescript/core\"")
                if (includeReact) {
                    append(", \"@rescript/react\"")
                }
            }

        val jsxSection =
            if (includeReact) {
                """
                |  "jsx": {
                |    "version": 4
                |  },
                """.trimMargin()
            } else {
                ""
            }

        return buildString {
            appendLine("{")
            appendLine("  \"name\": \"$name\",")
            appendLine("  \"sources\": [")
            appendLine("    {")
            appendLine("      \"dir\": \"src\",")
            appendLine("      \"subdirs\": true")
            appendLine("    }")
            appendLine("  ],")
            appendLine("  \"package-type\": \"module\",")
            appendLine("  \"suffix\": \".res.mjs\",")
            appendLine("  \"bs-dependencies\": [$bsDeps],")
            if (jsxSection.isNotEmpty()) {
                appendLine(jsxSection)
            }
            appendLine("  \"bsc-flags\": [\"-open RescriptCore\"]")
            append("}")
        }
    }

    fun generatePackageJson(
        name: String,
        includeReact: Boolean,
    ): String {
        val deps =
            buildString {
                appendLine("    \"rescript\": \"^11.0.0\",")
                append("    \"@rescript/core\": \"^1.0.0\"")
                if (includeReact) {
                    appendLine(",")
                    appendLine("    \"react\": \"^18.0.0\",")
                    appendLine("    \"react-dom\": \"^18.0.0\",")
                    append("    \"@rescript/react\": \"^0.13.0\"")
                }
            }

        return buildString {
            appendLine("{")
            appendLine("  \"name\": \"$name\",")
            appendLine("  \"version\": \"0.1.0\",")
            appendLine("  \"scripts\": {")
            appendLine("    \"build\": \"rescript build\",")
            appendLine("    \"clean\": \"rescript clean\",")
            appendLine("    \"dev\": \"rescript build -w\"")
            appendLine("  },")
            appendLine("  \"dependencies\": {")
            appendLine(deps)
            appendLine("  }")
            append("}")
        }
    }

    fun generateStarterModule(): String =
        buildString {
            appendLine("let greeting = \"Hello, ReScript!\"")
            appendLine("")
            append("Console.log(greeting)")
        }

    fun generateReactComponent(): String =
        buildString {
            appendLine("@react.component")
            appendLine("let make = () => {")
            appendLine("  <div>")
            appendLine("    {React.string(\"Hello, ReScript + React!\")}")
            appendLine("  </div>")
            append("}")
        }
}
