package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a React single-page application powered by ReScript and Vite+.
 *
 * Uses Vite+ (`vite-plus`) as the build/test toolchain. Vite+ is a unified wrapper over Vite,
 * Vitest, Oxlint, Oxfmt, and Rolldown. Because Vite+ is pre-1.0, the README warns users about
 * the early-access status and shows how to fall back to plain Vite.
 */
internal object ViteReactTemplateFiles {
    /**
     * Generates Vite + React template files using the supplied [TemplateContext].
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
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                            "vite" to TemplateVersions.VITE_PLUS, // Vite+ ships its own pinned vite via overrides
                            "vite-plus" to TemplateVersions.VITE_PLUS,
                            "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
                            "vitest" to TemplateVersions.VITEST,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "vp dev",
                            "build" to "vp build",
                            "preview" to "vp preview",
                            "test" to "vp test",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "index.html" to indexHtml(ctx.projectName),
            "vite.config.mjs" to viteConfig(),
            "src/App.res" to ProjectFileBuilders.reactComponent(),
            "src/Main.res" to mainRes(),
            "src/__tests__/App.test.mjs" to appTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A React single-page app built with ReScript and the Vite+ toolchain.",
                    scripts =
                        listOf(
                            "dev" to "Start the Vite+ dev server",
                            "build" to "Produce a production bundle",
                            "preview" to "Preview the production build locally",
                            "test" to "Run Vitest via Vite+",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "About Vite+" to vitePlusNote(),
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", ".vite/", "coverage/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

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
            appendLine("import { defineConfig } from \"vite-plus\";")
            appendLine("import react from \"@vitejs/plugin-react\";")
            appendLine("")
            appendLine("export default defineConfig({")
            appendLine("  plugins: [react()],")
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

    private fun appTest(): String =
        buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("import { make as App } from \"../App.res.mjs\";")
            appendLine("")
            appendLine("describe(\"App\", () => {")
            appendLine("  it(\"is a function component\", () => {")
            appendLine("    expect(typeof App).toBe(\"function\");")
            appendLine("  });")
            appendLine("});")
        }

    private fun vitePlusNote(): String =
        """
        This template uses [Vite+](https://vite.plus) (`vite-plus`), an early-access toolchain
        bundling Vite, Vitest, Oxlint, Oxfmt, and Rolldown. Vite+ is **pre-1.0** — APIs may
        change before stable release.

        > **Known issue:** the current pre-1.0 Vite+ does not resolve `vite/internal` cleanly
        > when paired with `@vitejs/plugin-react`, so `vp build` may fail. As a fallback,
        > replace `vite-plus` with `vite` in `vite.config.mjs` and switch the npm scripts to
        > `vite` / `vite build`. The migration path back to Vite+ is straightforward once the
        > stable release lands.
        """.trimIndent()
}
