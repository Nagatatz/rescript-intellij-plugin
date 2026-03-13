package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/** Generates project template files for a Vite + React ReScript project. */
internal object ViteReactTemplateFiles {
    fun generate(projectName: String): Map<String, String> =
        mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = projectName,
                    dependencies =
                        linkedMapOf(
                            "rescript" to "^12.0.0",
                            "@rescript/core" to "^1.0.0",
                            "@rescript/react" to "^0.14.0",
                            "react" to "^19.0.4",
                            "react-dom" to "^19.0.4",
                        ),
                    devDependencies = linkedMapOf("@vitejs/plugin-react" to "^6.0.0", "vite" to "^8.0.0"),
                    scripts =
                        linkedMapOf(
                            "dev" to "vite",
                            "build" to "vite build",
                            "preview" to "vite preview",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "index.html" to indexHtml(projectName),
            "vite.config.mjs" to viteConfig(),
            "src/App.res" to ProjectFileBuilders.reactComponent(),
            "src/Main.res" to mainRes(),
        )

    private fun indexHtml(projectName: String): String =
        buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"en\">")
            appendLine("  <head>")
            appendLine("    <meta charset=\"UTF-8\" />")
            appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
            appendLine("    <title>$projectName</title>")
            appendLine("  </head>")
            appendLine("  <body>")
            appendLine("    <div id=\"root\"></div>")
            appendLine("    <script type=\"module\" src=\"/src/Main.res.mjs\"></script>")
            appendLine("  </body>")
            append("</html>")
        }

    private fun viteConfig(): String =
        buildString {
            appendLine("import { defineConfig } from \"vite\";")
            appendLine("import react from \"@vitejs/plugin-react\";")
            appendLine("")
            appendLine("export default defineConfig({")
            appendLine("  plugins: [react()],")
            append("});")
        }

    private fun mainRes(): String =
        buildString {
            appendLine("switch ReactDOM.Client.createRoot(ReactDOM.querySelector(\"#root\")) {")
            appendLine("| Some(root) => ReactDOM.Client.Root.render(root, <App />)")
            appendLine("| None => Console.error(\"Could not find root element\")")
            append("}")
        }
}
