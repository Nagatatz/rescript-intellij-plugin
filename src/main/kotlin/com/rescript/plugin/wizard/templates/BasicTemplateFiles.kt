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
    private const val RESOURCE_ROOT = "basic"

    /**
     * Generates Basic template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val projectVars = mapOf("projectName" to ctx.projectName)
        val runVars = mapOf("cmdStart" to ctx.runCmd("start"))
        return mapOf(
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
                ),
            "src/Args.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Args.res"),
            "src/Files.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Files.res"),
            "src/App.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/App.res", projectVars),
            "src/__tests__/App.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs"),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A starter ReScript project showcasing CLI argument parsing and " +
                            "file I/O via Node's fs/promises API.",
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
                                TemplateResourceLoader.load("$RESOURCE_ROOT/readme/project-layout.md"),
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point that defaults to pnpm when only a project name is provided.
     */
    fun generate(projectName: String): Map<String, String> =
        generate(TemplateContext(projectName, com.rescript.plugin.wizard.PackageManager.PNPM))
}
