package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a single-package full-stack ReScript app.
 *
 * Pairs a Hono + Drizzle (SQLite) backend with a Vite+/React frontend in one
 * `package.json`. Types are shared through `src/shared/` rather than through a
 * workspace, which keeps the project simpler than [MonorepoTemplateFiles] while
 * still demonstrating the end-to-end loop (fetch → API → DB → response).
 *
 * The generated project answers the common day-two question "how do I share
 * types between server and client?" by shipping a `src/shared/Api.res` module
 * imported by both sides.
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
                            "@libsql/client" to TemplateVersions.LIBSQL_CLIENT,
                            "drizzle-orm" to TemplateVersions.DRIZZLE_ORM,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "concurrently" to TemplateVersions.CONCURRENTLY,
                            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                            "@vitejs/plugin-react" to TemplateVersions.VITEJS_PLUGIN_REACT,
                            "vite" to TemplateVersions.VITE_PLUS,
                            "vite-plus" to TemplateVersions.VITE_PLUS,
                            "@voidzero-dev/vite-plus-core" to TemplateVersions.VITE_PLUS_CORE,
                            "vitest" to TemplateVersions.VITEST,
                        ),
                    scripts =
                        linkedMapOf(
                            "dev" to "concurrently \"npm:dev:server\" \"npm:dev:client\"",
                            "dev:server" to "node --watch src/server/Main.res.mjs",
                            "dev:client" to "vp dev",
                            "build" to "vp build",
                            "preview" to "vp preview",
                            "test" to "vitest run",
                            "db:generate" to "drizzle-kit generate",
                            "db:migrate" to "drizzle-kit migrate",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "index.html" to indexHtml(ctx.projectName),
            "vite.config.mjs" to
                ProjectFileBuilders.viteConfigWithProxy(
                    imports =
                        listOf(
                            """import { defineConfig } from "vite-plus";""",
                            """import react from "@vitejs/plugin-react";""",
                        ),
                ),
            "drizzle.config.ts" to drizzleConfig(),
            "src/shared/Api.res" to sharedApiRes(),
            "src/shared/Types.res" to sharedTypesRes(),
            "src/server/Main.res" to serverMainRes(),
            "src/server/Server.res" to serverServerRes(),
            "src/server/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/server/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/server/Schema.res" to serverSchemaRes(),
            "src/server/Db.res" to serverDbRes(),
            "src/server/Routes/Users.res" to serverRoutesUsersRes(),
            "src/server/__tests__/Server.test.mjs" to serverTest(),
            "src/client/Main.res" to clientMainRes(),
            "src/client/App.res" to clientAppRes(ctx.projectName),
            "src/client/Api.res" to clientApiRes(),
            "src/client/__tests__/Api.test.mjs" to clientTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A single-package full-stack ReScript app: Hono + Drizzle (SQLite) on " +
                            "the server, Vite+/React on the client, with shared types in `src/shared/`.",
                    scripts =
                        listOf(
                            "dev" to "Run server and client together (via concurrently)",
                            "dev:server" to "Run only the Hono backend (with --watch)",
                            "dev:client" to "Run only the Vite+ client dev server",
                            "build" to "Bundle the client for production",
                            "test" to "Run Vitest (covers both server and client smoke tests)",
                            "db:generate" to "Generate Drizzle migration SQL",
                            "db:migrate" to "Apply pending migrations to the SQLite file",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Architecture" to architectureSection(),
                            "Shared Types" to sharedTypesSection(),
                            "Database" to databaseSection(ctx),
                            "Project Layout" to projectLayoutSection(),
                            "About Vite+" to vitePlusNote(),
                        ),
                ),
            ".gitignore" to
                CommonFiles.gitignore(
                    extra = listOf("data/", "dist/", ".vite/", "drizzle/"),
                ),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasBuild = false, hasTest = true),
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

    private fun clientTest(): String =
        buildString {
            // Main.res touches ReactDOM at import time. Api.res is pure fetch bindings,
            // safe to import under Node.
            appendLine("import { describe, expect, it } from \"vitest\";")
            appendLine("")
            appendLine("describe(\"client Api module\", () => {")
            appendLine("  it(\"loads without throwing\", async () => {")
            appendLine("    await expect(import(\"../Api.res.mjs\")).resolves.toBeDefined();")
            appendLine("  });")
            appendLine("});")
        }

    private fun indexHtml(projectName: String): String =
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
            appendLine("    <script type=\"module\" src=\"/src/client/Main.res.mjs\"></script>")
            appendLine("  </body>")
            append("</html>")
        }

    private fun drizzleConfig(): String =
        buildString {
            appendLine("import { defineConfig } from \"drizzle-kit\";")
            appendLine("")
            appendLine("export default defineConfig({")
            appendLine("  schema: \"./src/server/Schema.res.mjs\",")
            appendLine("  out: \"./drizzle\",")
            appendLine("  dialect: \"sqlite\",")
            appendLine("  dbCredentials: {")
            appendLine("    url: process.env.DATABASE_URL ?? \"file:./data/app.db\",")
            appendLine("  },")
            append("});")
        }

    private fun sharedTypesRes(): String =
        buildString {
            appendLine("// Domain types shared between server and client.")
            appendLine("type user = {id: int, name: string, email: string}")
        }

    private fun sharedApiRes(): String =
        buildString {
            appendLine("// Wire format for the /api/users endpoints. Keep server and client")
            appendLine("// in sync by editing just this file.")
            appendLine("type createUserReq = {name: string, email: string}")
            appendLine("type createUserRes = {id: int, name: string, email: string}")
        }

    private fun serverMainRes(): String =
        buildString {
            appendLine("// Entry point invoked by `npm run dev:server`. Delegates to Server.res so")
            appendLine("// wiring stays testable in isolation.")
            appendLine("Server.start()")
        }

    private fun serverServerRes(): String =
        buildString {
            appendLine("// Hono app wiring. Routes live under src/server/Routes/ for easy growth.")
            appendLine("let app = Hono.createApp()")
            appendLine("")
            appendLine("app->Hono.get(\"/api/health\", ctx => ctx->Hono.json({\"status\": \"ok\"}))")
            appendLine("Routes.Users.register(app)")
            appendLine("")
            appendLine("let start = () => {")
            appendLine("  HonoNodeServer.serve(app, {port: 3000})")
            appendLine("  Console.log(\"Server on http://localhost:3000 — try /api/health\")")
            append("}")
        }

    private fun serverSchemaRes(): String =
        buildString {
            appendLine("// Drizzle SQLite schema. The domain `user` type in src/shared/Types.res")
            appendLine("// mirrors this table.")
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
            appendLine("// libsql + Drizzle client. Defaults to a local SQLite file; set DATABASE_URL")
            appendLine("// (e.g. a Turso `libsql://` URL) to point elsewhere.")
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
            appendLine("@send external select: ('db, 'opts) => 'query = \"select\"")
            appendLine("@send external from: ('q, 'table) => 'q = \"from\"")
            appendLine("@send external allAsync: 'q => promise<array<'row>> = \"all\"")
            appendLine("@send external insert: ('db, 'table) => 'builder = \"insert\"")
            appendLine("@send external values: ('builder, 'row) => 'builder = \"values\"")
            append("@send external returning: 'q => promise<array<'row>> = \"returning\"")
        }

    private fun serverRoutesUsersRes(): String =
        buildString {
            appendLine("// Users CRUD wired against the Drizzle `users` table.")
            appendLine("let register = app => {")
            appendLine("  app->Hono.get(\"/api/users\", async ctx => {")
            appendLine("    let rows =")
            appendLine("      await Db.db")
            appendLine("      ->Db.select({")
            appendLine("        \"id\": Schema.users[\"id\"],")
            appendLine("        \"name\": Schema.users[\"name\"],")
            appendLine("        \"email\": Schema.users[\"email\"],")
            appendLine("      })")
            appendLine("      ->Db.from(Schema.users)")
            appendLine("      ->Db.allAsync")
            appendLine("    ctx->Hono.json(rows)")
            appendLine("  })")
            appendLine("")
            appendLine("  app->Hono.post(\"/api/users\", async ctx => {")
            appendLine("    let payload: Shared.Api.createUserReq = await ctx->Hono.req->Hono.jsonBody")
            appendLine("    let inserted =")
            appendLine("      await Db.db")
            appendLine("      ->Db.insert(Schema.users)")
            appendLine(
                "      ->Db.values({\"name\": payload.name, \"email\": payload.email})",
            )
            appendLine("      ->Db.returning")
            appendLine("    ctx->Hono.status(201)->Hono.json(inserted->Array.get(0))")
            appendLine("  })")
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

    private fun clientAppRes(projectName: String): String =
        buildString {
            appendLine("// Users list + add-user form. Uses Shared.Types.user so the server")
            appendLine("// and client cannot drift out of sync.")
            appendLine("@react.component")
            appendLine("let make = () => {")
            appendLine("  let (users, setUsers) = React.useState(() => [])")
            appendLine("  let (name, setName) = React.useState(() => \"\")")
            appendLine("  let (email, setEmail) = React.useState(() => \"\")")
            appendLine("")
            appendLine("  let refresh = async () => {")
            appendLine("    let fetched = await Api.listUsers()")
            appendLine("    setUsers(_ => fetched)")
            appendLine("  }")
            appendLine("")
            appendLine("  React.useEffect0(() => {")
            appendLine("    refresh()->ignore")
            appendLine("    None")
            appendLine("  })")
            appendLine("")
            appendLine("  let handleSubmit = async event => {")
            appendLine("    ReactEvent.Form.preventDefault(event)")
            appendLine("    let req: Shared.Api.createUserReq = {name, email}")
            appendLine("    let _ = await Api.createUser(req)")
            appendLine("    setName(_ => \"\")")
            appendLine("    setEmail(_ => \"\")")
            appendLine("    await refresh()")
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
            appendLine("// Fetch wrapper. `/api/*` is proxied by Vite+ to the Hono backend during dev")
            appendLine("// (see vite.config.mjs); in production the same server hosts both.")
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

    private fun architectureSection(): String =
        """
        This template lives in **one** `package.json`, with three source roots that ReScript
        compiles together:

        - `src/shared/` — types used by both sides (wire format, domain records)
        - `src/server/` — Hono + Drizzle (SQLite) API on port 3000
        - `src/client/` — Vite+/React UI that fetches `/api/*` through a dev proxy

        Run `npm run dev` (or `pnpm dev` / `yarn dev`) to boot both processes via
        `concurrently`. The client dev server proxies `/api/*` to the server so you
        never need to configure CORS locally.
        """.trimIndent()

    private fun sharedTypesSection(): String =
        """
        `src/shared/Types.res` and `src/shared/Api.res` are imported from both
        `src/server/` and `src/client/` as `Shared.Types` / `Shared.Api`. When you
        change a field on a request or response record, both sides fail to compile
        until they agree — that is the point.
        """.trimIndent()

    private fun databaseSection(ctx: TemplateContext): String =
        buildString {
            appendLine("Persistence uses SQLite via `@libsql/client` and Drizzle ORM.")
            appendLine("Defaults to `./data/app.db`; set `DATABASE_URL` to swap.")
            appendLine()
            appendLine("```bash")
            appendLine(ctx.runCmd("db:generate"))
            appendLine(ctx.runCmd("db:migrate"))
            append("```")
        }

    private fun projectLayoutSection(): String =
        buildString {
            appendLine("| File | Purpose |")
            appendLine("| --- | --- |")
            appendLine("| `src/shared/Types.res` | Domain types (user, etc.) |")
            appendLine("| `src/shared/Api.res` | Wire types for /api/* |")
            appendLine("| `src/server/Main.res` | Process entry point |")
            appendLine("| `src/server/Server.res` | Hono app + health route |")
            appendLine("| `src/server/Routes/Users.res` | Users CRUD handlers |")
            appendLine("| `src/server/Schema.res` | Drizzle SQLite schema |")
            appendLine("| `src/server/Db.res` | libsql client + query helpers |")
            appendLine("| `src/client/Main.res` | React root render |")
            appendLine("| `src/client/App.res` | Users form + list UI |")
            appendLine("| `src/client/Api.res` | Fetch wrapper using Shared types |")
            append("| `vite.config.mjs` | Vite+ config with /api proxy |")
        }

    private fun vitePlusNote(): String =
        """
        The client uses [Vite+](https://vite.plus) (`vite-plus`) for the build/test toolchain.
        Vite+ is **pre-1.0** — if a command breaks, replace `vite-plus` with `vite` in
        `vite.config.mjs` and swap the `vp` scripts for `vite` to fall back to classic Vite.
        """.trimIndent()
}
