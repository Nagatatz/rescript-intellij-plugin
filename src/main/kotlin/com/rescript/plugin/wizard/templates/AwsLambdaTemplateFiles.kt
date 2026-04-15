package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for an AWS Lambda Hono service.
 *
 * Uses esbuild to bundle the compiled ReScript output into a single ESM file deployable as
 * a Lambda handler. Ships README, .gitignore (`dist/`), .editorconfig, and a CI workflow.
 */
internal object AwsLambdaTemplateFiles {
    /**
     * Generates AWS Lambda template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = ctx.projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = ctx.projectName,
                    isPrivate = true,
                    type = "module",
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "hono" to TemplateVersions.HONO,
                        ),
                    devDependencies = linkedMapOf("esbuild" to TemplateVersions.ESBUILD),
                    scripts =
                        linkedMapOf(
                            "bundle" to
                                "esbuild src/Server.res.mjs --bundle --platform=node " +
                                "--outfile=dist/index.mjs --format=esm",
                            "build" to "rescript && ${ctx.runCmd("bundle")}",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoLambda.res" to
                "type lambdaEvent\ntype lambdaResult\n" +
                "type handler = (lambdaEvent) => promise<lambdaResult>\n\n" +
                "@module(\"hono/aws-lambda\") external handle: Hono.app => handler = \"handle\"",
            "src/Server.res" to
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n" +
                "  ctx->Hono.text(\"Hello, AWS Lambda + ReScript!\")\n})\n\n" +
                "%%raw(\"export const handler = HonoLambda.handle(app)\")",
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "An AWS Lambda function powered by Hono and ReScript, bundled with esbuild.",
                    scripts =
                        listOf(
                            "build" to "Compile ReScript and bundle into dist/index.mjs",
                            "bundle" to "Run esbuild only",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Deploy" to
                                "After running `${ctx.runCmd("build")}`, upload `dist/index.mjs` as " +
                                "your Lambda handler. Set the runtime to Node.js 20 and the handler to `index.handler`.",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "*.zip")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
