package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a minimal ReScript project.
 *
 * The Basic template doubles as a "Hello World + one practical step" starter: in addition to
 * `Console.log`, it demonstrates reading CLI arguments, reading/writing files via `fs/promises`,
 * and validates a bundled `config.sample.json` through `Validation.res`, whose implementation
 * (zod or sury) is chosen via [TemplateContext.validationLibrary] in the Wizard.
 */
internal object BasicTemplateFiles {
    private const val RESOURCE_ROOT = "basic"

    /**
     * Generates Basic template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val runVars =
            mapOf(
                "cmdStart" to ctx.runCmd("start"),
                "validationLibrary" to ctx.validationLibrary.displayName,
            )
        val layoutVars = mapOf("validationLibrary" to ctx.validationLibrary.displayName)
        val readme =
            CommonFiles.readme(
                ctx = ctx,
                description =
                    "A starter ReScript project showcasing CLI argument parsing, file I/O via " +
                        "Node's fs/promises API, and ${ctx.validationLibrary.displayName}-backed " +
                        "validation of a bundled `config.sample.json`.",
                scripts =
                    listOf(
                        "start" to "Build and run the entry module",
                        "test" to "Run Vitest",
                        "res:dev" to "Compile ReScript sources in watch mode",
                        "res:build" to "Compile ReScript sources once",
                        "res:clean" to "Remove generated build artifacts",
                    ),
                extraSections =
                    listOf(
                        "Run the App" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/run-the-app.md", runVars),
                        "Project Layout" to
                            TemplateResourceLoader.load(
                                "$RESOURCE_ROOT/readme/project-layout.md",
                                layoutVars,
                            ),
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
                isPrivate = true,
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
                        "start" to "node src/App.res.mjs",
                        "test" to "vitest run",
                        "test:coverage" to "vitest run --coverage",
                        "res:build" to "rescript",
                        "res:clean" to "rescript clean",
                        "res:dev" to "rescript -w",
                    ),
            )
        files.putAll(TemplateScaffold.resourceFiles(RESOURCE_ROOT, listOf("src/Args.res", "src/Files.res")))
        files["src/App.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/App.res", projectVars)
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf("config.sample.json", "src/__tests__/App.test.mjs"),
            ),
        )
        files.putAll(TemplateScaffold.commonTail(ctx, readme = readme, ciHasTest = true))
        return files
    }

    /**
     * Back-compatible entry point that defaults to pnpm when only a project name is provided.
     */
    fun generate(projectName: String): Map<String, String> =
        generate(TemplateContext(projectName, com.rescript.plugin.wizard.PackageManager.PNPM))
}
