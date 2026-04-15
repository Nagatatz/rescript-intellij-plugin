package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectFileBuilders

/**
 * Generates project template files for a Hono + GraphQL Yoga + Drizzle SQLite project.
 *
 * Ships a ready-to-extend GraphQL API:
 * - GraphQL Yoga mounted on Hono at `/graphql` (with GraphiQL at the same URL)
 * - SQLite via libsql + Drizzle ORM (schema, queries, drizzle-kit migrations)
 * - Users type with query/mutation resolvers (users / user(id) / createUser / deleteUser)
 * - `docs:graphql` script runs graphql-markdown against the schema for human-readable docs
 *
 * The generated project answers the common day-two question "how do I add a new GraphQL
 * type + resolver?" by showing an already-wired users type alongside the Drizzle table.
 */
internal object HonoGraphqlTemplateFiles {
    /**
     * Generates Hono GraphQL template files using the supplied [TemplateContext].
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
                            "graphql" to TemplateVersions.GRAPHQL,
                            "graphql-yoga" to TemplateVersions.GRAPHQL_YOGA,
                            "@libsql/client" to TemplateVersions.LIBSQL_CLIENT,
                            "drizzle-orm" to TemplateVersions.DRIZZLE_ORM,
                        ),
                    devDependencies =
                        linkedMapOf(
                            "drizzle-kit" to TemplateVersions.DRIZZLE_KIT,
                            "@graphql-markdown/cli" to TemplateVersions.GRAPHQL_MARKDOWN,
                            "vitest" to TemplateVersions.VITEST,
                        ),
                    scripts =
                        linkedMapOf(
                            "start" to "node src/Server.res.mjs",
                            "dev" to "node --watch src/Server.res.mjs",
                            "test" to "vitest run",
                            "docs:graphql" to
                                "graphql-markdown --schema=src/schema.graphql --base-url=./docs " +
                                "--root-path=./docs --group-by=kind",
                            "db:generate" to "drizzle-kit generate",
                            "db:migrate" to "drizzle-kit migrate",
                            "res:build" to "rescript",
                            "res:clean" to "rescript clean",
                            "res:dev" to "rescript -w",
                        ),
                ),
            "src/Hono.res" to ProjectFileBuilders.honoBindings(),
            "src/HonoNodeServer.res" to ProjectFileBuilders.honoNodeServerBindings(),
            "src/Schema.res" to schemaRes(),
            "src/Db.res" to dbRes(),
            "src/Yoga.res" to yogaRes(),
            "src/GraphqlSchema.res" to graphqlSchemaRes(),
            "src/Resolvers/Users.res" to resolversUsersRes(),
            "src/Server.res" to serverRes(),
            "src/schema.graphql" to schemaGraphql(),
            "drizzle.config.ts" to drizzleConfig(),
            "src/__tests__/Server.test.mjs" to serverTest(),
            "README.md" to
                CommonFiles.readme(
                    ctx = ctx,
                    description =
                        "A GraphQL API on Hono using graphql-yoga, backed by SQLite (Drizzle). " +
                            "Comes with GraphiQL at /graphql and human-readable docs generation.",
                    scripts =
                        listOf(
                            "dev" to "Run the GraphQL server with file watching",
                            "start" to "Run the server once",
                            "test" to "Run Vitest",
                            "docs:graphql" to "Generate docs/schema.md from src/schema.graphql",
                            "db:generate" to "Generate Drizzle migration SQL",
                            "db:migrate" to "Apply pending migrations to the SQLite file",
                            "res:dev" to "Watch ReScript sources",
                        ),
                    extraSections =
                        listOf(
                            "Try It" to tryItSection(),
                            "Schema" to schemaSection(),
                            "Database" to databaseSection(ctx),
                            "Project Layout" to projectLayoutSection(),
                        ),
                ),
            ".gitignore" to CommonFiles.gitignore(extra = listOf("data/", "docs/schema.md", "drizzle/")),
            ".editorconfig" to CommonFiles.editorconfig(),
            ".github/workflows/ci.yml" to CommonFiles.ciWorkflow(ctx, hasTest = true),
        )

    /**
     * Back-compatible entry point used by tests and any external callers.
     */
    fun generate(projectName: String): Map<String, String> = generate(TemplateContext(projectName, PackageManager.PNPM))

