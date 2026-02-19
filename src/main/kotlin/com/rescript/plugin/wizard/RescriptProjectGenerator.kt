package com.rescript.plugin.wizard

/**
 * Supported package managers for ReScript project initialization.
 */
enum class PackageManager(
    val command: String,
) {
    NPM("npm"),
    PNPM("pnpm"),
    YARN("yarn"),
}

/**
 * Generates starter project files for a new ReScript project.
 *
 * Produces `rescript.json` (with optional JSX config), `package.json` (with
 * rescript and optional React dependencies), and a starter `.res` module
 * (either a plain module or a React component).
 */
object RescriptProjectGenerator {
    /**
     * Generates the content of `rescript.json` for a new project.
     *
     * @param name the project name
     * @param includeReact whether to include JSX config and React dependencies
     * @return the JSON content as a string
     */
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
            appendLine("  \"sources\": {")
            appendLine("    \"dir\": \"src\",")
            appendLine("    \"subdirs\": true")
            appendLine("  },")
            appendLine("  \"package-specs\": {")
            appendLine("    \"module\": \"esmodule\",")
            appendLine("    \"in-source\": true")
            appendLine("  },")
            appendLine("  \"suffix\": \".res.mjs\",")
            appendLine("  \"bs-dependencies\": [$bsDeps],")
            if (jsxSection.isNotEmpty()) {
                appendLine(jsxSection)
            }
            appendLine("  \"bsc-flags\": [\"-open RescriptCore\"]")
            append("}")
        }
    }

    /**
     * Generates the content of `package.json` for a new project.
     *
     * @param name the project name
     * @param includeReact whether to include React and related dependencies
     * @return the JSON content as a string
     */
    fun generatePackageJson(
        name: String,
        includeReact: Boolean,
    ): String {
        val deps =
            buildString {
                append("    \"rescript\": \"^12.0.0\"")
                if (includeReact) {
                    appendLine(",")
                    appendLine("    \"react\": \"^19.0.0\",")
                    appendLine("    \"react-dom\": \"^19.0.0\",")
                    append("    \"@rescript/react\": \"^0.14.0\"")
                }
            }

        return buildString {
            appendLine("{")
            appendLine("  \"name\": \"$name\",")
            appendLine("  \"version\": \"0.1.0\",")
            appendLine("  \"scripts\": {")
            appendLine("    \"res:build\": \"rescript\",")
            appendLine("    \"res:clean\": \"rescript clean\",")
            appendLine("    \"res:dev\": \"rescript -w\"")
            appendLine("  },")
            appendLine("  \"dependencies\": {")
            appendLine(deps)
            appendLine("  }")
            append("}")
        }
    }

    fun generateStarterModule(): String = "Console.log(\"Hello, ReScript!\")"

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
