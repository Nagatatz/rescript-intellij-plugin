package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HonoGraphqlTemplateFilesTest {
    private val ctx = TemplateContext("graphql-svc", PackageManager.PNPM)
    private val suryCtx = TemplateContext("graphql-svc", PackageManager.PNPM, ValidationLibrary.SURY)

    @Test
    fun `package json declares hono and graphql-yoga`() {
        val pkg = HonoGraphqlTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"hono\": \"${TemplateVersions.HONO}\""))
        assertTrue(pkg.contains("\"graphql-yoga\": \"${TemplateVersions.GRAPHQL_YOGA}\""))
        assertTrue(pkg.contains("\"graphql\": \"${TemplateVersions.GRAPHQL}\""))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
    }

    @Test
    fun `gitignore excludes data and generated graphql docs`() {
        val gitignore = HonoGraphqlTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains("data/"))
        assertTrue(gitignore.contains("docs/schema.md"))
    }

    @Test
    fun `ships schema, db, yoga, resolvers, and GraphQL SDL`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Schema.res"))
        assertTrue(files.containsKey("src/Db.res"))
        assertTrue(files.containsKey("src/Yoga.res"))
        assertTrue(files.containsKey("src/GraphqlSchema.res"))
        assertTrue(files.containsKey("src/Resolvers.res"))
        assertTrue(files.containsKey("src/schema.graphql"))
        assertTrue(files.containsKey("drizzle.config.ts"))
    }

    @Test
    fun `server mounts yoga at slash graphql with GET and POST`() {
        val server = HonoGraphqlTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Yoga.createYoga"))
        assertTrue(server.contains("Hono.get(\"/graphql\""))
        assertTrue(server.contains("Hono.post(\"/graphql\""))
    }

    @Test
    fun `Users resolvers expose list, byId, create, delete`() {
        val resolvers = HonoGraphqlTemplateFiles.generate(ctx)["src/Resolvers.res"]!!
        assertTrue(resolvers.contains("module Users"))
        assertTrue(resolvers.contains("listUsers"))
        assertTrue(resolvers.contains("userById"))
        assertTrue(resolvers.contains("createUser"))
        assertTrue(resolvers.contains("deleteUser"))
    }

    @Test
    fun `createUser resolver validates input via Validation module`() {
        val resolvers = HonoGraphqlTemplateFiles.generate(ctx)["src/Resolvers.res"]!!
        assertTrue(resolvers.contains("Validation.parseCreateUserInput"))
        assertTrue(resolvers.contains("failwith(msg)"))
    }

    @Test
    fun `zod variant ships zod Validation module and zod dependency`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Validation.res"))
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseCreateUserInput"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury Validation module and sury dependency`() {
        val files = HonoGraphqlTemplateFiles.generate(suryCtx)
        assertTrue(files.containsKey("src/Validation.res"))
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        assertFalse(validation.contains("@module(\"zod\")"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `schema graphql declares User type and CRUD query slash mutation`() {
        val sdl = HonoGraphqlTemplateFiles.generate(ctx)["src/schema.graphql"]!!
        assertTrue(sdl.contains("type User"))
        assertTrue(sdl.contains("type Query"))
        assertTrue(sdl.contains("type Mutation"))
        assertTrue(sdl.contains("createUser(name: String!, email: String!): User!"))
    }

    @Test
    fun `package json includes libsql, drizzle, graphql-yoga, and graphql-markdown`() {
        val pkg = HonoGraphqlTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"@libsql/client\""))
        assertTrue(pkg.contains("\"drizzle-orm\""))
        assertTrue(pkg.contains("\"drizzle-kit\""))
        assertTrue(pkg.contains("\"graphql-yoga\""))
        assertTrue(pkg.contains("\"@graphql-markdown/cli\""))
        assertTrue(pkg.contains("\"docs:graphql\""))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("graphql-svc"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = HonoGraphqlTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `ships env example documenting DATABASE_URL`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".env.example"))
        assertTrue(files[".env.example"]!!.contains("DATABASE_URL"))
    }

    @Test
    fun `wires a global onError handler returning JSON 500`() {
        val server = HonoGraphqlTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Hono.onError"))
        assertTrue(server.contains("Internal Server Error"))
        assertTrue(server.contains("Hono.status(500)"))
    }

    @Test
    fun `server smoke test uses app request harness against DB-free route`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        val server = files["src/__tests__/Server.test.mjs"]!!
        assertTrue(server.contains("import { app } from"))
        assertTrue(server.contains("app.request(\"/health\")"))
    }

    @Test
    fun `README Schema section explains schema-first vs code-first trade-off`() {
        val readme = HonoGraphqlTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("Schema-first by design"))
        // Must explicitly contrast with code-first frameworks so users can
        // make an informed choice when their project outgrows schema-first.
        assertTrue(readme.contains("code-first"))
        assertTrue(readme.contains("Pothos"))
        // Must teach the canonical "add a new type + resolver" workflow,
        // including the SDL/typeDefs sync requirement.
        assertTrue(readme.contains("Adding a new type + resolver"))
        assertTrue(readme.contains("schema.graphql"))
        assertTrue(readme.contains("typeDefs"))
    }

    @Test
    fun `README Schema section interpolates the docs-graphql command for the selected PM`() {
        val readme = HonoGraphqlTemplateFiles.generate(ctx)["README.md"]!!
        // pnpm context: the regenerate-docs step must be the actual `pnpm` command,
        // not a literal `{{cmdDocsGraphql}}` placeholder leaking through.
        assertTrue(readme.contains("pnpm docs:graphql"))
        assertFalse(readme.contains("{{cmdDocsGraphql}}"))
    }

    @Test
    fun `README Schema section shows how to test resolvers in isolation`() {
        val readme = HonoGraphqlTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("Testing resolvers in isolation"))
        // The example must use graphql/execution against the schema/rootValue
        // exported from GraphqlSchema.res rather than only the HTTP boundary.
        assertTrue(readme.contains("graphql/execution") || readme.contains("from \"graphql\""))
        assertTrue(readme.contains("GraphqlSchema.res.mjs"))
    }

    @Test
    fun `Server res defines a start function and never calls serve at top level`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        val server = files["src/Server.res"]!!
        assertTrue(server.contains("let start = () => {"))
        val topLevelLines = server.lines().filter { !it.startsWith("  ") && !it.startsWith("//") }
        assertFalse(
            topLevelLines.any { it.trimStart().startsWith("HonoNodeServer.serve") },
            "Server.res must wrap serve() so vitest can import it without binding a port",
        )
    }

    @Test
    fun `ServerMain res ships and is the dev start entry point`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/ServerMain.res"))
        assertTrue(files["src/ServerMain.res"]!!.contains("Server.start()"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"start\": \"node src/ServerMain.res.mjs\""))
        assertTrue(pkg.contains("\"dev\": \"node --watch src/ServerMain.res.mjs\""))
    }

    @Test
    fun `vitest setup pins DATABASE_URL to in-memory libsql before module load`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("vitest.config.mjs"))
        assertTrue(files.containsKey("vitest.setup.mjs"))
        assertTrue(files["vitest.config.mjs"]!!.contains("setupFiles"))
        assertTrue(files["vitest.setup.mjs"]!!.contains("DATABASE_URL"))
        assertTrue(files["vitest.setup.mjs"]!!.contains(":memory:"))
    }
}
