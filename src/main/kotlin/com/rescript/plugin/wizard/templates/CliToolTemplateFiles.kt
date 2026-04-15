package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates files for the CLI Tool template.
 *
 * Produces a command-line tool project with a `bin` entry point and `Process.argv`
 * argument parsing. Ships README, .gitignore, .editorconfig, CI workflow, and a
 * Vitest sample.
 */
internal object CliToolTemplateFiles {
    /**
     * Generates CLI Tool template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    type = "module",
                    bin = "./src/Cli.res.mjs",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                        ),
                    devDependencies = linkedMapOf("vitest" to TemplateVersions.VITEST),
                    scripts =
                        linkedMapOf(
                            "build" to "rescript",
                            "start" to "node src/Cli.res.mjs",
                            "test" to "vitest run",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Cli.res" to cliRes(ctx.projectName),
            "src/__tests__/Cli.test.mjs" to cliTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A small command-line tool built with ReScript and Node.js.",
                    scripts =
                        listOf(
                            "build" to "Compile ReScript sources",
                            "start" to "Run the CLI once",
                            "test" to "Run Vitest",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Install Locally" to
                                "After running `${ctx.runCmd("build")}`, link the CLI globally:\n\n" +
                                "```bash\nnpm link\n${ctx.projectName} --help\n```",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("coverage/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun cliRes(projectName: String): String {
        val dollar = '$'
        return "@val external argv: array<string> = \"process.argv\"\n" +
            "\n" +
            "let args = argv->Array.sliceToEnd(~start=2)\n" +
            "\n" +
            "switch args->Array.get(0) {\n" +
            "| Some(\"--help\") | Some(\"-h\") =>\n" +
            "  Console.log(\"Usage: $projectName [options]\")\n" +
            "  Console.log(\"  --help, -h  Show this help message\")\n" +
            "| Some(arg) =>\n" +
            "  Console.log(`Hello, $dollar{arg}!`)\n" +
            "| None =>\n" +
            "  Console.log(\"Hello from $projectName!\")\n" +
            "}"
    }

    private fun cliTest(): String =
        buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("")
            appendLine("describe(\"cli module\", () => {")
            appendLine("  it(\"loads without throwing\", async () => {")
            appendLine("    await expect(import(\"../Cli.res.mjs\")).resolves.toBeDefined();")
            appendLine("  });")
            appendLine("});")
        }
}
