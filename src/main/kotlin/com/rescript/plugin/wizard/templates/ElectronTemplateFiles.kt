package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

internal object ElectronTemplateFiles {
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
                    devDependencies =
                        linkedMapOf(
                            "electron" to "^35.0.0",
                            "@vitejs/plugin-react" to "^5.0.0",
                            "vite" to "^7.0.0",
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "vite",
                            "build" to "vite build",
                            "electron" to "electron .",
                            "start" to "vite build && electron .",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "main.cjs" to mainCjs(projectName),
            "index.html" to indexHtml(projectName),
            "vite.config.mjs" to
                "import { defineConfig } from \"vite\";\nimport react from \"@vitejs/plugin-react\";\n\nexport default defineConfig({\n  plugins: [react()],\n  base: \"./\",\n});",
            "src/App.res" to ProjectFileBuilders.reactComponent(),
            "src/Main.res" to
                "switch ReactDOM.Client.createRoot(ReactDOM.querySelector(\"#root\")) {\n| Some(root) => ReactDOM.Client.Root.render(root, <App />)\n| None => Console.error(\"Could not find root element\")\n}",
        )

    private fun mainCjs(projectName: String): String =
        buildString {
            appendLine("const { app, BrowserWindow } = require(\"electron\");")
            appendLine("const path = require(\"path\");")
            appendLine("")
            appendLine("function createWindow() {")
            appendLine("  const win = new BrowserWindow({")
            appendLine("    width: 800,")
            appendLine("    height: 600,")
            appendLine("    title: \"$projectName\",")
            appendLine("  });")
            appendLine("")
            appendLine("  win.loadFile(path.join(__dirname, \"dist\", \"index.html\"));")
            appendLine("}")
            appendLine("")
            appendLine("app.whenReady().then(createWindow);")
            appendLine("")
            appendLine("app.on(\"window-all-closed\", () => {")
            appendLine("  if (process.platform !== \"darwin\") app.quit();")
            append("});")
        }

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
}
