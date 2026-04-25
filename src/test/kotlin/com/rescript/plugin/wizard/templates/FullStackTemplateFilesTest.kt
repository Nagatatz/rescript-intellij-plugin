package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ApiStrategy
import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FullStackTemplateFilesTest {
    private val ctx = TemplateContext("fs-app", PackageManager.PNPM)
    private val suryCtx = TemplateContext("fs-app", PackageManager.PNPM, ValidationLibrary.SURY)
    private val graphqlCtx =
        TemplateContext("fs-app", PackageManager.PNPM, ValidationLibrary.ZOD, ApiStrategy.GRAPHQL)
    private val graphqlSuryCtx =
        TemplateContext("fs-app", PackageManager.PNPM, ValidationLibrary.SURY, ApiStrategy.GRAPHQL)

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
        assertTrue(files.containsKey("src/shared/Shared.res"))
        assertTrue(files.containsKey("src/server/ServerMain.res"))
        assertTrue(files.containsKey("src/server/Server.res"))
        assertTrue(files.containsKey("src/server/Routes.res"))
        assertTrue(files.containsKey("src/server/Schema.res"))
        assertTrue(files.containsKey("src/server/Db.res"))
        assertTrue(files.containsKey("src/client/App.res"))
        assertTrue(files.containsKey("src/client/ClientMain.res"))
        assertTrue(files.containsKey("src/client/ApiClient.res"))
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
    fun `dev script also boots rescript -w so res file edits propagate`() {
        // Without npm:res:dev in the concurrent group, `npm run dev` would
        // serve stale .res.mjs forever — a real footgun. Guard against
        // accidental removal.
        val pkg = FullStackTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("npm:res:dev"))
        assertTrue(pkg.contains("\"res:dev\": \"rescript -w\""))
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
    fun `users route uses drizzle and validates via Validation`() {
        val users = FullStackTemplateFiles.generate(ctx)["src/server/Routes.res"]!!
        assertTrue(users.contains("Db.select"))
        assertTrue(users.contains("Db.insert"))
        assertTrue(users.contains("Validation.parseCreateUserReq"))
        assertTrue(users.contains("Hono.status(400)"))
    }

    @Test
    fun `zod variant ships zod Validation module and zod dependency`() {
        val files = FullStackTemplateFiles.generate(ctx)
        val validation = files["src/server/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseCreateUserReq"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury Validation module and sury dependency`() {
        val files = FullStackTemplateFiles.generate(suryCtx)
        val validation = files["src/server/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
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
        assertTrue(
            files["src/server/__tests__/Server.test.mjs"]!!.contains("app.request(\"/api/health\")"),
        )
        assertTrue(files["src/client/__tests__/Api.test.mjs"]!!.contains("import(\"../ApiClient.res.mjs\")"))
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

    // ---- API strategy: REST ----

    @Test
    fun `rest variant ships REST server routes and fetch-based client`() {
        val files = FullStackTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/server/Routes.res"))
        assertTrue(files.containsKey("src/client/ApiClient.res"))
        assertFalse(files.containsKey("src/server/Yoga.res"))
        assertFalse(files.containsKey("src/server/GraphqlSchema.res"))
        assertFalse(files.containsKey("src/client/RelayEnvironment.res"))
        assertFalse(files.containsKey("relay.config.js"))
    }

    // ---- API strategy: GRAPHQL ----

    @Test
    fun `graphql variant ships yoga server mount and rescript-relay client`() {
        val files = FullStackTemplateFiles.generate(graphqlCtx)
        assertTrue(files.containsKey("src/server/Yoga.res"))
        assertTrue(files.containsKey("src/server/GraphqlSchema.res"))
        assertTrue(files.containsKey("src/server/Resolvers.res"))
        assertTrue(files.containsKey("src/server/schema.graphql"))
        assertTrue(files.containsKey("src/client/RelayEnvironment.res"))
        assertTrue(files.containsKey("src/client/UsersListQuery.res"))
        assertTrue(files.containsKey("relay.config.js"))
        assertFalse(files.containsKey("src/server/Routes.res"))
        assertFalse(files.containsKey("src/client/ApiClient.res"))
    }

    @Test
    fun `graphql variant package json includes graphql-yoga and rescript-relay`() {
        val pkg = FullStackTemplateFiles.generate(graphqlCtx)["package.json"]!!
        assertTrue(pkg.contains("\"graphql\": \"${TemplateVersions.GRAPHQL}\""))
        assertTrue(pkg.contains("\"graphql-yoga\": \"${TemplateVersions.GRAPHQL_YOGA}\""))
        assertTrue(pkg.contains("\"rescript-relay\": \"${TemplateVersions.RESCRIPT_RELAY}\""))
        assertTrue(pkg.contains("\"relay-compiler\": \"${TemplateVersions.RELAY_COMPILER}\""))
    }

    @Test
    fun `graphql variant dev script runs relay compiler watcher alongside server and client`() {
        val pkg = FullStackTemplateFiles.generate(graphqlCtx)["package.json"]!!
        assertTrue(pkg.contains("\"relay\": \"relay-compiler\""))
        assertTrue(pkg.contains("\"relay:watch\": \"relay-compiler --watch\""))
        assertTrue(pkg.contains("npm:relay:watch"))
        // Same fix as the REST variant: ReScript watcher must be in the concurrent group.
        assertTrue(pkg.contains("npm:res:dev"))
    }

    @Test
    fun `graphql variant rescript json wires the relay ppx and bs-dependency`() {
        val rj = FullStackTemplateFiles.generate(graphqlCtx)["rescript.json"]!!
        assertTrue(rj.contains("\"rescript-relay\""))
        assertTrue(rj.contains("\"ppx-flags\""))
        assertTrue(rj.contains("rescript-relay/ppx"))
    }

    @Test
    fun `graphql variant gitignores the Relay artifact directory`() {
        val gi = FullStackTemplateFiles.generate(graphqlCtx)[".gitignore"]!!
        assertTrue(gi.contains("src/client/__generated__/"))
    }

    @Test
    fun `graphql variant README documents the GraphQL workflow`() {
        val readme = FullStackTemplateFiles.generate(graphqlCtx)["README.md"]!!
        assertTrue(readme.contains("## GraphQL"))
        assertTrue(readme.contains("relay-compiler"))
        assertTrue(readme.contains("%relay()"))
    }

    @Test
    fun `graphql variant server mounts yoga at slash graphql`() {
        val server = FullStackTemplateFiles.generate(graphqlCtx)["src/server/Server.res"]!!
        assertTrue(server.contains("Yoga.createYoga"))
        assertTrue(server.contains("/graphql"))
    }

    @Test
    fun `graphql variant resolvers use shared Db helpers`() {
        val resolvers = FullStackTemplateFiles.generate(graphqlCtx)["src/server/Resolvers.res"]!!
        assertTrue(resolvers.contains("Db.where(Db.eq(Schema.users[\"id\"], args[\"id\"]))"))
        assertTrue(resolvers.contains("Db.deleteFrom(Schema.users)"))
    }

    // ---- 4-combo: all (API × Validation) pairs generate without errors ----

    @Test
    fun `all four variant combinations emit a package json and rescript json`() {
        val combos =
            listOf(
                TemplateContext("fs-app", PackageManager.PNPM, ValidationLibrary.ZOD, ApiStrategy.REST),
                TemplateContext("fs-app", PackageManager.PNPM, ValidationLibrary.SURY, ApiStrategy.REST),
                TemplateContext("fs-app", PackageManager.PNPM, ValidationLibrary.ZOD, ApiStrategy.GRAPHQL),
                TemplateContext("fs-app", PackageManager.PNPM, ValidationLibrary.SURY, ApiStrategy.GRAPHQL),
            )
        combos.forEach { combo ->
            val files = FullStackTemplateFiles.generate(combo)
            assertTrue(
                files.containsKey("package.json"),
                "${combo.apiStrategy}+${combo.validationLibrary} should emit package.json",
            )
            assertTrue(
                files.containsKey("rescript.json"),
                "${combo.apiStrategy}+${combo.validationLibrary} should emit rescript.json",
            )
            assertTrue(
                files.containsKey("src/server/Validation.res"),
                "${combo.apiStrategy}+${combo.validationLibrary} should emit Validation.res",
            )
        }
    }

    @Test
    fun `graphql plus sury keeps sury validation while adding graphql deps`() {
        val files = FullStackTemplateFiles.generate(graphqlSuryCtx)
        val validation = files["src/server/Validation.res"]!!
        val pkg = files["package.json"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertTrue(pkg.contains("\"graphql-yoga\""))
        assertTrue(pkg.contains("\"rescript-relay\""))
    }

    @Test
    fun `shared Yoga res is byte-identical between full-stack graphql and hono-graphql`() {
        val fullStackYoga = FullStackTemplateFiles.generate(graphqlCtx)["src/server/Yoga.res"]!!
        val honoGraphqlYoga =
            HonoGraphqlTemplateFiles.generate(TemplateContext("hg", PackageManager.PNPM))["src/Yoga.res"]!!
        assertTrue(fullStackYoga == honoGraphqlYoga, "Yoga.res should be shared across templates")
    }
}
