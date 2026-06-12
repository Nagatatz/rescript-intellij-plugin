package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for an AWS Lambda Hono service.
 *
 * Uses esbuild to bundle the compiled ReScript output into a single ESM file deployable as
 * a Lambda handler. Ships a local Node entry (`src/Local.res` + `@hono/node-server`) so
 * developers can iterate without deploying, plus README, .gitignore (`dist/`), .editorconfig,
 * and a CI workflow. Runtime HTTP body validation is provided by `Validation.res` (zod or
 * sury, selected in Wizard via [TemplateContext.validationLibrary]).
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
                "nodeMajor" to ctx.nodeMajor,
            )
        val bundlingVars =
            mapOf(
                "projectName" to ctx.projectName,
                "nodeMajor" to ctx.nodeMajor,
            )
        val localVars =
            mapOf(
                "cmdDev" to ctx.runCmd("dev"),
                "cmdResDev" to ctx.runCmd("res:dev"),
            )
        val deps = linkedMapOf<String, String>()
        deps["rescript"] = TemplateVersions.RESCRIPT
        deps["@rescript/core"] = TemplateVersions.RESCRIPT_CORE
        deps["@rescript/runtime"] = TemplateVersions.RESCRIPT_RUNTIME
        deps["hono"] = TemplateVersions.HONO
        val (validationName, validationVersion) = TemplateScaffold.validationDependency(ctx)
        deps[validationName] = validationVersion
        val readme =
            CommonFiles.readme(
                ctx = ctx,
                description =
                    "An AWS Lambda function powered by Hono and ReScript, bundled with esbuild. " +
                        "Ships POST/GET endpoints, a local Node entry for fast iteration, " +
                        "and a DynamoDB integration recipe in the README.",
                scripts =
                    listOf(
                        "build" to "Compile ReScript and bundle into dist/index.mjs",
                        "bundle" to "Run esbuild only",
                        "dev" to "Run the local Node server with file watching",
                        "start" to "Run the local Node server once",
                        "test" to "Run Vitest",
                        "res:dev" to "Watch ReScript sources",
                    ),
                extraSections =
                    listOf(
                        "API" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/api.md"),
                        "Local development" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/local.md", localVars),
                        "Deploy" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/deploy.md", deployVars),
                        "Bundling Strategy" to
                            TemplateResourceLoader.load("$RESOURCE_ROOT/readme/bundling.md", bundlingVars),
                        "DynamoDB Recipe" to TemplateResourceLoader.load("$RESOURCE_ROOT/readme/dynamodb.md"),
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
                isPrivate = true,
                type = "module",
                packageManager = ctx.packageManagerSpec(),
                engines = mapOf("node" to ctx.nodeEngine),
                dependencies = deps,
                // `@hono/node-server` powers `src/Local.res` only and is not
                // referenced from `src/Server.res`, so the esbuild bundle
                // (entry: Server.res.mjs) never pulls it in. Keeping it as a
                // devDependency makes that contract explicit.
                devDependencies =
                    linkedMapOf(
                        "esbuild" to TemplateVersions.ESBUILD,
                        "vitest" to TemplateVersions.VITEST,
                        "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        "@hono/node-server" to TemplateVersions.HONO_NODE_SERVER,
                    ),
                scripts =
                    linkedMapOf(
                        "bundle" to
                            "esbuild src/Server.res.mjs --bundle --platform=node " +
                            "--outfile=dist/index.mjs --format=esm",
                        "build" to "rescript && ${ctx.runCmd("bundle")}",
                        // `dev` / `start` invoke the local-only entry
                        // (`src/Local.res`). The Lambda handler exported
                        // from `src/Server.res` is unused on Node.
                        "start" to "node src/Local.res.mjs",
                        "dev" to "node --watch src/Local.res.mjs",
                        "test" to "vitest run",
                        "test:coverage" to "vitest run --coverage",
                        "res:build" to "rescript",
                        "res:clean" to "rescript clean",
                        "res:dev" to "rescript -w",
                    ),
            )
        files["src/Hono.res"] = ProjectFileBuilders.honoBindings()
        files["src/HonoLambda.res"] = TemplateResourceLoader.load("$RESOURCE_ROOT/src/HonoLambda.res")
        files["src/HonoNodeServer.res"] = ProjectFileBuilders.honoNodeServerBindings()
        files += TemplateScaffold.validationVariant(ctx, RESOURCE_ROOT)
        files.putAll(
            TemplateScaffold.resourceFiles(
                RESOURCE_ROOT,
                listOf("src/Server.res", "src/Local.res", "src/__tests__/Server.test.mjs"),
            ),
        )
        files.putAll(
            TemplateScaffold.commonTail(
                ctx,
                readme = readme,
                gitignoreExtra = listOf("dist/", "*.zip", ".aws-sam/"),
                ciHasBuild = true,
                ciHasTest = true,
            ),
        )
        return files
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
