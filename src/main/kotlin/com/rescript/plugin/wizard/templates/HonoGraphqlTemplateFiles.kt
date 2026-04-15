package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Hono + GraphQL Yoga + Drizzle SQLite project.
 *
 * Stub implementation — to be fully populated in the Hono GraphQL implementation step.
 * Currently emits a minimal valid project (Hono root + placeholder schema + Drizzle
 * configuration) so the wizard, integration tests, and downstream consumers can rely
 * on the template enum entry from the foundation step onwards.
 */
internal object HonoGraphqlTemplateFiles {
    /**
     * Generates Hono GraphQL template files using the supplied [TemplateContext].
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
                            "@hono/node-server" to TemplateVersions.HONO_NODE_SERVER,
                            "graphql" to TemplateVersions.GRAPHQL,
                            "graphql-yoga" to TemplateVersions.GRAPHQL_YOGA,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Server.res" to placeholderServer(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A Hono GraphQL service powered by graphql-yoga and Drizzle (placeholder).",
                    scripts =
                        listOf(
                            "dev" to "Run the GraphQL server with file watching",
                            "start" to "Run the server once",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("data/", "docs/schema.md")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun placeholderServer(): String =
        buildString {
            appendLine("// Placeholder server. Replaced with a full GraphQL implementation in Step 7.")
            appendLine("let app = Hono.createApp()")
            appendLine("")
            appendLine("app->Hono.get(\"/\", ctx => {")
            appendLine("  ctx->Hono.text(\"Hono GraphQL placeholder\")")
            appendLine("})")
            appendLine("")
            appendLine("HonoNodeServer.serve(app, {port: 4000})")
            append("Console.log(\"Server running on http://localhost:4000\")")
        }
}
