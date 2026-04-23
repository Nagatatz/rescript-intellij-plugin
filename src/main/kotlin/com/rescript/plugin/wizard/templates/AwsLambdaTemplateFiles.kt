package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders
import com.rescript.plugin.wizard.ValidationLibrary

/**
 * Generates project template files for an AWS Lambda Hono service.
 *
 * Uses esbuild to bundle the compiled ReScript output into a single ESM file deployable as
 * a Lambda handler. Ships README, .gitignore (`dist/`), .editorconfig, and a CI workflow.
 * Runtime HTTP body validation is provided by `Validation.res` (zod or sury, selected in
 * Wizard via [TemplateContext.validationLibrary]).
 */
internal object AwsLambdaTemplateFiles {
    private const val RESOURCE_ROOT = "aws-lambda"

    /**
     * Generates AWS Lambda template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val deployVars =
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
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to ctx.nodeEngine),
                    dependencies = awsLambdaDependencies(ctx.validationLibrary),
                    devDependencies =
                        linkedMapOf(
                            "esbuild" to TemplateVersions.ESBUILD,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "bundle" to
                                "esbuild src/Server.res.mjs --bundle --platform=node " +
                                "--outfile=dist/index.mjs --format=esm",
                            "build" to "rescript && ${ctx.runCmd("bundle")}",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoLambda.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/HonoLambda.res"),
            "src/Validation.res" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/variants/$variantKey/src/Validation.res"),
            "src/Server.res" to TemplateResourceLoader.load("$RESOURCE_ROOT/src/Server.res"),
            "src/__tests__/Server.test.mjs" to
                TemplateResourceLoader.load("$RESOURCE_ROOT/src/__tests__/Server.test.mjs"),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "An AWS Lambda function powered by Hono and ReScript, bundled with esbuild. " +
                            "Ships POST/GET endpoints and a DynamoDB integration recipe in the README.",
                    scripts =
                        listOf(
                            "build" to "Compile ReScript and bundle into dist/index.mjs",
                            "bundle" to "Run esbuild only",
                            "test" to "Run Vitest",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "API" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/api.md"),
                            "Deploy" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/deploy.md", deployVars),
                            "DynamoDB Recipe" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/dynamodb.md"),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(ctx),
            "LICENSE" to CommonFiles.mitLicense(ctx, holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "*.zip", ".aws-sam/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun awsLambdaDependencies(validationLibrary: ValidationLibrary): LinkedHashMap<String, String> {
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["hono"] = TemplateVersions.HONO
        when (validationLibrary) {
            ValidationLibrary.ZOD -> deps["zod"] = TemplateVersions.ZOD
            ValidationLibrary.SURY -> deps["sury"] = TemplateVersions.SURY
        }
        return deps
    }
}
