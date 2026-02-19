package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates files for the CLI Tool template.
 *
 * Produces a command-line tool project with a `bin` entry point
 * and `Process.argv` argument parsing.
 */
internal object CliToolTemplateFiles {
    fun generate(projectName: String): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = projectName,
                    dependencies = linkedMapOf("rescript" to "^12.0.0", "@rescript/core" to "^1.0.0"),
                    scripts =
                        linkedMapOf(
                            "build" to "rescript",
                            "start" to "node src/Cli.res.mjs",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    bin = "./src/Cli.res.mjs",
                    type = "module",
                ),
            "src/Cli.res" to cliRes(projectName),
        )

    private fun cliRes(projectName: String): String {
        val dollar = '$'
        return "let args = Process.argv->Array.sliceToEnd(~start=2)\n" +
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
}
