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
            "src/Server.res" to serverRes(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A Hono service deployable to Google Cloud Run. Ships POST/GET endpoints, " +
                            "environment-variable reading, and a Cloud SQL integration recipe.",
                    scripts =
                        listOf(
                            "dev" to "Run locally with file watching",
                            "start" to "Run the server once",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "API" to apiSection(),
                            "Environment" to environmentSection(),
                            "Deploy" to deploySection(ctx),
                            "Cloud SQL Recipe" to cloudSqlRecipe(),
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

    private fun serverRes(): String {
        val dollar = '$'
        return buildString {
            appendLine("// Request/response types.")
            appendLine("type echoPayload = {message: string}")
            appendLine("")
            appendLine("// Cloud Run sets PORT on startup — respect it, fall back to 8080 locally.")
            appendLine("@val external processEnv: Dict.t<string> = \"process.env\"")
            appendLine("")
            appendLine("let port =")
            appendLine("  processEnv")
            appendLine("  ->Dict.get(\"PORT\")")
            appendLine("  ->Option.flatMap(Int.fromString(_))")
            appendLine("  ->Option.getOr(8080)")
            appendLine("")
            appendLine("let app = Hono.createApp()")
            appendLine("")
            appendLine("app->Hono.get(\"/\", ctx => ctx->Hono.text(\"Cloud Run + Hono + ReScript\"))")
            appendLine("")
            appendLine("app->Hono.post(\"/echo\", async ctx => {")
            appendLine("  let payload: echoPayload = await ctx->Hono.req->Hono.jsonBody")
            appendLine(
                "  ctx->Hono.json({\"echo\": payload.message, " +
                    "\"receivedAt\": Date.now()->Float.toString})",
            )
            appendLine("})")
            appendLine("")
            appendLine("HonoNodeServer.serve(app, {port: port})")
            append("Console.log(`Server running on http://localhost:$dollar{port->Int.toString}`)")
        }
    }

    private fun apiSection(): String =
        buildString {
            appendLine("| Endpoint | Method | Description |")
            appendLine("| --- | --- | --- |")
            appendLine("| `/` | GET | Health check |")
            append("| `/echo` | POST | Returns `{ message }` with a receivedAt timestamp |")
        }

    private fun environmentSection(): String =
        buildString {
            appendLine("The server reads `PORT` from `process.env` and falls back to `8080`. Cloud Run")
            append("sets PORT automatically; override locally with `PORT=3000 pnpm dev`.")
        }

    private fun deploySection(ctx: TemplateContext): String {
        val dollar = '$'
        return buildString {
            appendLine("```bash")
            appendLine("gcloud builds submit --tag gcr.io/PROJECT-ID/${ctx.projectName}")
            appendLine("gcloud run deploy ${ctx.projectName} \\\\")
            appendLine("  --image gcr.io/PROJECT-ID/${ctx.projectName} \\\\")
            appendLine("  --port 8080 \\\\")
            appendLine("  --allow-unauthenticated")
            append("```")
        }
    }

    private fun cloudSqlRecipe(): String =
        buildString {
            appendLine("Connect to Cloud SQL via the `@google-cloud/cloud-sql-connector` or a")
            appendLine("standard Postgres driver with the Cloud SQL Auth Proxy.")
            appendLine()
            appendLine("```bash")
            appendLine("pnpm add pg @google-cloud/cloud-sql-connector")
            appendLine("```")
            appendLine()
            appendLine("```rescript")
            appendLine("@module(\"pg\") @new external makePool: 'opts => 'pool = \"Pool\"")
            appendLine("@send external queryAsync: ('pool, string, array<'a>) => promise<'rows> = \"query\"")
            appendLine("```")
            appendLine()
            append(
                "Pass the `DATABASE_URL` as an environment variable via `--set-env-vars` on deploy.",
            )
        }

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
