package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Cloudflare Workers Hono service.
 *
 * The generated app goes beyond "hello world": it wires POST + GET against a KV namespace so
 * users have a working storage example out of the box (add/list greetings). `wrangler.jsonc`
 * pre-declares the KV binding so `wrangler dev` boots with a real local store.
 */
internal object CloudflareWorkersTemplateFiles {
    /**
     * Generates Cloudflare Workers template files using the supplied [TemplateContext].
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
                            "wrangler" to TemplateVersions.WRANGLER,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "wrangler dev",
                            "deploy" to "wrangler deploy",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "wrangler.jsonc" to wranglerConfig(ctx.projectName),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/Kv.res" to kvBindings(),
            "src/Server.res" to serverRes(),
            "src/__tests__/Server.test.mjs" to serverTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A Cloudflare Workers service powered by Hono, demonstrating POST/GET against " +
                            "a Workers KV namespace.",
                    scripts =
                        listOf(
                            "dev" to "Run wrangler dev locally",
                            "deploy" to "Deploy with `wrangler deploy`",
                            "test" to "Run Vitest",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "API" to apiSection(),
                            "KV Setup" to kvSetupSection(),
                            "Deploy" to deploySection(ctx),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".wrangler/", "dist/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
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

    private fun wranglerConfig(projectName: String): String =
        buildString {
            appendLine("{")
            appendLine("  // Workers KV binding. Replace the placeholder id after running:")
            appendLine("  //   npx wrangler kv namespace create GREETINGS")
            appendLine("  \"name\": \"$projectName\",")
            appendLine("  \"main\": \"src/Server.res.mjs\",")
            appendLine("  \"compatibility_date\": \"2024-01-01\",")
            appendLine("  \"kv_namespaces\": [")
            appendLine("    { \"binding\": \"GREETINGS\", \"id\": \"REPLACE_WITH_KV_NAMESPACE_ID\" }")
            appendLine("  ]")
            append("}")
        }

    private fun kvBindings(): String =
        buildString {
            appendLine("// Bindings over the Workers KV API (subset). The runtime exposes the")
            appendLine("// binding on `env.GREETINGS` (see Server.res).")
            appendLine("type namespace")
            appendLine("type listResult = {keys: array<{\"name\": string}>}")
            appendLine("")
            appendLine("@send external get: (namespace, string) => promise<Nullable.t<string>> = \"get\"")
            appendLine("@send external put: (namespace, string, string) => promise<unit> = \"put\"")
            appendLine("@send external list: namespace => promise<listResult> = \"list\"")
        }

    private fun serverRes(): String =
        buildString {
            appendLine("// Request/response types shared between routes.")
            appendLine("type greetingPayload = {name: string}")
            appendLine("")
            appendLine("// env is the bindings object Workers injects. We narrow it for our KV binding.")
            appendLine("type env = {\"GREETINGS\": Kv.namespace}")
            appendLine("")
            appendLine("let app = Hono.createApp()")
            appendLine("")
            appendLine("app->Hono.get(\"/\", ctx => ctx->Hono.text(\"Workers + Hono + ReScript\"))")
            appendLine("")
            appendLine("app->Hono.post(\"/greetings\", async ctx => {")
            appendLine("  let env: env = %raw(\"ctx.env\")")
            appendLine("  let payload: greetingPayload = await ctx->Hono.req->Hono.jsonBody")
            appendLine(
                "  await env[\"GREETINGS\"]->Kv.put(payload.name, " +
                    "Date.now()->Float.toString)",
            )
            appendLine("  ctx->Hono.status(201)->Hono.json({\"ok\": true, \"name\": payload.name})")
            appendLine("})")
            appendLine("")
            appendLine("app->Hono.get(\"/greetings\", async ctx => {")
            appendLine("  let env: env = %raw(\"ctx.env\")")
            appendLine("  let result = await env[\"GREETINGS\"]->Kv.list")
            appendLine(
                "  ctx->Hono.json({\"names\": result.keys->Array.map(k => k[\"name\"])})",
            )
            appendLine("})")
            appendLine("")
            append("%%raw(\"export default app\")")
        }

    private fun apiSection(): String =
        buildString {
            appendLine("| Endpoint | Method | Description |")
            appendLine("| --- | --- | --- |")
            appendLine("| `/` | GET | Health check |")
            appendLine("| `/greetings` | POST | Store `{ name }` in KV |")
            append("| `/greetings` | GET | List stored names |")
        }

    private fun kvSetupSection(): String =
        buildString {
            appendLine("Create the namespace and paste the returned ID into `wrangler.jsonc`:")
            appendLine()
            appendLine("```bash")
            appendLine("npx wrangler kv namespace create GREETINGS")
            append("```")
        }

    private fun deploySection(ctx: TemplateContext): String =
        buildString {
            appendLine("1. Authenticate: `npx wrangler login`")
            append("2. Deploy: ${ctx.runCmd("deploy")}")
        }
}
