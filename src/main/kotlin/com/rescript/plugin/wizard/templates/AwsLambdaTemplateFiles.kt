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
                    devDependencies =
                        linkedMapOf(
                            "esbuild" to TemplateVersions.ESBUILD,
                            "vitest" to TemplateVersions.VITEST,
                        ),
                    scripts =
                        linkedMapOf(
                            "bundle" to
                                "esbuild src/Server.res.mjs --bundle --platform=node " +
                                "--outfile=dist/index.mjs --format=esm",
                            "build" to "rescript && ${ctx.runCmd("bundle")}",
                            "test" to "vitest run",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoLambda.res" to honoLambdaBindings(),
            "src/Server.res" to serverRes(),
            "src/__tests__/Server.test.mjs" to serverTest(),
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
                            "API" to apiSection(),
                            "Deploy" to deploySection(ctx),
                            "DynamoDB Recipe" to dynamoDbRecipe(),
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "*.zip")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun serverTest(): String =
        buildString {
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("")
            appendLine("describe(\"Server module\", () => {")
            appendLine("  it(\"loads without throwing\", async () => {")
            appendLine("    await expect(import(\"../Server.res.mjs\")).resolves.toBeDefined();")
            appendLine("  });")
            appendLine("});")
        }

    private fun honoLambdaBindings(): String =
        buildString {
            appendLine("// hono/aws-lambda adapter: converts a Hono app into a Lambda handler.")
            appendLine("type lambdaEvent")
            appendLine("type lambdaResult")
            appendLine("type handler = lambdaEvent => promise<lambdaResult>")
            appendLine("")
            appendLine("@module(\"hono/aws-lambda\") external handle: Hono.app => handler = \"handle\"")
        }

    private fun serverRes(): String =
        buildString {
            appendLine("// Request/response types.")
            appendLine("type createOrderPayload = {productId: string, quantity: int}")
            appendLine("type createOrderResponse = {orderId: string, productId: string, quantity: int}")
            appendLine("")
            appendLine("let app = Hono.createApp()")
            appendLine("")
            appendLine("app->Hono.get(\"/\", ctx => ctx->Hono.text(\"Lambda + Hono + ReScript\"))")
            appendLine("")
            appendLine("// Example GET with a path param")
            appendLine("app->Hono.get(\"/orders/:id\", ctx => {")
            appendLine("  let id = ctx->Hono.req->Hono.paramAt(\"id\")")
            appendLine("  ctx->Hono.json({\"orderId\": id, \"status\": \"pending\"})")
            appendLine("})")
            appendLine("")
            appendLine("// Example POST with JSON body.")
            appendLine("app->Hono.post(\"/orders\", async ctx => {")
            appendLine("  let payload: createOrderPayload = await ctx->Hono.req->Hono.jsonBody")
            appendLine("  let response: createOrderResponse = {")
            appendLine("    orderId: \"ord_\" ++ Date.now()->Float.toString,")
            appendLine("    productId: payload.productId,")
            appendLine("    quantity: payload.quantity,")
            appendLine("  }")
            appendLine("  ctx->Hono.status(201)->Hono.json(response)")
            appendLine("})")
            appendLine("")
            append("%%raw(\"export const handler = HonoLambda.handle(app)\")")
        }

    private fun apiSection(): String =
        buildString {
            appendLine("| Endpoint | Method | Description |")
            appendLine("| --- | --- | --- |")
            appendLine("| `/` | GET | Health check |")
            appendLine("| `/orders/:id` | GET | Look up an order by ID |")
            append("| `/orders` | POST | Create an order from `{ productId, quantity }` |")
        }

    private fun deploySection(ctx: TemplateContext): String =
        buildString {
            appendLine("Build & upload:")
            appendLine()
            appendLine("```bash")
            appendLine(ctx.runCmd("build"))
            appendLine("cd dist && zip lambda.zip index.mjs")
            appendLine("aws lambda update-function-code --function-name ${ctx.projectName} \\\\")
            appendLine("  --zip-file fileb://lambda.zip")
            appendLine("```")
            appendLine()
            append("Runtime: Node.js 20. Handler: `index.handler`. Use API Gateway HTTP API as the trigger.")
        }

    private fun dynamoDbRecipe(): String =
        buildString {
            appendLine("Persist orders to DynamoDB:")
            appendLine()
            appendLine("```bash")
            appendLine("pnpm add @aws-sdk/client-dynamodb @aws-sdk/lib-dynamodb")
            appendLine("```")
            appendLine()
            appendLine("```rescript")
            appendLine("type docClient")
            appendLine("@module(\"@aws-sdk/lib-dynamodb\") @new")
            appendLine("external makeClient: 'opts => docClient = \"DynamoDBDocumentClient\"")
            appendLine("@send external send: (docClient, 'command) => promise<'result> = \"send\"")
            appendLine("```")
            appendLine()
            append("Grant the Lambda IAM role `dynamodb:PutItem` / `dynamodb:GetItem` on the target table.")
        }
}
