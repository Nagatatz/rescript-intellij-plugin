package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a minimal ReScript project.
 *
 * The Basic template doubles as a "Hello World + one practical step" starter: in addition to
 * `Console.log`, it demonstrates reading CLI arguments, reading/writing files via `fs/promises`,
 * and a small app entry point that ties them together.
 */
internal object BasicTemplateFiles {
    /**
     * Generates Basic template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    type = "module",
                    isPrivate = true,
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/App.res.mjs",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Args.res" to argsRes(),
            "src/Files.res" to filesRes(),
            "src/App.res" to appRes(ctx.projectName),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A starter ReScript project showcasing CLI argument parsing and " +
                            "file I/O via Node's fs/promises API.",
                    scripts =
                        listOf(
                            "start" to "Build and run the entry module",
                            "res:dev" to "Compile ReScript sources in watch mode",
                            "res:build" to "Compile ReScript sources once",
                            "res:clean" to "Remove generated build artifacts",
                        ),
                    extraSections =
                        listOf(
                            "Run the App" to runTheAppSection(ctx),
                            "Project Layout" to projectLayoutSection(),
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx),
        )

    /**
     * Back-compatible entry point that defaults to pnpm when only a project name is provided.
     */
    fun generate(projectName: String): Map<String, String> =
        generate(TemplateContext(projectName, com.rescript.plugin.wizard.PackageManager.PNPM))

    private fun runTheAppSection(ctx: TemplateContext): String =
        buildString {
            appendLine("```bash")
            appendLine("# Print hello with the default name")
            appendLine(ctx.runCmd("start"))
            appendLine()
            appendLine("# Pass a name (arguments after -- are forwarded to Node)")
            appendLine("${ctx.runCmd("start")} -- Alice")
            appendLine()
            appendLine("# Tell the app to read a file")
            appendLine("${ctx.runCmd("start")} -- Alice --file hello.txt")
            append("```")
        }

    private fun projectLayoutSection(): String =
        buildString {
            appendLine("| File | Purpose |")
            appendLine("| --- | --- |")
            appendLine("| `src/App.res` | Entry point: parses arguments, runs the greeting |")
            appendLine("| `src/Args.res` | Reads `process.argv` and extracts a named flag |")
            append("| `src/Files.res` | Thin bindings over `node:fs/promises` for read/write |")
        }

    private fun argsRes(): String =
        buildString {
            appendLine("// Minimal CLI argument helpers backed by `process.argv`.")
            appendLine("@val external argv: array<string> = \"process.argv\"")
            appendLine("")
            appendLine("/** Returns arguments excluding the node binary and script path. */")
            appendLine("let positional = () => argv->Array.sliceToEnd(~start=2)")
            appendLine("")
            appendLine("/** Extracts the value that follows `--flag` (returns None if absent). */")
            appendLine("let named = (flag: string): option<string> => {")
            appendLine("  let args = positional()")
            appendLine("  switch args->Array.indexOf(flag) {")
            appendLine("  | -1 => None")
            appendLine("  | index => args->Array.get(index + 1)")
            appendLine("  }")
            append("}")
        }

    private fun filesRes(): String =
        buildString {
            appendLine("// Thin bindings over Node's fs/promises API, just enough for the starter app.")
            appendLine("type encoding = Utf8")
            appendLine("")
            appendLine("@module(\"node:fs/promises\")")
            appendLine("external readFileUtf8Raw: (string, @as(\"utf8\") _) => promise<string> = \"readFile\"")
            appendLine("")
            appendLine("@module(\"node:fs/promises\")")
            appendLine("external writeFileUtf8Raw: (string, string, @as(\"utf8\") _) => promise<unit> = \"writeFile\"")
            appendLine("")
            appendLine("/** Reads [path] as UTF-8 text. Rejects if the file does not exist. */")
            appendLine("let read = (path: string): promise<string> => readFileUtf8Raw(path, ())")
            appendLine("")
            appendLine("/** Writes [contents] to [path] as UTF-8. Creates or overwrites the file. */")
            appendLine("let write = (path: string, contents: string): promise<unit> =>")
            append("  writeFileUtf8Raw(path, contents, ())")
        }

    private fun appRes(projectName: String): String {
        val dollar = '$'
        return buildString {
            appendLine("// Entry point. Builds a greeting from CLI arguments and optionally")
            appendLine("// reads a text file to append its first line.")
            appendLine("let name = Args.positional()->Array.get(0)->Option.getOr(\"world\")")
            appendLine("")
            appendLine("let run = async () => {")
            appendLine("  let greeting = `Hello, $dollar{name}! — welcome to $projectName.`")
            appendLine("  Console.log(greeting)")
            appendLine("")
            appendLine("  switch Args.named(\"--file\") {")
            appendLine("  | Some(path) =>")
            appendLine("    try {")
            appendLine("      let contents = await Files.read(path)")
            appendLine("      Console.log(`First line of $dollar{path}:`)")
            appendLine("      Console.log(contents->String.split(\"\\n\")->Array.get(0)->Option.getOr(\"\"))")
            appendLine("    } catch {")
            appendLine("    | Exn.Error(err) =>")
            appendLine(
                "      Console.error(`Could not read $dollar{path}: $dollar{err->Exn.message" +
                    "->Option.getOr(\"unknown error\")}`)",
            )
            appendLine("    }")
            appendLine("  | None => ()")
            appendLine("  }")
            appendLine("}")
            appendLine("")
            append("run()->ignore")
        }
    }
}
