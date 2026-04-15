package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

/** Generates project template files for a Hono web framework ReScript project. */
internal object HonoTemplateFiles {
    /** Context-aware entry point; falls through to the project-name generator until migrated. */
    fun generate(ctx: TemplateContext): Map<String, String> = generate(ctx.projectName)

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
                            "@hono/node-server" to "^1.0.0",
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    type = "module",
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Server.res" to
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n  ctx->Hono.text(\"Hello, Hono + ReScript!\")\n})\n\nHonoNodeServer.serve(app, {port: 3000})\nConsole.log(\"Server running on http://localhost:3000\")",
        )
}
