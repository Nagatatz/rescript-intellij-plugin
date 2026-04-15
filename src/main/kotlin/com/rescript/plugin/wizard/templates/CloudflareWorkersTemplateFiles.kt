package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Cloudflare Workers Hono service.
 *
 * Ships wrangler config, README with deploy instructions, .gitignore (including `.wrangler/`),
 * .editorconfig, and a CI workflow.
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
                    devDependencies = linkedMapOf("wrangler" to TemplateVersions.WRANGLER),
                    scripts =
                        linkedMapOf(
                            "dev" to "wrangler dev",
                            "deploy" to "wrangler deploy",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "wrangler.jsonc" to
                "{\n  \"name\": \"${ctx.projectName}\",\n  \"main\": \"src/Server.res.mjs\",\n" +
                "  \"compatibility_date\": \"2024-01-01\"\n}",
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/Server.res" to
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n" +
                "  ctx->Hono.text(\"Hello, Cloudflare Workers + ReScript!\")\n})\n\n" +
                "%%raw(\"export default app\")",
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A Cloudflare Workers service powered by Hono and ReScript.",
                    scripts =
                        listOf(
                            "dev" to "Run wrangler dev locally",
                            "deploy" to "Deploy with `wrangler deploy`",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Deploy" to
                                "1. Authenticate: `npx wrangler login`\n" +
                                "2. Deploy: ${ctx.runCmd("deploy")}",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf(".wrangler/", "dist/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))
}
