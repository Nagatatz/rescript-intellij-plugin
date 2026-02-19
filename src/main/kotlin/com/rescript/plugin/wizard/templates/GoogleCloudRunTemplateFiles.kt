package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

internal object GoogleCloudRunTemplateFiles {
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
            "Dockerfile" to
                "FROM node:22-slim\nWORKDIR /app\nCOPY package*.json ./\nRUN npm install\nCOPY . .\nRUN npx rescript\nEXPOSE 8080\nCMD [\"node\", \"src/Server.res.mjs\"]",
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Server.res" to
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n  ctx->Hono.text(\"Hello, Cloud Run + ReScript!\")\n})\n\nHonoNodeServer.serve(app, {port: 8080})\nConsole.log(\"Server running on http://localhost:8080\")",
        )
}
