package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a single-package full-stack ReScript app.
 *
 * Stub implementation — to be fully populated in the Full-Stack implementation step.
 * Currently emits a minimal valid project so the wizard, integration tests, and
 * downstream consumers can rely on the template enum entry from the foundation step
 * onwards. The full implementation in Step 8 wires Hono + Drizzle on the server side
 * and Vite+ + React on the client side, sharing types via `src/shared/`.
 */
internal object FullStackTemplateFiles {
    /**
     * Generates Full-Stack template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> =
        mapOf(
            "rescript.json" to
                ProjectFileBuilders.rescriptJson(
                    name = ctx.projectName,
                    bsDependencies = listOf("@rescript/core", "@rescript/react"),
                    includeJsx = true,
                    sources =
                        "  \"sources\": [\n" +
                            "    {\"dir\": \"src/shared\", \"subdirs\": true},\n" +
                            "    {\"dir\": \"src/server\", \"subdirs\": true},\n" +
                            "    {\"dir\": \"src/client\", \"subdirs\": true}\n" +
                            "  ],",
                ),
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
                            "@rescript/react" to TemplateVersions.RESCRIPT_REACT,
                            "react" to TemplateVersions.REACT,
                            "react-dom" to TemplateVersions.REACT_DOM,
                            "hono" to TemplateVersions.HONO,
                            "@hono/node-server" to TemplateVersions.HONO_NODE_SERVER,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev:server" to "node --watch src/server/Main.res.mjs",
                            "dev:client" to "vp dev",
                            "build" to "vp build",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/shared/Api.res" to placeholderApi(),
            "src/server/Main.res" to placeholderServer(),
            "src/server/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/server/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/client/App.res" to ProjectFileBuilders.reactComponent(),
            "src/client/Main.res" to placeholderClientMain(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A single-package full-stack ReScript app (placeholder).",
                    scripts =
                        listOf(
                            "dev:server" to "Run the Hono backend with watch mode",
                            "dev:client" to "Run the Vite+ client",
                            "build" to "Build the client for production",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("data/", "dist/", ".vite/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun placeholderApi(): String =
        buildString {
            appendLine("// Shared request/response types for the Full-Stack template.")
            appendLine("type pingResponse = {message: string}")
        }

    private fun placeholderServer(): String =
        buildString {
            appendLine("let app = Hono.createApp()")
            appendLine("")
            appendLine("app->Hono.get(\"/api/ping\", ctx => {")
            appendLine("  ctx->Hono.json({\"message\": \"pong\"})")
            appendLine("})")
            appendLine("")
            appendLine("HonoNodeServer.serve(app, {port: 3000})")
            append("Console.log(\"Server running on http://localhost:3000\")")
        }

    private fun placeholderClientMain(): String =
        buildString {
            appendLine("switch ReactDOM.querySelector(\"#root\") {")
            appendLine("| Some(rootEl) =>")
            appendLine("  ReactDOM.Client.Root.render(ReactDOM.Client.createRoot(rootEl), <App />)")
            appendLine("| None => Console.error(\"Could not find root element\")")
            append("}")
        }
}
