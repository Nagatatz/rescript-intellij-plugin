package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Hono web service running on Node.js.
 *
 * Bundles README, .gitignore, .editorconfig, CI workflow, and a Vitest sample so that
 * users can run `pnpm test` immediately after generation.
 */
internal object HonoTemplateFiles {
    /**
     * Generates Hono template files using the supplied [TemplateContext].
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
                        ),
                    devDependencies = linkedMapOf("vitest" to TemplateVersions.VITEST),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "test" to "vitest run",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Server.res" to
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n" +
                "  ctx->Hono.text(\"Hello, Hono + ReScript!\")\n})\n\n" +
                "HonoNodeServer.serve(app, {port: 3000})\nConsole.log(\"Server running on http://localhost:3000\")",
            "src/__tests__/Server.test.mjs" to serverTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A Hono web service running on Node.js, written in ReScript.",
                    scripts =
                        listOf(
                            "dev" to "Run the server with file watching",
                            "start" to "Run the server once",
                            "test" to "Run Vitest",
                            "res:dev" to "Watch ReScript sources",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "coverage/")),
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
            appendLine("describe(\"server module\", () => {")
            appendLine("  it(\"loads without throwing\", async () => {")
            appendLine("    await expect(import(\"../Server.res.mjs\")).resolves.toBeDefined();")
            appendLine("  });")
            appendLine("});")
        }
}
