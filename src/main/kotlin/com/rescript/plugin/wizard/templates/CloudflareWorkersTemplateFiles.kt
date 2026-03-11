package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/** Generates project files for the Cloudflare Workers ReScript template. */
internal object CloudflareWorkersTemplateFiles {
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
                    devDependencies = linkedMapOf("wrangler" to "^4.0.0"),
                    scripts =
                        linkedMapOf(
                            "dev" to "wrangler dev",
                            "deploy" to "wrangler deploy",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    type = "module",
                ),
            "wrangler.jsonc" to
                "{\n  \"name\": \"$projectName\",\n  \"main\": \"src/Server.res.mjs\",\n  \"compatibility_date\": \"2024-01-01\"\n}",
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/Server.res" to
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n  ctx->Hono.text(\"Hello, Cloudflare Workers + ReScript!\")\n})\n\n%%raw(\"export default app\")",
        )
}
