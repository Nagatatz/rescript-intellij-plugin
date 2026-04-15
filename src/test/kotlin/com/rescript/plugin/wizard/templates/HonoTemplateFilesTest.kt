package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HonoTemplateFilesTest {
    private val ctx = TemplateContext("svc", PackageManager.PNPM)

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
    fun `server ships CRUD routes, OpenAPI spec, and Scalar UI`() {
        val files = HonoTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Routes/Users.res"))
        assertTrue(files.containsKey("src/ZodOpenapi.res"))
        assertTrue(files.containsKey("src/Scalar.res"))
        val server = files["src/Server.res"]!!
        assertTrue(server.contains("Logger.logger"))
        assertTrue(server.contains("/openapi.json"))
        assertTrue(server.contains("/docs"))
        assertTrue(server.contains("Scalar.apiReference"))
        val routes = files["src/Routes/Users.res"]!!
        assertTrue(routes.contains("Hono.post"))
        assertTrue(routes.contains("Hono.put"))
        assertTrue(routes.contains("Hono.deleteRoute"))
    }

    @Test
    fun `package json declares SQLite, OpenAPI, and drizzle-kit`() {
        val pkg = HonoTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"@libsql/client\""))
        assertTrue(pkg.contains("\"drizzle-orm\""))
        assertTrue(pkg.contains("\"drizzle-kit\""))
        assertTrue(pkg.contains("\"@hono/zod-openapi\""))
        assertTrue(pkg.contains("\"@scalar/hono-api-reference\""))
        assertTrue(pkg.contains("\"zod\""))
        assertTrue(pkg.contains("\"db:generate\""))
        assertTrue(pkg.contains("\"db:migrate\""))
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
}
