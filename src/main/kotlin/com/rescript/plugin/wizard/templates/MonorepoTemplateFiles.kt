package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a full-stack monorepo (Hono backend + Vite+ React frontend).
 *
 * Layout:
 * - `packages/shared` — shared ReScript types
 * - `packages/server` — Hono + Node.js HTTP server
 * - `packages/client` — React frontend bundled by Vite+
 *
 * Workspace plumbing adapts to the selected package manager: pnpm gets a `pnpm-workspace.yaml`
 * file, while npm/yarn use the `workspaces` field in the root `package.json`.
 */
internal object MonorepoTemplateFiles {
    /**
     * Generates Monorepo template files using the supplied [TemplateContext].
     */
    fun generate(ctx: TemplateContext): Map<String, String> {
        val name = ctx.projectName
        val pm = ctx.packageManager
        val devCommand = devScript(pm)
        val workspaceField = if (pm == PackageManager.PNPM) null else listOf("packages/*")

        return buildMap {
            put(
                "package.json",
                ProjectFileBuilders.packageJson(
                    name = name,
                    workspaces = workspaceField,
                    isPrivate = true,
                    packageManager = ctx.packageManagerSpec(),
                    engines = mapOf("node" to TemplateVersions.NODE_ENGINE),
                    scripts =
                        linkedMapOf(
                            "dev" to devCommand,
                            "dev:server" to perWorkspaceCmd(pm, "server", "dev"),
                            "dev:client" to perWorkspaceCmd(pm, "client", "dev"),
                            "build:client" to perWorkspaceCmd(pm, "client", "build"),
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                    devDependencies = linkedMapOf("concurrently" to TemplateVersions.CONCURRENTLY),
                ),
            )
            if (pm == PackageManager.PNPM) {
                put("pnpm-workspace.yaml", "packages:\n  - \"packages/*\"\n")
            }
            // shared
            put("packages/shared/rescript.json", ProjectFileBuilders.rescriptJson(name = "@$name/shared"))
            put(
                "packages/shared/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$name/shared",
                    type = "module",
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                        ),
                ),
            )
            put("packages/shared/src/Types.res", "type user = {\n  id: string,\n  name: string,\n}")
            // server
            put(
                "packages/server/rescript.json",
                ProjectFileBuilders.rescriptJson(
                    name = "@$name/server",
                    bsDependencies = listOf("@rescript/core", "@$name/shared"),
                ),
            )
            put(
                "packages/server/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$name/server",
                    type = "module",
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "@$name/shared" to workspaceDep(pm),
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
            )
            put("packages/server/src/Hono.res", ProjectFileBuilders.honoBindings())
            put("packages/server/src/HonoNodeServer.res", ProjectFileBuilders.honoNodeServerBindings())
            put(
                "packages/server/src/Server.res",
                "let app = Hono.createApp()\n\napp->Hono.get(\"/api/hello\", ctx => {\n" +
                    "  ctx->Hono.json({\"message\": \"Hello from server!\"})\n})\n\n" +
                    "HonoNodeServer.serve(app, {port: 3000})\nConsole.log(\"Server running on http://localhost:3000\")",
            )
            // client (Vite+)
            put(
                "packages/client/rescript.json",
                ProjectFileBuilders.rescriptJson(
                    name = "@$name/client",
                    bsDependencies = listOf("@rescript/core", "@rescript/react", "@$name/shared"),
                    includeJsx = true,
                ),
            )
            put(
                "packages/client/package.json",
                ProjectFileBuilders.packageJson(
                    name = "@$name/client",
                    type = "module",
                    isPrivate = true,
                    dependencies =
                        linkedMapOf(
                            "rescript" to TemplateVersions.RESCRIPT,
                            "@rescript/core" to TemplateVersions.RESCRIPT_CORE,
                            "@rescript/react" to TemplateVersions.RESCRIPT_REACT,
                            "@$name/shared" to workspaceDep(pm),
                            "react" to TemplateVersions.REACT,
                            "react-dom" to TemplateVersions.REACT_DOM,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                            "vite" to TemplateVersions.VITE_PLUS,
                            "vite-plus" to TemplateVersions.VITE_PLUS,
                            "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "vp dev",
                            "build" to "vp build",
                            "preview" to "vp preview",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            )
            put("packages/client/index.html", clientIndexHtml(name))
            put(
                "packages/client/vite.config.mjs",
                ProjectFileBuilders.viteConfigWithProxy(
                    imports =
                        listOf(
                            """import { defineConfig } from "vite-plus";""",
                            """import react from "@vitejs/plugin-react";""",
                        ),
                ),
            )
            put(
                "packages/client/src/App.res",
                "@react.component\nlet make = () => {\n  <div>\n" +
                    "    {React.string(\"Hello, ReScript Monorepo!\")}\n  </div>\n}",
            )
            put(
                "packages/client/src/Main.res",
                "switch ReactDOM.querySelector(\"#root\") {\n" +
                    "| Some(rootEl) =>\n" +
                    "  ReactDOM.Client.Root.render(ReactDOM.Client.createRoot(rootEl), <App />)\n" +
                    "| None => Console.error(\"Could not find root element\")\n}",
            )
            put(
                "README.md",
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A full-stack monorepo with a Hono server and a Vite+/React client written in ReScript.",
                    scripts =
                        listOf(
                            "dev" to "Run server and client concurrently",
                            "dev:server" to "Run only the Hono server",
                            "dev:client" to "Run only the Vite+ client",
                            "build:client" to "Build the client for production",
                        ),
                    extraSections =
                        listOf(
                            "Workspaces" to workspacesNote(pm),
                            "About Vite+" to vitePlusNote(),
                        ),
                ),
            )
            put(".gitignore", CommonFiles.gitignore(extra = listOf("dist/", ".vite/", "packages/*/dist/")))
            put(".editorconfig", CommonFiles.editorconfig())
            put(".github/workflows/ci.yml", CommonFiles.ciWorkflow(ctx, hasBuild = false))
        }
    }

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun devScript(pm: PackageManager): String {
        val client = perWorkspaceCmd(pm, "client", "dev")
        val server = perWorkspaceCmd(pm, "server", "dev")
        return "concurrently \\\"$server\\\" \\\"$client\\\""
    }

