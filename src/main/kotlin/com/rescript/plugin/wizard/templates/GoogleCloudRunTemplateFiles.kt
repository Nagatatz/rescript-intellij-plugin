package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Google Cloud Run Hono service.
 *
 * Container-based deployment with a multi-stage Dockerfile, README, .gitignore, .editorconfig,
 * and CI workflow. The Dockerfile honors the selected package manager so the production image
 * uses the same toolchain as local development.
 */
internal object GoogleCloudRunTemplateFiles {
    /**
     * Generates Google Cloud Run template files using the supplied [TemplateContext].
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
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "Dockerfile" to dockerfile(ctx),
            ".dockerignore" to "node_modules\nlib\n.git\n.github\n.idea\n.vscode\n",
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Server.res" to
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n" +
                "  ctx->Hono.text(\"Hello, Cloud Run + ReScript!\")\n})\n\n" +
                "HonoNodeServer.serve(app, {port: 8080})\nConsole.log(\"Server running on http://localhost:8080\")",
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description = "A Hono service deployable to Google Cloud Run, written in ReScript.",
                    scripts =
                        listOf(
                            "dev" to "Run locally with file watching",
                            "start" to "Run the server once",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Deploy" to
                                "Build the container image and deploy to Cloud Run:\n\n" +
                                "```bash\ngcloud builds submit --tag gcr.io/PROJECT-ID/${ctx.projectName}\n" +
                                "gcloud run deploy ${ctx.projectName} \\\n" +
                                "  --image gcr.io/PROJECT-ID/${ctx.projectName} \\\n  --port 8080\n```",
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun dockerfile(ctx: TemplateContext): String {
        val installInPm =
            when (ctx.packageManager) {
                PackageManager.NPM -> "npm install --omit=dev"
                PackageManager.PNPM -> "corepack enable && pnpm install --prod --frozen-lockfile=false"
                PackageManager.YARN -> "corepack enable && yarn install --production"
            }
        return buildString {
            appendLine("FROM node:22-slim")
            appendLine("WORKDIR /app")
            appendLine("COPY package*.json ./")
            appendLine("RUN $installInPm")
            appendLine("COPY . .")
            appendLine("RUN ${ctx.execCmd("rescript")}")
            appendLine("EXPOSE 8080")
            append("CMD [\"node\", \"src/Server.res.mjs\"]")
        }
    }
}
