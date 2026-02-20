package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

internal object AwsLambdaTemplateFiles {
    fun generate(projectName: String): Map<String, String> =
        mapOf(
            "rescript.json" to ProjectFileBuilders.rescriptJson(name = projectName),
            "package.json" to
                ProjectFileBuilders.packageJson(
                    name = projectName,
                    dependencies =
                        linkedMapOf(
                            "rescript" to "^12.0.0",
                            "@rescript/core" to "^1.0.0",
                            "hono" to "^4.0.0",
                        ),
                    devDependencies = linkedMapOf("esbuild" to "^0.25.0"),
                    scripts =
                        linkedMapOf(
                            "bundle" to
                                "esbuild src/Server.res.mjs --bundle --platform=node --outfile=dist/index.mjs --format=esm",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    type = "module",
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoLambda.res" to
                "type lambdaEvent\ntype lambdaResult\ntype handler = (lambdaEvent) => promise<lambdaResult>\n\n@module(\"hono/aws-lambda\") external handle: Hono.app => handler = \"handle\"",
            "src/Server.res" to
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n  ctx->Hono.text(\"Hello, AWS Lambda + ReScript!\")\n})\n\n%%raw(\"export const handler = HonoLambda.handle(app)\")",
        )
}
