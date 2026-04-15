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
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "vp dev",
                            "build" to "vp build",
                            "electron" to "electron .",
                            "start" to "vp build && electron .",
                            "test" to "vp test",
                            "test:coverage" to "vp test --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "main.cjs" to mainCjs(ctx.projectName),
            "index.html" to indexHtml(ctx.projectName),
            "vite.config.mjs" to viteConfig(),
            "preload.cjs" to preloadCjs(),
            "src/App.res" to appRes(),
            "src/Electron.res" to electronRes(),
            "src/Main.res" to mainRes(),
            "src/__tests__/App.test.mjs" to appTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "An Electron desktop app with a ReScript + React renderer bundled by Vite+.",
                    scripts =
                        listOf(
                            "dev" to "Start the Vite+ dev server for the renderer",
                            "build" to "Bundle the renderer for production",
                            "start" to "Bundle and launch the Electron app",
                            "test" to "Run Vitest via Vite+",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "About Vite+" to vitePlusNote(),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "out/", ".vite/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun mainCjs(projectName: String): String =
        buildString {
            appendLine("const { app, BrowserWindow, ipcMain } = require(\"electron\");")
            appendLine("const path = require(\"path\");")
            appendLine("const os = require(\"os\");")
            appendLine("")
            appendLine("function createWindow() {")
            appendLine("  const win = new BrowserWindow({")
            appendLine("    width: 800,")
            appendLine("    height: 600,")
            appendLine("    title: \"$projectName\",")
            appendLine("    webPreferences: {")
            appendLine("      preload: path.join(__dirname, \"preload.cjs\"),")
            appendLine("      contextIsolation: true,")
            appendLine("      nodeIntegration: false,")
            appendLine("    },")
            appendLine("  });")
            appendLine("")
            appendLine("  win.loadFile(path.join(__dirname, \"dist\", \"index.html\"));")
            appendLine("}")
            appendLine("")
            appendLine("// IPC handlers live in the main process and are invoked from the renderer")
            appendLine("// via the `electronAPI` object exposed in preload.cjs.")
            appendLine("ipcMain.handle(\"app:getInfo\", () => ({")
            appendLine("  name: \"$projectName\",")
            appendLine("  electronVersion: process.versions.electron,")
            appendLine("  platform: os.platform(),")
            appendLine("  arch: os.arch(),")
            appendLine("}));")
            appendLine("")
            appendLine("app.whenReady().then(createWindow);")
            appendLine("")
            appendLine("app.on(\"window-all-closed\", () => {")
            appendLine("  if (process.platform !== \"darwin\") app.quit();")
            append("});")
        }

    private fun preloadCjs(): String =
        buildString {
            appendLine("// Exposes a typed, minimal API to the renderer via contextBridge so the")
            appendLine("// renderer does not need Node integration enabled.")
            appendLine("const { contextBridge, ipcRenderer } = require(\"electron\");")
            appendLine("")
            appendLine("contextBridge.exposeInMainWorld(\"electronAPI\", {")
            appendLine("  getInfo: () => ipcRenderer.invoke(\"app:getInfo\"),")
            append("});")
        }

    private fun electronRes(): String =
        buildString {
            appendLine("// Bindings over the `window.electronAPI` object exposed by preload.cjs.")
            appendLine("type info = {name: string, electronVersion: string, platform: string, arch: string}")
            appendLine("")
            appendLine("@val external electronAPI: {\"getInfo\": unit => promise<info>} = \"electronAPI\"")
            appendLine("")
            appendLine("let getInfo = (): promise<info> => electronAPI[\"getInfo\"]()")
        }

    private fun appTest(): String =
        buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("")
            appendLine("describe(\"App module\", () => {")
            appendLine("  it(\"loads without throwing\", async () => {")
            appendLine("    await expect(import(\"../App.res.mjs\")).resolves.toBeDefined();")
            appendLine("  });")
            appendLine("});")
        }

    private fun appRes(): String =
        buildString {
            appendLine("// Interactive demo: click the button to invoke the main process via IPC")
            appendLine("// and render the returned info.")
            appendLine("@react.component")
            appendLine("let make = () => {")
            appendLine("  let (info, setInfo) = React.useState(() => None)")
            appendLine("  let (loading, setLoading) = React.useState(() => false)")
            appendLine("")
            appendLine("  let handleClick = async _ => {")
            appendLine("    setLoading(_ => true)")
            appendLine("    let result = await Electron.getInfo()")
            appendLine("    setInfo(_ => Some(result))")
            appendLine("    setLoading(_ => false)")
            appendLine("  }")
            appendLine("")
            appendLine("  <main style={ReactDOM.Style.make(~padding=\"2rem\", ~fontFamily=\"sans-serif\", ())}>")
            appendLine("    <h1> {React.string(\"ReScript + Electron\")} </h1>")
            appendLine("    <button onClick={handleClick} disabled={loading}>")
            appendLine("      {React.string(loading ? \"Loading...\" : \"Get system info\")}")
            appendLine("    </button>")
            appendLine("    {switch info {")
            appendLine("    | Some(i) =>")
            appendLine("      <dl>")
            appendLine("        <dt> {React.string(\"Name\")} </dt> <dd> {React.string(i.name)} </dd>")
            appendLine("        <dt> {React.string(\"Electron\")} </dt> <dd> {React.string(i.electronVersion)} </dd>")
            appendLine("        <dt> {React.string(\"Platform\")} </dt> <dd> {React.string(i.platform)} </dd>")
            appendLine("        <dt> {React.string(\"Arch\")} </dt> <dd> {React.string(i.arch)} </dd>")
            appendLine("      </dl>")
            appendLine("    | None => React.null")
            appendLine("    }}")
            append("  </main>")
            appendLine()
            append("}")
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
            appendLine("switch ReactDOM.querySelector(\"#root\") {")
            appendLine("| Some(rootEl) =>")
            appendLine("  ReactDOM.Client.Root.render(ReactDOM.Client.createRoot(rootEl), <App />)")
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
