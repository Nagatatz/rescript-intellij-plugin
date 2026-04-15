package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for an Electron desktop application powered by ReScript and Vite+.
 *
 * Pairs Electron's main process (CommonJS) with a Vite+ renderer that bundles the React UI written
 * in ReScript. The template ships a ready-to-run dev workflow plus README, gitignore, editorconfig,
 * and a CI workflow.
 */
internal object ElectronTemplateFiles {
    /**
     * Generates Electron template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                ),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "@rescript/react" to TemplateVersions.RESCRIPT_REACT,
                            "react" to TemplateVersions.REACT,
                            "react-dom" to TemplateVersions.REACT_DOM,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "electron" to TemplateVersions.ELECTRON,
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                            "vite" to TemplateVersions.VITE_PLUS,
                            "vite-plus" to TemplateVersions.VITE_PLUS,
                            "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "vp dev",
                            "build" to "vp build",
                            "electron" to "electron .",
                            "start" to "vp build && electron .",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "main.cjs" to mainCjs(ctx.projectName),
            "index.html" to indexHtml(ctx.projectName),
            "vite.config.mjs" to viteConfig(),
            "src/App.res" to ProjectFileBuilders.reactComponent(),
            "src/Main.res" to mainRes(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "An Electron desktop app with a ReScript + React renderer bundled by Vite+.",
                    scripts =
                        listOf(
                            "dev" to "Start the Vite+ dev server for the renderer",
                            "build" to "Bundle the renderer for production",
                            "start" to "Bundle and launch the Electron app",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "About Vite+" to vitePlusNote(),
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "out/", ".vite/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

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

    private fun viteConfig(): String =
        buildString {
            appendLine("import { defineConfig } from \"vite-plus\";")
            appendLine("import react from \"@vitejs/plugin-react\";")
            appendLine("")
            appendLine("export default defineConfig({")
            appendLine("  plugins: [react()],")
            appendLine("  base: \"./\",")
            append("});")
        }

    private fun mainRes(): String =
        buildString {
            appendLine("switch ReactDOM.Client.createRoot(ReactDOM.querySelector(\"#root\")) {")
            appendLine("| Some(root) => ReactDOM.Client.Root.render(root, <App />)")
            appendLine("| None => Console.error(\"Could not find root element\")")
            append("}")
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

    private fun vitePlusNote(): String =
        """
        This template uses [Vite+](https://vite.plus) (`vite-plus`) for the renderer build.
        Vite+ is **pre-1.0** — replace `vite-plus` with `vite` and adjust `vite.config.mjs`
        if you prefer classic Vite.
        """.trimIndent()
}
