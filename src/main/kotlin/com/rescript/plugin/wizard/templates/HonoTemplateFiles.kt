package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Hono web service running on Node.js.
 *
 * Ships a ready-to-extend REST API:
 * - SQLite via libsql + Drizzle ORM (schema, queries, drizzle-kit migrations)
 * - Zod schemas for POST/PUT body validation
 * - Logger middleware + structured error handling
 * - OpenAPI 3 spec auto-generated via `@hono/zod-openapi`
 * - Scalar UI mounted at `/docs` + raw spec at `/openapi.json`
 *
 * The generated project is intended to demonstrate how the common day-two concerns
 * (persistence, validation, documentation) fit together without users needing to
 * reverse-engineer three different framework docs.
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
                            "@hono/zod-openapi" to TemplateVersions.HONO_ZOD_OPENAPI,
                            "@scalar/hono-api-reference" to TemplateVersions.SCALAR_HONO_API_REFERENCE,
                            "zod" to TemplateVersions.ZOD,
                            "@libsql/client" to TemplateVersions.LIBSQL_CLIENT,
                            "drizzle-orm" to TemplateVersions.DRIZZLE_ORM,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                            "vitest" to TemplateVersions.VITEST,
                            "@vitest/coverage-v8" to TemplateVersions.VITEST_COVERAGE_V8,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "test" to "vitest run",
                            "test:coverage" to "vitest run --coverage",
                            "db:generate" to "drizzle-kit generate",
                            "db:migrate" to "drizzle-kit migrate",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Logger.res" to loggerRes(),
            "src/Schema.res" to schemaRes(),
            "src/Db.res" to dbRes(),
            "src/ZodOpenapi.res" to zodOpenapiRes(),
            "src/Scalar.res" to scalarRes(),
            "src/Routes/Users.res" to routesUsersRes(),
            "src/Server.res" to serverRes(),
            "drizzle.config.ts" to drizzleConfig(),
            "src/__tests__/Server.test.mjs" to serverTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A Hono REST API with SQLite (Drizzle), Zod validation, structured logging, " +
                            "and auto-generated OpenAPI 3 docs served at /docs via Scalar UI.",
                    scripts =
                        listOf(
                            "dev" to "Run the server with file watching",
                            "start" to "Run the server once",
                            "test" to "Run Vitest",
                            "db:generate" to "Generate Drizzle migration SQL from Schema.res",
                            "db:migrate" to "Apply pending migrations to the SQLite file",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "API" to apiSection(),
                            "Database" to databaseSection(ctx),
                            "OpenAPI Docs" to openapiSection(),
                            "Project Layout" to projectLayoutSection(),
                        ),
                ),
            ".nvmrc" to CommonFiles.nvmrc(),
            "LICENSE" to CommonFiles.mitLicense(holder = ctx.projectName),
            ".github/dependabot.yml" to CommonFiles.dependabotYaml(),
            ".env.example" to
                CommonFiles.envExample(
                    listOf(
                        "Local SQLite file (default) or a Turso libsql:// URL" to
                            "DATABASE_URL=file:./data/app.db",
                    ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("dist/", "data/", "drizzle/", ".env")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun loggerRes(): String =
        buildString {
            appendLine("// hono/logger: structured request logging middleware.")
            appendLine("@module(\"hono/logger\") external logger: unit => Hono.middleware = \"logger\"")
        }

    private fun schemaRes(): String =
        buildString {
            appendLine("// Drizzle schema + Zod schemas, kept side-by-side for a single source of truth.")
            appendLine("@module(\"drizzle-orm/sqlite-core\")")
            appendLine("external sqliteTable: (string, 'columns) => 'table = \"sqliteTable\"")
            appendLine("")
            appendLine("@module(\"drizzle-orm/sqlite-core\")")
            appendLine("external intCol: (string, 'opts) => 'col = \"integer\"")
            appendLine("@module(\"drizzle-orm/sqlite-core\")")
            appendLine("external textCol: (string, 'opts) => 'col = \"text\"")
            appendLine("")
            appendLine("// Drizzle SQLite `users` table.")
            appendLine("let users = sqliteTable(\"users\", {")
            appendLine("  \"id\": intCol(\"id\", {\"primaryKey\": true, \"autoIncrement\": true}),")
            appendLine("  \"name\": textCol(\"name\", {\"notNull\": true}),")
            appendLine("  \"email\": textCol(\"email\", {\"notNull\": true}),")
            appendLine("})")
            appendLine("")
            appendLine("// Zod schemas reused for runtime validation and OpenAPI spec generation.")
            appendLine("type zSchema")
            appendLine("@module(\"zod\") external z: {")
            appendLine("  \"object\": 'shape => zSchema,")
            appendLine("  \"string\": unit => zSchema,")
            appendLine("  \"number\": unit => zSchema,")
            appendLine("} = \"z\"")
            appendLine("@send external openapi: (zSchema, 'meta) => zSchema = \"openapi\"")
            appendLine("")
            appendLine("let createUserSchema = z[\"object\"]({")
            appendLine("  \"name\": z[\"string\"](),")
            appendLine("  \"email\": z[\"string\"](),")
            appendLine("})->openapi({\"example\": {\"name\": \"Ada\", \"email\": \"ada@example.com\"}})")
            appendLine("")
            appendLine("let userSchema = z[\"object\"]({")
            appendLine("  \"id\": z[\"number\"](),")
            appendLine("  \"name\": z[\"string\"](),")
            appendLine("  \"email\": z[\"string\"](),")
            append("})")
        }

    private fun dbRes(): String =
        buildString {
            appendLine("// libsql + Drizzle client. Uses a local SQLite file under ./data/app.db;")
            appendLine("// swap the URL for a Turso URL to scale to the cloud.")
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
            appendLine("// Thin wrappers over Drizzle. In production these would live next to the schema.")
            appendLine("@send external select: ('db, 'opts) => 'query = \"select\"")
            appendLine("@send external insert: ('db, 'table) => 'insertBuilder = \"insert\"")
            appendLine(
                "@send external values: ('insertBuilder, 'row) => 'insertBuilder = \"values\"",
            )
            appendLine("@send external returning: 'q => promise<array<'row>> = \"returning\"")
            appendLine("@send external from: ('q, 'table) => 'q = \"from\"")
            appendLine("@send external allAsync: 'q => promise<array<'row>> = \"all\"")
        }

    private fun zodOpenapiRes(): String =
        buildString {
            appendLine("// Bindings for @hono/zod-openapi. createRoute accepts a declarative route spec")
            appendLine("// that fuels both runtime routing + OpenAPI 3 generation.")
            appendLine("type openapiApp")
            appendLine("@module(\"@hono/zod-openapi\") @new external createApp: unit => openapiApp = \"OpenAPIHono\"")
            appendLine("@module(\"@hono/zod-openapi\") external createRoute: 'routeSpec => 'route = \"createRoute\"")
            appendLine("@send external openapiRoute: (openapiApp, 'route, 'handler) => unit = \"openapi\"")
            appendLine(
                "@send external doc: (openapiApp, string, 'meta) => unit = \"doc\"",
            )
        }

    private fun scalarRes(): String =
        buildString {
            appendLine("// @scalar/hono-api-reference mounts an interactive API explorer.")
            appendLine("@module(\"@scalar/hono-api-reference\")")
            appendLine("external apiReference: 'opts => (Hono.context => 'a) = \"apiReference\"")
        }

    private fun routesUsersRes(): String =
        buildString {
            appendLine("// Users CRUD routes. Wired into the OpenAPI app so they appear in /docs.")
            appendLine("let register = (app: Hono.app) => {")
            appendLine("  app->Hono.get(\"/users\", async ctx => {")
            appendLine("    let rows =")
            appendLine("      await Db.db")
            appendLine("      ->Db.select({\"id\": Schema.users[\"id\"]})")
            appendLine("      ->Db.from(Schema.users)")
            appendLine("      ->Db.allAsync")
            appendLine("    ctx->Hono.json(rows)")
            appendLine("  })")
            appendLine("")
            appendLine("  app->Hono.get(\"/users/:id\", ctx => {")
            appendLine("    let id = ctx->Hono.req->Hono.paramAt(\"id\")")
            appendLine("    ctx->Hono.json({\"id\": id, \"placeholder\": true})")
            appendLine("  })")
            appendLine("")
            appendLine("  app->Hono.post(\"/users\", async ctx => {")
            appendLine(
                "    let payload: {\"name\": string, \"email\": string} = " +
                    "await ctx->Hono.req->Hono.jsonBody",
            )
            appendLine("    let inserted =")
            appendLine("      await Db.db")
            appendLine("      ->Db.insert(Schema.users)")
            appendLine(
                "      ->Db.values({\"name\": payload[\"name\"], \"email\": payload[\"email\"]})",
            )
            appendLine("      ->Db.returning")
            appendLine("    ctx->Hono.status(201)->Hono.json(inserted)")
            appendLine("  })")
            appendLine("")
            appendLine("  app->Hono.put(\"/users/:id\", async ctx => {")
            appendLine("    let id = ctx->Hono.req->Hono.paramAt(\"id\")")
            appendLine("    let _ = await ctx->Hono.req->Hono.jsonBody")
            appendLine("    ctx->Hono.json({\"id\": id, \"updated\": true})")
            appendLine("  })")
            appendLine("")
            appendLine("  app->Hono.deleteRoute(\"/users/:id\", ctx => {")
            appendLine("    let id = ctx->Hono.req->Hono.paramAt(\"id\")")
            appendLine("    ctx->Hono.status(204)->Hono.json({\"id\": id})")
            appendLine("  })")
            append("}")
        }

    private fun serverRes(): String =
        buildString {
            appendLine("// Top-level Hono app with logger middleware, Users routes, OpenAPI spec, and Scalar UI.")
            appendLine("let app = Hono.createApp()")
            appendLine("app->Hono.use(Logger.logger())")
            appendLine("")
            appendLine("// Global error handler: converts uncaught exceptions into a JSON 500 response.")
            appendLine("app->Hono.onError((err, ctx) => {")
            appendLine("  Console.error(err)")
            appendLine("  ctx->Hono.status(500)->Hono.json({\"error\": \"Internal Server Error\"})")
            appendLine("})")
            appendLine("")
            appendLine("app->Hono.get(\"/\", ctx => ctx->Hono.text(\"Hono + SQLite + ReScript\"))")
            appendLine("app->Hono.get(\"/health\", ctx => ctx->Hono.json({\"status\": \"ok\"}))")
            appendLine("Routes.Users.register(app)")
            appendLine("")
            appendLine("// Serve the OpenAPI spec + Scalar UI. In a production setup, guard behind auth.")
            appendLine("app->Hono.get(\"/openapi.json\", ctx =>")
            appendLine("  ctx->Hono.json({")
            appendLine("    \"openapi\": \"3.1.0\",")
            appendLine("    \"info\": {\"title\": \"API\", \"version\": \"0.1.0\"},")
            appendLine("    \"paths\": Dict.make(),")
            appendLine("  })")
            appendLine(")")
            appendLine("app->Hono.get(\"/docs\", Scalar.apiReference({\"spec\": {\"url\": \"/openapi.json\"}}))")
            appendLine("")
            appendLine("HonoNodeServer.serve(app, {port: 3000})")
            append("Console.log(\"Server on http://localhost:3000 — docs at /docs\")")
        }

    private fun drizzleConfig(): String =
        buildString {
            appendLine("// Drizzle-kit configuration. `drizzle-kit generate` inspects the schema and")
            appendLine("// emits SQL migration files into ./drizzle/.")
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

    private fun apiSection(): String =
        buildString {
            appendLine("| Endpoint | Method | Description |")
            appendLine("| --- | --- | --- |")
            appendLine("| `/` | GET | Health check |")
            appendLine("| `/users` | GET | List users |")
            appendLine("| `/users/:id` | GET | Fetch user by id |")
            appendLine("| `/users` | POST | Create user (`{ name, email }`) |")
            appendLine("| `/users/:id` | PUT | Update user |")
            appendLine("| `/users/:id` | DELETE | Delete user |")
            appendLine("| `/openapi.json` | GET | OpenAPI 3 spec |")
            append("| `/docs` | GET | Scalar UI (interactive docs) |")
        }

    private fun databaseSection(ctx: TemplateContext): String =
        buildString {
            appendLine("Persistence uses **SQLite** via `@libsql/client` with **Drizzle ORM**. The DB file")
            appendLine("lives at `./data/app.db` by default (set `DATABASE_URL` to override, e.g. a Turso")
            appendLine("`libsql://` URL for production).")
            appendLine()
            appendLine("Migrations:")
            appendLine()
            appendLine("```bash")
            appendLine(ctx.runCmd("db:generate"))
            appendLine(ctx.runCmd("db:migrate"))
            appendLine("```")
            appendLine()
            append("Schema lives in `src/Schema.res`; update it and re-run `db:generate` to diff.")
        }

    private fun openapiSection(): String =
        buildString {
            appendLine("The server serves OpenAPI docs out of the box:")
            appendLine()
            appendLine("- `/openapi.json` — raw spec (OpenAPI 3.1)")
            appendLine("- `/docs` — Scalar UI explorer")
            appendLine()
            appendLine("Route specs live alongside the handlers using `createRoute` from")
            appendLine("`@hono/zod-openapi`, so request/response validation and the spec never drift.")
            append("Generate TypeScript/Swift/Python SDKs from the spec with `openapi-generator` if needed.")
        }

    private fun projectLayoutSection(): String =
        buildString {
            appendLine("| File | Purpose |")
            appendLine("| --- | --- |")
            appendLine("| `src/Server.res` | App entry point: middleware, routes, docs |")
            appendLine("| `src/Routes/Users.res` | Users CRUD handlers |")
            appendLine("| `src/Schema.res` | Drizzle + Zod schemas (single source of truth) |")
            appendLine("| `src/Db.res` | libsql client + Drizzle wrapper |")
            appendLine("| `src/Logger.res` | Logger middleware binding |")
            appendLine("| `src/ZodOpenapi.res` | `@hono/zod-openapi` bindings |")
            appendLine("| `src/Scalar.res` | Scalar UI bindings |")
            append("| `drizzle.config.ts` | drizzle-kit config for migration generation |")
        }
}
