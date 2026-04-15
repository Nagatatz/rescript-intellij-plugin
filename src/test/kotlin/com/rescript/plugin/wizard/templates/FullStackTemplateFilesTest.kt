package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FullStackTemplateFilesTest {
    private val ctx = TemplateContext("fs-app", PackageManager.PNPM)

    @Test
    fun `package json bundles server and client deps in one place`() {
        val pkg = FullStackTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"hono\""))
        assertTrue(pkg.contains("\"react\""))
        assertTrue(pkg.contains("\"react-dom\""))
        assertTrue(pkg.contains("\"@rescript/react\""))
        assertTrue(pkg.contains("\"@libsql/client\""))
        assertTrue(pkg.contains("\"drizzle-orm\""))
        assertTrue(pkg.contains("\"drizzle-kit\""))
        assertTrue(pkg.contains("\"concurrently\""))
    }

    @Test
    fun `rescript json declares three source roots`() {
        val rj = FullStackTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("src/shared"))
        assertTrue(rj.contains("src/server"))
        assertTrue(rj.contains("src/client"))
    }

    @Test
    fun `template ships shared, server, and client entry files`() {
        val files = FullStackTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/shared/Api.res"))
        assertTrue(files.containsKey("src/shared/Types.res"))
        assertTrue(files.containsKey("src/server/Main.res"))
        assertTrue(files.containsKey("src/server/Server.res"))
        assertTrue(files.containsKey("src/server/Routes/Users.res"))
        assertTrue(files.containsKey("src/server/Schema.res"))
        assertTrue(files.containsKey("src/server/Db.res"))
        assertTrue(files.containsKey("src/client/App.res"))
        assertTrue(files.containsKey("src/client/Main.res"))
        assertTrue(files.containsKey("src/client/Api.res"))
    }

    @Test
    fun `top-level ships vite config, drizzle config, and index html`() {
        val files = FullStackTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("vite.config.mjs"))
        assertTrue(files.containsKey("drizzle.config.ts"))
        assertTrue(files.containsKey("index.html"))
        assertTrue(files["vite.config.mjs"]!!.contains("from \"vite-plus\""))
        assertTrue(files["vite.config.mjs"]!!.contains("/api"))
        assertTrue(files["drizzle.config.ts"]!!.contains("dialect: \"sqlite\""))
    }

    @Test
    fun `dev script runs server and client concurrently`() {
        val pkg = FullStackTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("concurrently"))
        assertTrue(pkg.contains("\"dev:server\""))
        assertTrue(pkg.contains("\"dev:client\""))
        assertTrue(pkg.contains("\"db:generate\""))
        assertTrue(pkg.contains("\"db:migrate\""))
    }

    @Test
    fun `README documents architecture, shared types, and database`() {
        val readme = FullStackTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## Architecture"))
        assertTrue(readme.contains("## Shared Types"))
        assertTrue(readme.contains("## Database"))
        assertTrue(readme.contains("## Project Layout"))
    }

    @Test
    fun `users route uses drizzle and shared api types`() {
        val users = FullStackTemplateFiles.generate(ctx)["src/server/Routes/Users.res"]!!
        assertTrue(users.contains("Db.select"))
        assertTrue(users.contains("Db.insert"))
        assertTrue(users.contains("Shared.Api.createUserReq"))
    }

    @Test
    fun `package json declares test script and vitest devDep`() {
        val pkg = FullStackTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test\": \"vitest run\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
    }

    @Test
    fun `ships server and client smoke tests`() {
        val files = FullStackTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/server/__tests__/Server.test.mjs"))
        assertTrue(files.containsKey("src/client/__tests__/Api.test.mjs"))
        assertTrue(files["src/server/__tests__/Server.test.mjs"]!!.contains("import(\"../Server.res.mjs\")"))
        assertTrue(files["src/client/__tests__/Api.test.mjs"]!!.contains("import(\"../Api.res.mjs\")"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = FullStackTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("fs-app"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = FullStackTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `ships env example documenting DATABASE_URL`() {
        val files = FullStackTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".env.example"))
        assertTrue(files[".env.example"]!!.contains("DATABASE_URL"))
    }

    @Test
    fun `wires a global onError handler returning JSON 500`() {
        val server = FullStackTemplateFiles.generate(ctx)["src/server/Server.res"]!!
        assertTrue(server.contains("Hono.onError"))
        assertTrue(server.contains("Internal Server Error"))
        assertTrue(server.contains("Hono.status(500)"))
    }
}
