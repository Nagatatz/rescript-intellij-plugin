package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates files for the CLI Tool template.
 *
 * Produces a command-line tool with a subcommand dispatcher (`greet`, `init`) plus a small
 * flag-parsing helper (`Args.res`). The shape mirrors common CLIs (git, docker, rescript):
 * one top-level binary that dispatches to per-command modules under `src/Commands/`.
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
            "src/Args.res" to argsRes(),
            "src/Cli.res" to cliRes(ctx.projectName),
            "src/Commands/Greet.res" to greetCommand(),
            "src/Commands/Init.res" to initCommand(ctx.projectName),
            "src/__tests__/Args.test.mjs" to argsTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A ReScript CLI with a subcommand dispatcher, flag parsing helpers, and " +
                            "Vitest coverage — ready to extend with new commands.",
                    scripts =
                        listOf(
                            "build" to "Compile ReScript sources",
                            "start" to "Run the CLI once",
                            "test" to "Run Vitest",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Usage" to usageSection(ctx),
                            "Project Layout" to projectLayoutSection(),
                            "Install Locally" to installLocallySection(ctx),
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

    private fun usageSection(ctx: TemplateContext): String =
        buildString {
            appendLine("```bash")
            appendLine("${ctx.runCmd("start")} -- greet Alice")
            appendLine("${ctx.runCmd("start")} -- greet Alice --shout")
            appendLine("${ctx.runCmd("start")} -- init my-project")
            appendLine("${ctx.runCmd("start")} -- --help")
            append("```")
        }

    private fun projectLayoutSection(): String =
        buildString {
            appendLine("| File | Purpose |")
            appendLine("| --- | --- |")
            appendLine("| `src/Cli.res` | Entry point + subcommand dispatcher |")
            appendLine("| `src/Args.res` | Positional / named flag helpers |")
            appendLine("| `src/Commands/Greet.res` | Sample subcommand with a boolean flag |")
            append("| `src/Commands/Init.res` | Sample subcommand with a positional argument |")
        }

    private fun installLocallySection(ctx: TemplateContext): String =
        buildString {
            appendLine("After `${ctx.runCmd("build")}`, link globally:")
            appendLine()
            appendLine("```bash")
            appendLine("npm link")
            appendLine("${ctx.projectName} greet Alice")
            append("```")
        }

    private fun argsRes(): String =
        buildString {
            appendLine("// Flag-parsing helpers backed by `process.argv`. Tested via Vitest.")
            appendLine("@val external argv: array<string> = \"process.argv\"")
            appendLine("")
            appendLine("/** Returns arguments excluding the node binary and script path. */")
            appendLine("let positional = () => argv->Array.sliceToEnd(~start=2)")
            appendLine("")
            appendLine("/** Splits the first positional argument (subcommand) from the remaining args. */")
            appendLine("let splitSubcommand = (args: array<string>): (option<string>, array<string>) =>")
            appendLine("  switch args {")
            appendLine("  | [] => (None, [])")
            appendLine("  | _ => (args->Array.get(0), args->Array.sliceToEnd(~start=1))")
            appendLine("  }")
            appendLine("")
            appendLine("/** Returns `true` if [flag] appears anywhere in [args]. */")
            appendLine("@genType")
            appendLine("let hasFlag = (args: array<string>, flag: string): bool =>")
            appendLine("  args->Array.some(a => a === flag)")
            appendLine("")
            appendLine("/** Reads the value following [flag] (returns None if [flag] is absent). */")
            appendLine("@genType")
            appendLine("let namedValue = (args: array<string>, flag: string): option<string> =>")
            appendLine("  switch args->Array.indexOf(flag) {")
            appendLine("  | -1 => None")
            appendLine("  | index => args->Array.get(index + 1)")
            append("  }")
        }

    private fun cliRes(projectName: String): String {
        val dollar = '$'
        return buildString {
            appendLine("// Entry point: reads argv, dispatches to a subcommand, or prints usage.")
            appendLine("let rec run = () => {")
            appendLine("  let allArgs = Args.positional()")
            appendLine("  let (subcommand, remaining) = Args.splitSubcommand(allArgs)")
            appendLine("")
            appendLine("  switch subcommand {")
            appendLine("  | Some(\"greet\") => Commands.Greet.run(remaining)")
            appendLine("  | Some(\"init\") => Commands.Init.run(remaining)")
            appendLine("  | Some(\"--help\") | Some(\"-h\") | None => printUsage()")
            appendLine("  | Some(cmd) =>")
            appendLine("    Console.error(`Unknown subcommand: $dollar{cmd}`)")
            appendLine("    printUsage()")
            appendLine("  }")
            appendLine("}")
            appendLine("")
            appendLine("and printUsage = () => {")
            appendLine("  Console.log(\"Usage: $projectName <command> [options]\")")
            appendLine("  Console.log(\"\")")
            appendLine("  Console.log(\"Commands:\")")
            appendLine("  Console.log(\"  greet <name> [--shout]   Say hello to someone\")")
            appendLine("  Console.log(\"  init <project-name>      Scaffold a new project\")")
            appendLine("  Console.log(\"  --help, -h               Show this help message\")")
            appendLine("}")
            appendLine("")
            append("run()")
        }
    }

    private fun greetCommand(): String {
        val dollar = '$'
        return buildString {
            appendLine("// `greet <name> [--shout]` — demonstrates positional + boolean flag.")
            appendLine("let run = (args: array<string>) => {")
            appendLine("  switch args->Array.get(0) {")
            appendLine("  | None =>")
            appendLine("    Console.error(\"greet: missing required argument <name>\")")
            appendLine("  | Some(name) =>")
            appendLine("    let shout = args->Args.hasFlag(\"--shout\")")
            appendLine("    let greeting = `Hello, $dollar{name}!`")
            appendLine("    Console.log(shout ? greeting->String.toUpperCase : greeting)")
            appendLine("  }")
            append("}")
        }
    }

    private fun initCommand(projectName: String): String {
        val dollar = '$'
        return buildString {
            appendLine("// `init <project-name>` — demonstrates a subcommand that would normally")
            appendLine("// scaffold files. In this template it just logs what it would do.")
            appendLine("let run = (args: array<string>) => {")
            appendLine("  switch args->Array.get(0) {")
            appendLine("  | None =>")
            appendLine("    Console.error(\"init: missing required argument <project-name>\")")
            appendLine("  | Some(target) =>")
            appendLine("    Console.log(`Initializing new \\\"$projectName\\\"-style project at $dollar{target}...`)")
            appendLine("    Console.log(\"(In a real CLI, this would create files on disk.)\")")
            appendLine("  }")
            append("}")
        }
    }

    private fun argsTest(): String =
        buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("import { hasFlag, namedValue } from \"../Args.res.mjs\";")
            appendLine("")
            appendLine("describe(\"hasFlag\", () => {")
            appendLine("  it(\"returns true when the flag is present\", () => {")
            appendLine("    expect(hasFlag([\"--shout\", \"Alice\"], \"--shout\")).toBe(true);")
            appendLine("  });")
            appendLine("  it(\"returns false when the flag is absent\", () => {")
            appendLine("    expect(hasFlag([\"Alice\"], \"--shout\")).toBe(false);")
            appendLine("  });")
            appendLine("});")
            appendLine("")
            appendLine("describe(\"namedValue\", () => {")
            appendLine("  it(\"returns the value following the flag\", () => {")
            appendLine("    expect(namedValue([\"--out\", \"dist\"], \"--out\")).toEqual(\"dist\");")
            appendLine("  });")
            appendLine("  it(\"returns undefined when the flag is missing\", () => {")
            appendLine("    expect(namedValue([\"Alice\"], \"--out\")).toBeUndefined();")
            appendLine("  });")
            appendLine("});")
        }
}