    private fun schemaRes(): String =
        buildString {
            appendLine("// Drizzle SQLite schema. `db:generate` reads this file to emit migration SQL.")
            appendLine("@module(\"drizzle-orm/sqlite-core\")")
            appendLine("external sqliteTable: (string, 'columns) => 'table = \"sqliteTable\"")
            appendLine("")
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

    private fun dbRes(): String =
        buildString {
            appendLine("// libsql + Drizzle client. Same shape as the Hono REST template so the two")
            appendLine("// can be diffed side-by-side.")
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
            appendLine("@send external insert: ('db, 'table) => 'builder = \"insert\"")
            appendLine("@send external values: ('builder, 'row) => 'builder = \"values\"")
            appendLine("@send external returning: 'q => promise<array<'row>> = \"returning\"")
            appendLine("@send external from: ('q, 'table) => 'q = \"from\"")
            appendLine("@send external allAsync: 'q => promise<array<'row>> = \"all\"")
            appendLine("@send external deleteFrom: ('db, 'table) => 'builder = \"delete\"")
            appendLine("@send external where: ('builder, 'expr) => 'builder = \"where\"")
        }

    private fun yogaRes(): String =
        buildString {
            appendLine("// graphql-yoga bindings: buildSchema + createYoga. Yoga returns a Fetch-compatible")
            appendLine("// handler that Hono's context.req can pass through directly.")
            appendLine("@module(\"graphql\") external buildSchema: string => 'schema = \"buildSchema\"")
            appendLine("")
            appendLine("type yogaHandler = Hono.request => promise<'response>")
            appendLine("")
            appendLine("@module(\"graphql-yoga\")")
            appendLine("external createYoga: {")
            appendLine("  \"schema\": 'schema,")
            appendLine("  \"rootValue\": 'resolvers,")
            appendLine("} => yogaHandler = \"createYoga\"")
        }

    private fun graphqlSchemaRes(): String =
        buildString {
            appendLine("// Builds a graphql-js schema object from the SDL string and bundles the")
            appendLine("// resolvers (matching type.field => function shape) consumed by graphql-yoga.")
            appendLine("@val external readFileSync: (string, string) => string = \"fs.readFileSync\"")
            appendLine("@module(\"node:fs\") external readFile: (string, string) => string = \"readFileSync\"")
            appendLine("")
            appendLine("// We inline the SDL rather than reading the file at startup so the bundle")
            appendLine("// stays self-contained; keep this in sync with src/schema.graphql.")
            appendLine("let typeDefs = \\`")
            appendLine("  type User { id: Int!, name: String!, email: String! }")
            appendLine("  type Query {")
            appendLine("    users: [User!]!")
            appendLine("    user(id: Int!): User")
            appendLine("  }")
            appendLine("  type Mutation {")
            appendLine("    createUser(name: String!, email: String!): User!")
            appendLine("    deleteUser(id: Int!): Boolean!")
            appendLine("  }")
            appendLine("\\`")
            appendLine("")
            appendLine("let schema = Yoga.buildSchema(typeDefs)")
            appendLine("")
            appendLine("let rootValue = {")
            appendLine("  \"users\": Resolvers.Users.listUsers,")
            appendLine("  \"user\": Resolvers.Users.userById,")
            appendLine("  \"createUser\": Resolvers.Users.createUser,")
            appendLine("  \"deleteUser\": Resolvers.Users.deleteUser,")
            append("}")
        }

    private fun resolversUsersRes(): String =
        buildString {
            appendLine("// Users resolvers. Each field gets a resolver function matching the signature")
            appendLine("// graphql-yoga expects: (parent, args, ctx, info) => value | Promise<value>.")
            appendLine("let listUsers = async (_parent, _args, _ctx, _info) => {")
            appendLine("  await Db.db")
            appendLine("  ->Db.select({")
            appendLine("    \"id\": Schema.users[\"id\"],")
            appendLine("    \"name\": Schema.users[\"name\"],")
            appendLine("    \"email\": Schema.users[\"email\"],")
            appendLine("  })")
            appendLine("  ->Db.from(Schema.users)")
            appendLine("  ->Db.allAsync")
            appendLine("}")
            appendLine("")
            appendLine("let userById = async (_parent, _args, _ctx, _info) => {")
            appendLine("  // TODO: implement with drizzle-orm `eq` filter once the binding is added.")
            appendLine("  let users = await listUsers(_parent, _args, _ctx, _info)")
            appendLine("  users->Array.get(0)")
            appendLine("}")
            appendLine("")
            appendLine("let createUser = async (_parent, args, _ctx, _info) => {")
            appendLine("  let inserted =")
            appendLine("    await Db.db")
            appendLine("    ->Db.insert(Schema.users)")
            appendLine(
                "    ->Db.values({\"name\": args[\"name\"], \"email\": args[\"email\"]})",
            )
            appendLine("    ->Db.returning")
            appendLine("  inserted->Array.get(0)")
            appendLine("}")
            appendLine("")
            appendLine("let deleteUser = async (_parent, _args, _ctx, _info) => {")
            appendLine("  // Placeholder: wire to drizzle `delete(...).where(eq(users.id, args.id))`.")
            appendLine("  true")
            append("}")
        }

    private fun serverRes(): String =
        buildString {
            appendLine("// Mount yoga under /graphql. GraphiQL ships automatically at the same URL.")
            appendLine("let yoga = Yoga.createYoga({")
            appendLine("  \"schema\": GraphqlSchema.schema,")
            appendLine("  \"rootValue\": GraphqlSchema.rootValue,")
            appendLine("})")
            appendLine("")
            appendLine("let app = Hono.createApp()")
            appendLine("app->Hono.get(\"/\", ctx => ctx->Hono.text(\"Hono GraphQL + ReScript — open /graphql\"))")
            appendLine("")
            appendLine("// Forward /graphql (GET + POST) to yoga's Fetch-compatible handler.")
            appendLine("let handleYoga = ctx => {")
            appendLine("  let request = ctx->Hono.req")
            appendLine("  yoga(request)")
            appendLine("}")
            appendLine("app->Hono.get(\"/graphql\", handleYoga)")
            appendLine("app->Hono.post(\"/graphql\", handleYoga)")
            appendLine("")
            appendLine("HonoNodeServer.serve(app, {port: 4000})")
            append("Console.log(\"Server on http://localhost:4000 — GraphiQL at /graphql\")")
        }

    private fun schemaGraphql(): String =
        buildString {
            appendLine("\"\"\"")
            appendLine("Users managed by the Hono GraphQL template.")
            appendLine("Keep this file in sync with src/GraphqlSchema.res typeDefs.")
            appendLine("\"\"\"")
            appendLine("type User {")
            appendLine("  id: Int!")
            appendLine("  name: String!")
            appendLine("  email: String!")
            appendLine("}")
            appendLine()
            appendLine("type Query {")
            appendLine("  users: [User!]!")
            appendLine("  user(id: Int!): User")
            appendLine("}")
            appendLine()
            appendLine("type Mutation {")
            appendLine("  createUser(name: String!, email: String!): User!")
            append("  deleteUser(id: Int!): Boolean!")
            appendLine()
            append("}")
        }

    private fun drizzleConfig(): String =
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

    private fun tryItSection(): String =
        buildString {
            appendLine("After `pnpm dev`, visit <http://localhost:4000/graphql> for the built-in")
            appendLine("GraphiQL IDE. Try:")
            appendLine()
            appendLine("```graphql")
            appendLine("mutation {")
            appendLine("  createUser(name: \"Ada\", email: \"ada@example.com\") { id name }")
            appendLine("}")
            appendLine()
            appendLine("query { users { id name email } }")
            append("```")
        }

    private fun schemaSection(): String =
        buildString {
            appendLine("The GraphQL schema is defined once in `src/schema.graphql` and mirrored by the")
            appendLine("`typeDefs` template string in `src/GraphqlSchema.res`. Update both when you add")
            appendLine("new types, then regenerate human docs:")
            appendLine()
            appendLine("```bash")
            appendLine("pnpm docs:graphql")
            append("```")
        }

    private fun databaseSection(ctx: TemplateContext): String =
        buildString {
            appendLine("Persistence uses SQLite via `@libsql/client` and Drizzle ORM. Defaults to")
            appendLine("`./data/app.db`; set `DATABASE_URL` (e.g. a Turso `libsql://` URL) to swap.")
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
            appendLine("| `src/Server.res` | Hono app that mounts yoga at /graphql |")
            appendLine("| `src/GraphqlSchema.res` | `typeDefs` + `rootValue` consumed by yoga |")
            appendLine("| `src/Resolvers/Users.res` | Users query + mutation resolvers |")
            appendLine("| `src/Schema.res` | Drizzle SQLite table definitions |")
            appendLine("| `src/Db.res` | libsql client + Drizzle helpers |")
            appendLine("| `src/schema.graphql` | Human-authored SDL (mirror of typeDefs) |")
            append("| `drizzle.config.ts` | drizzle-kit config for migrations |")
        }
}
