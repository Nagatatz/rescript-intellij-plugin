package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager

/**
 * Shared file generators for README, .gitignore, .editorconfig, and CI workflow files.
 *
 * These files are identical in spirit across every template and only vary in small
 * details (project description, script names, extra ignore patterns). Centralizing
 * them here keeps individual template files focused on project-specific content.
 */
object CommonFiles {
    /**
     * Generates a `.gitignore` file with common ReScript, Node.js, and OS ignores.
     *
     * @param extra additional patterns to append (e.g. `dist/`, `.next/`, `.wrangler/`)
     */
    fun gitignore(extra: List<String> = emptyList()): String =
        buildString {
            appendLine("# Dependencies")
            appendLine("node_modules/")
            appendLine()
            appendLine("# ReScript build artifacts")
            appendLine("lib/")
            appendLine("*.res.js")
            appendLine("*.res.mjs")
            appendLine()
            appendLine("# Logs")
            appendLine("*.log")
            appendLine("npm-debug.log*")
            appendLine("pnpm-debug.log*")
            appendLine("yarn-debug.log*")
            appendLine("yarn-error.log*")
            appendLine()
            appendLine("# OS")
            appendLine(".DS_Store")
            appendLine("Thumbs.db")
            appendLine()
            appendLine("# Editor")
            appendLine(".idea/")
            appendLine(".vscode/")
            appendLine("*.swp")
            if (extra.isNotEmpty()) {
                appendLine()
                appendLine("# Project-specific")
                extra.forEach { appendLine(it) }
            }
        }

    /**
     * Generates a standard `.editorconfig` for ReScript projects (2-space indent, LF).
     */
    fun editorconfig(): String =
        """
        root = true

        [*]
        indent_style = space
        indent_size = 2
        end_of_line = lf
        charset = utf-8
        trim_trailing_whitespace = true
        insert_final_newline = true

        [*.md]
        trim_trailing_whitespace = false
        """.trimIndent() + "\n"

    /**
     * Generates a README.md for a template project.
     *
     * @param ctx template context (provides project name and PM-specific commands)
     * @param description short description shown under the title
     * @param scripts list of (script name, human-readable description) pairs to document
     * @param extraSections optional extra Markdown sections appended after the scripts table
     */
    fun readme(
        ctx: TemplateContext,
        description: String,
        scripts: List<Pair<String, String>>,
        extraSections: List<Pair<String, String>> = emptyList(),
    ): String =
        buildString {
            appendLine("# ${ctx.projectName}")
            appendLine()
            appendLine(description)
            appendLine()
            appendLine("## Prerequisites")
            appendLine()
            appendLine("- Node.js ${TemplateVersions.NODE_ENGINE.removePrefix(">=")}+")
            appendLine("- ${packageManagerName(ctx.packageManager)} (managed via Corepack)")
            appendLine()
            appendLine("## Getting Started")
            appendLine()
            appendLine("```bash")
            appendLine(ctx.installCmd())
            if (scripts.any { it.first == "dev" }) {
                appendLine(ctx.runCmd("dev"))
            } else if (scripts.any { it.first == "res:dev" }) {
                appendLine(ctx.runCmd("res:dev"))
            }
            appendLine("```")
            appendLine()
            appendLine("## Scripts")
            appendLine()
            appendLine("| Command | Description |")
            appendLine("| --- | --- |")
            scripts.forEach { (name, desc) ->
                appendLine("| `${ctx.runCmd(name)}` | $desc |")
            }
            appendLine()
            extraSections.forEach { (heading, body) ->
                appendLine("## $heading")
                appendLine()
                appendLine(body.trimEnd())
                appendLine()
            }
            appendLine("## Learn More")
            appendLine()
            appendLine("- [ReScript documentation](https://rescript-lang.org/)")
            appendLine("- [ReScript IntelliJ plugin](https://plugins.jetbrains.com/plugin/com.rescript.plugin)")
        }

    /**
     * Generates a minimal GitHub Actions workflow that installs dependencies and
     * runs the ReScript compiler plus an optional project build/test script.
     *
     * @param ctx template context (used for package-manager-specific install step)
     * @param hasBuild whether the project exposes a top-level `build` script to run
     * @param hasTest whether the project exposes a top-level `test` script to run
     */
    fun ciWorkflow(
        ctx: TemplateContext,
        hasBuild: Boolean = false,
        hasTest: Boolean = false,
    ): String =
        buildString {
            appendLine("name: CI")
            appendLine()
            appendLine("on:")
            appendLine("  push:")
            appendLine("    branches: [main]")
            appendLine("  pull_request:")
            appendLine("    branches: [main]")
            appendLine()
            appendLine("jobs:")
            appendLine("  build:")
            appendLine("    runs-on: ubuntu-latest")
            appendLine("    steps:")
            appendLine("      - uses: actions/checkout@v4")
            appendLine("      - uses: actions/setup-node@v4")
            appendLine("        with:")
            appendLine("          node-version: 20")
            if (ctx.packageManager == PackageManager.PNPM) {
                appendLine("      - uses: pnpm/action-setup@v4")
                appendLine("        with:")
                appendLine("          version: ${TemplateVersions.PNPM.substringBefore('.')}")
            }
            appendLine("      - name: Install dependencies")
            appendLine("        run: ${ctx.installCmd()}")
            appendLine("      - name: ReScript build")
            appendLine("        run: ${ctx.execCmd("rescript")}")
            if (hasBuild) {
                appendLine("      - name: Build")
                appendLine("        run: ${ctx.runCmd("build")}")
            }
            if (hasTest) {
                appendLine("      - name: Test")
                appendLine("        run: ${ctx.runCmd("test")}")
            }
        }

    private fun packageManagerName(pm: PackageManager): String =
        when (pm) {
            PackageManager.NPM -> "npm"
            PackageManager.PNPM -> "pnpm"
            PackageManager.YARN -> "Yarn"
        }
}
