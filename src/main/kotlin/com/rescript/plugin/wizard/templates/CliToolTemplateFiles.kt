package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates files for the CLI Tool template.
 *
 * Produces a command-line tool with a subcommand dispatcher (`greet`, `init`) plus a small
 * flag-parsing helper (`Args.res`). The shape mirrors common CLIs (git, docker, rescript):
 * one top-level binary that dispatches to per-command modules under `src/Commands/`.
 *
 * The `init` subcommand validates its `--name` / `--dir` options through a
 * `Validation.res` whose implementation is selected by [TemplateContext.validationLibrary]
 * (zod or sury), so changing the Wizard choice swaps the library in the generated project.
 */
internal object CliToolTemplateFiles {
    private const val RESOURCE_ROOT = "cli-tool"

    /**
     * Generates CLI Tool template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val readmeVars =
            mapOf(
                "cmdStart" to ctx.runCmd("start"),
                "validationLibrary" to ctx.validationLibrary.displayName,
            )
        val installVars =
            mapOf(
                "cmdBuild" to ctx.runCmd("build"),
                "projectName" to ctx.projectName,
            )
        val readme =
            CommonFiles.readme(
                ctx = ctx,
                description =
                    "A ReScript CLI with a subcommand dispatcher, flag parsing helpers, " +
                        "runtime option validation via ${ctx.validationLibrary.displayName}, " +
                        "and Vitest coverage — ready to extend with new commands.",
                scripts =
                    listOf(
                        "build" to "Compile ReScript sources",
                        "start" to "Run the CLI once",
                        "test" to "Run Vitest",
                        "res:dev" to "Watch ReScript sources",
                    ),
                extraSections =
                    listOf(
                        "Usage" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/usage.md", readmeVars),
                        "Project Layout" to
                            TemplateResourceLoader.load(
                                "$RESOURCE_ROOT/readme/project-layout.md",
                                mapOf("validationLibrary" to ctx.validationLibrary.displayName),
                            ),
                        "Install Locally" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/install-locally.md", installVars),
                    ),
            )
        val files = linkedMapOf<String, String>()
        files["rescript.json"] =
            ProjectFileBuilders.rescriptJson(
                name = ctx.projectName,
                bsDependencies = listOf("@rescript/core") + ctx.validationBsDeps(),
            )
        files["package.json"] =
            ProjectFileBuilders.packageJson(
                name = ctx.projectName,
                type = "module",
                // Object form so projects can expose multiple executables later
                // (e.g. add a sibling entry and a new bin/<name>.mjs wrapper).
                bin = linkedMapOf(ctx.projectName to "./bin/cli.mjs"),
                packageManager = ctx.packageManagerSpec(),
                engines = mapOf("node" to ctx.nodeEngine),
                dependencies = TemplateScaffold.standardDependencies(ctx),
                devDependencies =
                    linkedMapOf(
                        "vitest" to TemplateVersions.VITEST,
                        "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                    ),
                scripts =
                    linkedMapOf(
                        "build" to "rescript",
                        "start" to "node bin/cli.mjs",
                        "test" to "vitest run",
                        "test:coverage" to "vitest run --coverage",
                        "res:build" to "rescript",
                        "res:clean" to "rescript clean",
                        "res:dev" to "rescript -w",
                    ),
            )
        files["bin/cli.mjs"] = TemplateResourceLoader.load("$RESOURCE_ROOT/bin/cli.mjs", projectVars)
        files["src/Args.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/Args.res")
        files["src/Cli.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/Cli.res", projectVars)
        files["src/Commands.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/Commands.res", projectVars)
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files["src/__tests__/Args.test.mjs"] =
            TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Args.test.mjs")
        files.putAll(TemplateScaffold.commonTail(ctx, readme = readme, ciHasBuild = true, ciHasTest = true))
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
