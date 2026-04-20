package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HonoGraphqlTemplateFilesTest {
    private val ctx = TemplateContext("graphql-svc", PackageManager.PNPM)

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
}