    /**
     * Returns the dependency-version string used to refer to a sibling workspace package.
     *
     * pnpm and Yarn 3+ recognize `workspace:*`; npm interprets the same dependency as a
     * regular semver range, so we fall back to `*` for npm to keep `npm install` happy.
     */
    private fun workspaceDep(pm: PackageManager): String =
        when (pm) {
            PackageManager.PNPM -> "workspace:*"
            PackageManager.YARN -> "workspace:*"
            PackageManager.NPM -> "*"
        }

    private fun perWorkspaceCmd(
        pm: PackageManager,
        pkg: String,
        script: String,
    ): String =
        when (pm) {
            PackageManager.PNPM -> "pnpm --filter ./packages/$pkg $script"
            PackageManager.YARN -> "yarn workspace ./packages/$pkg run $script"
            PackageManager.NPM -> "npm --workspace packages/$pkg run $script"
        }

    private fun clientIndexHtml(projectName: String): String =
        buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"en\">")
            appendLine("  <head>")
            appendLine("    <meta charset=\"UTF-8\" />")
            appendLine("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
            appendLine("    <title>$projectName</title>")
            appendLine("  </head>")
            appendLine("  <body>")
            appendLine("    <div id=\"root\"></div>")
            appendLine("    <script type=\"module\" src=\"/src/Main.res.mjs\"></script>")
            appendLine("  </body>")
            append("</html>")
        }

    private fun workspacesNote(pm: PackageManager): String =
        when (pm) {
            PackageManager.PNPM -> "This project uses pnpm workspaces (see `pnpm-workspace.yaml`)."
            PackageManager.YARN -> "This project uses Yarn workspaces (see the `workspaces` field in `package.json`)."
            PackageManager.NPM -> "This project uses npm workspaces (see the `workspaces` field in `package.json`)."
        }

    private fun vitePlusNote(): String =
        """
        The client uses [Vite+](https://vite.plus) (`vite-plus`) for the build/test toolchain.
        Vite+ is **pre-1.0** — replace `vite-plus` with `vite` in `packages/client/vite.config.mjs`
        and update its scripts to fall back to classic Vite.
        """.trimIndent()
}
