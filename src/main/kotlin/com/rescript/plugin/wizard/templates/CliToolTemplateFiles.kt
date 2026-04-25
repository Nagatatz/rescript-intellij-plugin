package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

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
        val variantKey = ctx.validationLibrary.variantKey()
        return mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    type = "module",
                    // Object form so projects can expose multiple executables later
                    // (e.g. add a sibling entry and a new bin/<name>.mjs wrapper).
                    bin = linkedMapOf(ctx.projectName to "./bin/cli.mjs"),
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = cliToolDependencies(ctx.validationLibrary),
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
                ),
            "bin/cli.mjs" to TemplateResourceLoader.load("$RESOURCE_ROOT/bin/cli.mjs", projectVars),
            "src/Args.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Args.res"),
            "src/Cli.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Cli.res", projectVars),
            "src/Commands.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Commands.res", projectVars),
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/__tests__/Args.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Args.test.mjs"),
            "README.md" to
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
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun cliToolDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
