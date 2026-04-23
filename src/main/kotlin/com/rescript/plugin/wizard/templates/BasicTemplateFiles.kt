package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

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
        val variantKey = ctx.validationLibrary.variantKey()
        return mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    type = "module",
                    isPrivate = true,
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = basicDependencies(ctx.validationLibrary),
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
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "config.sample.json" to TemplateResourceLoader.load("$RESOURCE_ROOT/config.sample.json"),
            "src/__tests__/App.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/App.test.mjs"),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            "README.md" to
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

    private fun basicDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
