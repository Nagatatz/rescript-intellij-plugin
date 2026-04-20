package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HonoTemplateFilesTest {
    private val ctx = TemplateContext("svc", PackageManager.PNPM)
    private val suryCtx = TemplateContext("svc", PackageManager.PNPM, ValidationLibrary.SURY)

    @Test
    fun `package json declares hono and node-server pulled from TemplateVersions`() {
        val pkg = HonoTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"hono\": \"${TemplateVersions.HONO}\""))
        assertTrue(pkg.contains("\"@hono/node-server\": \"${TemplateVersions.HONO_NODE_SERVER}\""))
        assertTrue(pkg.contains("\"vitest\""))
    }

    @Test
    fun `template includes README, gitignore, editorconfig, CI, and a vitest sample`() {
        val files = HonoTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("src/__tests__/Server.test.mjs"))
    }

    @Test
    fun `ships Drizzle schema, libsql client, and drizzle config`() {
        val files = HonoTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Schema.res"))
        assertTrue(files.containsKey("src/Db.res"))
        assertTrue(files.containsKey("drizzle.config.ts"))
        assertTrue(files["src/Schema.res"]!!.contains("sqliteTable"))
        assertTrue(files["src/Db.res"]!!.contains("@libsql/client"))
        assertTrue(files["drizzle.config.ts"]!!.contains("dialect: \"sqlite\""))
    }

    @Test
    fun `drizzle schema does not mix in zod or sury bindings`() {
        val files = HonoTemplateFiles.generate(ctx)
        val schema = files["src/Schema.res"]!!
        assertFalse(schema.contains("@module(\"zod\")"))
        assertFalse(schema.contains("S.object"))
        assertFalse(schema.contains("createUserSchema"))
    }

    @Test
    fun `server ships CRUD routes, OpenAPI spec, and Scalar UI`() {
        val files = HonoTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Routes.res"))
        assertTrue(files.containsKey("src/Scalar.res"))
        val server = files["src/Server.res"]!!
        assertTrue(server.contains("Logger.logger"))
        assertTrue(server.contains("/openapi.json"))
        assertTrue(server.contains("/docs"))
        assertTrue(server.contains("Scalar.apiReference"))
        val routes = files["src/Routes.res"]!!
        assertTrue(routes.contains("module Users"))
        assertTrue(routes.contains("Hono.post"))
        assertTrue(routes.contains("Hono.put"))
        assertTrue(routes.contains("Hono.deleteRoute"))
    }

    @Test
    fun `routes wire Validation parseCreateUserInput with 400 response`() {
        val routes = HonoTemplateFiles.generate(ctx)["src/Routes.res"]!!
        assertTrue(routes.contains("Validation.parseCreateUserInput"))
        assertTrue(routes.contains("Hono.status(400)"))
        assertTrue(routes.contains("{\"error\": msg}"))
    }

    @Test
    fun `zod variant ships zod Validation module and ZodOpenapi bindings`() {
        val files = HonoTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Validation.res"))
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseCreateUserInput"))
        assertTrue(files.containsKey("src/ZodOpenapi.res"))
        assertTrue(files["src/ZodOpenapi.res"]!!.contains("@hono/zod-openapi"))
    }

    @Test
    fun `sury variant ships sury Validation module and drops zod-only scaffolding`() {
        val files = HonoTemplateFiles.generate(suryCtx)
        assertTrue(files.containsKey("src/Validation.res"))
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        assertTrue(validation.contains("parseCreateUserInput"))
        assertFalse(validation.contains("@module(\"zod\")"))
        assertFalse(files.containsKey("src/ZodOpenapi.res"))
    }

    @Test
    fun `package json declares SQLite, zod, and drizzle-kit for zod variant`() {
        val pkg = HonoTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"@libsql/client\""))
        assertTrue(pkg.contains("\"drizzle-orm\""))
        assertTrue(pkg.contains("\"drizzle-kit\""))
        assertTrue(pkg.contains("\"@hono/zod-openapi\""))
        assertTrue(pkg.contains("\"@scalar/hono-api-reference\""))
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
        assertTrue(pkg.contains("\"db:generate\""))
        assertTrue(pkg.contains("\"db:migrate\""))
    }

    @Test
    fun `package json swaps zod for sury and drops hono-zod-openapi in sury variant`() {
        val pkg = HonoTemplateFiles.generate(suryCtx)["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
        assertFalse(pkg.contains("\"@hono/zod-openapi\""))
        assertTrue(pkg.contains("\"@scalar/hono-api-reference\""))
    }

    @Test
    fun `README describes selected validation library`() {
        val zodReadme = HonoTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(zodReadme.contains("zod validation"))
        val suryReadme = HonoTemplateFiles.generate(suryCtx)["README.md"]!!
        assertTrue(suryReadme.contains("sury validation"))
    }

    @Test
    fun `README documents API, database, and OpenAPI`() {
        val readme = HonoTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## API"))
        assertTrue(readme.contains("## Database"))
        assertTrue(readme.contains("## OpenAPI Docs"))
        assertTrue(readme.contains("Scalar UI"))
        assertTrue(readme.contains("Drizzle"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = HonoTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("svc"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = HonoTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `ships env example documenting DATABASE_URL`() {
        val files = HonoTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".env.example"))
        assertTrue(files[".env.example"]!!.contains("DATABASE_URL"))
    }

    @Test
    fun `wires a global onError handler returning JSON 500`() {
        val server = HonoTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Hono.onError"))
        assertTrue(server.contains("Internal Server Error"))
        assertTrue(server.contains("Hono.status(500)"))
    }

    @Test
    fun `server smoke test uses app request harness against DB-free route`() {
        val files = HonoTemplateFiles.generate(ctx)
        val server = files["src/__tests__/Server.test.mjs"]!!
        assertTrue(server.contains("import { app } from"))
        assertTrue(server.contains("app.request(\"/health\")"))
    }
}
