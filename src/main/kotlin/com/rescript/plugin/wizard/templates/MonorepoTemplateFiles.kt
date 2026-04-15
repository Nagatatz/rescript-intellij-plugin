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
            put("packages/shared/src/Types.res", sharedTypesRes())
            put("packages/shared/src/Api.res", sharedApiRes())
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
                            "@libsql/client" to TemplateVersions.LIBSQL_CLIENT,
                            "drizzle-orm" to TemplateVersions.DRIZZLE_ORM,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "db:generate" to "drizzle-kit generate",
                            "db:migrate" to "drizzle-kit migrate",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            )
            put("packages/server/src/Hono.res", ProjectFileBuilders.honoBindings())
            put("packages/server/src/HonoNodeServer.res", ProjectFileBuilders.honoNodeServerBindings())
            put("packages/server/src/Schema.res", serverSchemaRes())
            put("packages/server/src/Db.res", serverDbRes())
            put("packages/server/src/Server.res", serverServerRes(name))
            put("packages/server/drizzle.config.ts", serverDrizzleConfig())
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
            put("packages/client/src/App.res", clientAppRes(name))
            put("packages/client/src/ApiClient.res", clientApiRes())
            put("packages/client/src/Main.res", clientMainRes())
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

    private fun sharedTypesRes(): String =
        buildString {
            appendLine("type user = {id: int, name: string, email: string}")
        }

    private fun sharedApiRes(): String =
        buildString {
            appendLine("// Request/response types shared by server and client.")
            appendLine("type createUserReq = {name: string, email: string}")
            appendLine("type createUserRes = {id: int, name: string, email: string}")
        }

    private fun serverSchemaRes(): String =
        buildString {
            appendLine("// Drizzle SQLite schema. Shared type lives in @<project>/shared/Types.res;")
            appendLine("// the table definition stays here because it is server-only.")
            appendLine("@module(\"drizzle-orm/sqlite-core\")")
            appendLine("external sqliteTable: (string, 'columns) => 'table = \"sqliteTable\"")
            appendLine("@module(\"drizzle-orm/sqlite-core\")")
            appendLine("external intCol: (string, 'opts) => 'col = \"integer\"")
            appendLine("@module(\"drizzle-orm/sqlite-core\")")
            appendLine("external textCol: (string, 'opts) => 'col = \"text\"")
            appendLine("")
            appendLine("let users = sqliteTable(\"users\", {")
            appendLine("  \"id\": intCol(\"id\", {\"primaryKey\": true, \"autoIncrement\": true}),")
            appendLine("  \"name\": textCol(\"name\", {\"notNull\": true}),")
            appendLine("  \"email\": textCol(\"email\", {\"notNull\": true}),")
            append("})")
        }

    private fun serverDbRes(): String =
        buildString {
            appendLine("@module(\"@libsql/client\") external createClient: 'opts => 'client = \"createClient\"")
            appendLine("@module(\"drizzle-orm/libsql\") external drizzle: 'client => 'db = \"drizzle\"")
            appendLine("")
            appendLine("@val external processEnv: Dict.t<string> = \"process.env\"")
            appendLine("")
            appendLine("let dbUrl =")
            appendLine("  processEnv")
            appendLine("  ->Dict.get(\"DATABASE_URL\")")
            appendLine("  ->Option.getOr(\"file:./data/app.db\")")
            appendLine("")
            appendLine("let client = createClient({\"url\": dbUrl})")
            appendLine("let db: 'db = drizzle(client)")
            appendLine("")
            appendLine("@send external insert: ('db, 'table) => 'builder = \"insert\"")
            appendLine("@send external values: ('builder, 'row) => 'builder = \"values\"")
            appendLine("@send external returning: 'q => promise<array<'row>> = \"returning\"")
            appendLine("@send external select: ('db, 'opts) => 'query = \"select\"")
            appendLine("@send external from: ('q, 'table) => 'q = \"from\"")
            appendLine("@send external allAsync: 'q => promise<array<'row>> = \"all\"")
        }

    private fun serverServerRes(projectName: String): String =
        buildString {
            appendLine("let app = Hono.createApp()")
            appendLine("")
            appendLine("app->Hono.get(\"/api/hello\", ctx =>")
            appendLine("  ctx->Hono.json({\"message\": \"Hello from @$projectName/server!\"})")
            appendLine(")")
            appendLine("")
            appendLine("app->Hono.get(\"/api/users\", async ctx => {")
            appendLine("  let rows =")
            appendLine("    await Db.db")
            appendLine("    ->Db.select({")
            appendLine("      \"id\": Schema.users[\"id\"],")
            appendLine("      \"name\": Schema.users[\"name\"],")
            appendLine("      \"email\": Schema.users[\"email\"],")
            appendLine("    })")
            appendLine("    ->Db.from(Schema.users)")
            appendLine("    ->Db.allAsync")
            appendLine("  ctx->Hono.json(rows)")
            appendLine("})")
            appendLine("")
            appendLine("app->Hono.post(\"/api/users\", async ctx => {")
            appendLine("  let payload = await ctx->Hono.req->Hono.jsonBody")
            appendLine("  let inserted =")
            appendLine("    await Db.db")
            appendLine("    ->Db.insert(Schema.users)")
            appendLine(
                "    ->Db.values({\"name\": payload[\"name\"], \"email\": payload[\"email\"]})",
            )
            appendLine("    ->Db.returning")
            appendLine("  ctx->Hono.status(201)->Hono.json(inserted->Array.get(0))")
            appendLine("})")
            appendLine("")
            appendLine("HonoNodeServer.serve(app, {port: 3000})")
            append("Console.log(\"Server running on http://localhost:3000\")")
        }

    private fun serverDrizzleConfig(): String =
        buildString {
            appendLine("import { defineConfig } from \"drizzle-kit\";")
            appendLine("")
            appendLine("export default defineConfig({")
            appendLine("  schema: \"./src/Schema.res.mjs\",")
            appendLine("  out: \"./drizzle\",")
            appendLine("  dialect: \"sqlite\",")
            appendLine("  dbCredentials: {")
            appendLine("    url: process.env.DATABASE_URL ?? \"file:./data/app.db\",")
            appendLine("  },")
            append("});")
        }

    private fun clientAppRes(projectName: String): String =
        buildString {
            appendLine("// Form + useState + fetch to the Hono server. Demonstrates the")
            appendLine("// full client ↔ server loop with a shared Request type.")
            appendLine("@react.component")
            appendLine("let make = () => {")
            appendLine("  let (users, setUsers) = React.useState(() => [])")
            appendLine("  let (name, setName) = React.useState(() => \"\")")
            appendLine("  let (email, setEmail) = React.useState(() => \"\")")
            appendLine("")
            appendLine("  let loadUsers = async () => {")
            appendLine("    let fetched = await ApiClient.listUsers()")
            appendLine("    setUsers(_ => fetched)")
            appendLine("  }")
            appendLine("")
            appendLine("  React.useEffect0(() => {")
            appendLine("    loadUsers()->ignore")
            appendLine("    None")
            appendLine("  })")
            appendLine("")
            appendLine("  let handleSubmit = async event => {")
            appendLine("    ReactEvent.Form.preventDefault(event)")
            appendLine("    let req: Shared.Api.createUserReq = {name, email}")
            appendLine("    let _ = await ApiClient.createUser(req)")
            appendLine("    setName(_ => \"\")")
            appendLine("    setEmail(_ => \"\")")
            appendLine("    await loadUsers()")
            appendLine("  }")
            appendLine("")
            appendLine("  <main style={ReactDOM.Style.make(~padding=\"2rem\", ~fontFamily=\"sans-serif\", ())}>")
            appendLine("    <h1> {React.string(\"$projectName\")} </h1>")
            appendLine("    <form onSubmit={handleSubmit}>")
            appendLine(
                "      <input placeholder=\"name\" value={name} " +
                    "onChange={e => setName(_ => (e->ReactEvent.Form.target)[\"value\"])} />",
            )
            appendLine(
                "      <input placeholder=\"email\" value={email} " +
                    "onChange={e => setEmail(_ => (e->ReactEvent.Form.target)[\"value\"])} />",
            )
            appendLine("      <button type_=\"submit\"> {React.string(\"Add user\")} </button>")
            appendLine("    </form>")
            appendLine("    <ul>")
            appendLine("      {users")
            appendLine("        ->Array.map(u =>")
            appendLine("          <li key={u.id->Int.toString}>")
            appendLine("            {React.string(u.name ++ \" — \" ++ u.email)}")
            appendLine("          </li>")
            appendLine("        )")
            appendLine("        ->React.array}")
            appendLine("    </ul>")
            append("  </main>")
            appendLine()
            append("}")
        }

    private fun clientApiRes(): String =
        buildString {
            appendLine("// Fetch wrapper. Routes go through /api/* and are proxied by Vite+ to the Hono")
            appendLine("// server during development.")
            appendLine("type response")
            appendLine("@send external json: response => promise<'a> = \"json\"")
            appendLine("@val external fetch: (string, 'opts) => promise<response> = \"fetch\"")
            appendLine("")
            appendLine("let listUsers = async (): promise<array<Shared.Types.user>> => {")
            appendLine("  let response = await fetch(\"/api/users\", Obj.magic())")
            appendLine("  await response->json")
            appendLine("}")
            appendLine("")
            appendLine(
                "let createUser = async (payload: Shared.Api.createUserReq): " +
                    "promise<Shared.Api.createUserRes> => {",
            )
            appendLine("  let response = await fetch(\"/api/users\", {")
            appendLine("    \"method\": \"POST\",")
            appendLine("    \"headers\": {\"Content-Type\": \"application/json\"},")
            appendLine("    \"body\": JSON.stringifyAny(payload)->Option.getOr(\"{}\"),")
            appendLine("  })")
            appendLine("  await response->json")
            append("}")
        }

    private fun clientMainRes(): String =
        buildString {
            appendLine("switch ReactDOM.querySelector(\"#root\") {")
            appendLine("| Some(rootEl) =>")
            appendLine("  ReactDOM.Client.Root.render(ReactDOM.Client.createRoot(rootEl), <App />)")
            appendLine("| None => Console.error(\"Could not find root element\")")
            append("}")
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
