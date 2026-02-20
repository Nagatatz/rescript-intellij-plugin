package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ProjectFileBuilders

internal object MonorepoTemplateFiles {
    fun generate(projectName: String): Map<String, String> =
        buildMap {
            put(
                "package.json",
                ProjectFileBuilders.packageJson(
                    name = projectName,
                    workspaces = listOf("packages/*"),
                    isPrivate = true,
                    scripts =
                        linkedMapOf(
                            "dev:server" to "cd packages/server && npm run start",
                            "dev:client" to "cd packages/client && npm run dev",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            )
            put("packages/shared/rescript.json", ProjectFileBuilders.rescriptJson(name = "@$projectName/shared"))
            put(
                "packages/shared/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$projectName/shared",
                    dependencies = linkedMapOf("rescript" to "^12.0.0", "@rescript/core" to "^1.0.0"),
                    type = "module",
                ),
            )
            put("packages/shared/src/Types.res", "type user = {\n  id: string,\n  name: string,\n}")
            put(
                "packages/server/rescript.json",
                ProjectFileBuilders.rescriptJson(
                    name = "@$projectName/server",
                    bsDependencies = listOf("@rescript/core", "@$projectName/shared"),
                ),
            )
            put(
                "packages/server/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$projectName/server",
                    dependencies =
                        linkedMapOf(
                            "rescript" to "^12.0.0",
                            "@rescript/core" to "^1.0.0",
                            "@$projectName/shared" to "*",
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
            )
            put("packages/server/src/Hono.res", ProjectFileBuilders.honoBindings())
            put("packages/server/src/HonoNodeServer.res", ProjectFileBuilders.honoNodeServerBindings())
            put(
                "packages/server/src/Server.res",
                "let app = Hono.createApp()\n\napp->Hono.get(\"/\", ctx => {\n  ctx->Hono.json({\"message\": \"Hello from server!\"})\n})\n\nHonoNodeServer.serve(app, {port: 3000})\nConsole.log(\"Server running on http://localhost:3000\")",
            )
            put(
                "packages/client/rescript.json",
                ProjectFileBuilders.rescriptJson(
                    name = "@$projectName/client",
                    bsDependencies = listOf("@rescript/core", "@rescript/react", "@$projectName/shared"),
                    includeJsx = true,
                ),
            )
            put(
                "packages/client/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$projectName/client",
                    dependencies =
                        linkedMapOf(
                            "rescript" to "^12.0.0",
                            "@rescript/core" to "^1.0.0",
                            "@rescript/react" to "^0.14.0",
                            "@$projectName/shared" to "*",
                            "react" to "^19.0.4",
                            "react-dom" to "^19.0.4",
                        ),
                    devDependencies = linkedMapOf("@vitejs/plugin-react" to "^5.0.0", "vite" to "^7.0.0"),
                    scripts =
                        linkedMapOf(
                            "dev" to "vite",
                            "build" to "vite build",
                            "preview" to "vite preview",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            )
            put(
                "packages/client/index.html",
                "<!DOCTYPE html>\n<html lang=\"en\">\n  <head>\n    <meta charset=\"UTF-8\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n    <title>$projectName</title>\n  </head>\n  <body>\n    <div id=\"root\"></div>\n    <script type=\"module\" src=\"/src/Main.res.mjs\"></script>\n  </body>\n</html>",
            )
            put(
                "packages/client/vite.config.mjs",
                "import { defineConfig } from \"vite\";\nimport react from \"@vitejs/plugin-react\";\n\nexport default defineConfig({\n  plugins: [react()],\n});",
            )
            put(
                "packages/client/src/App.res",
                "@react.component\nlet make = () => {\n  <div>\n    {React.string(\"Hello, ReScript Monorepo!\")}\n  </div>\n}",
            )
            put(
                "packages/client/src/Main.res",
                "switch ReactDOM.Client.createRoot(ReactDOM.querySelector(\"#root\")) {\n| Some(root) => ReactDOM.Client.Root.render(root, <App />)\n| None => Console.error(\"Could not find root element\")\n}",
            )
        }
}
