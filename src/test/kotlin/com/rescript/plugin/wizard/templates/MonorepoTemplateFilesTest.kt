package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.Database
import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MonorepoTemplateFilesTest {
    @Test
    fun `pnpm context emits pnpm-workspace yaml and omits workspaces field`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(files.containsKey("pnpm-workspace.yaml"))
        assertFalse(files["package.json"]!!.contains("\"workspaces\""))
    }

    @Test
    fun `npm context emits workspaces field and skips pnpm-workspace yaml`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.NPM))
        assertFalse(files.containsKey("pnpm-workspace.yaml"))
        assertTrue(files["package.json"]!!.contains("\"workspaces\""))
    }

    @Test
    fun `client uses vite-plus`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val clientPkg = files["packages/client/package.json"]!!
        val clientCfg = files["packages/client/vite.config.mjs"]!!
        assertTrue(clientPkg.contains("\"vite-plus\""))
        assertTrue(clientCfg.contains("from \"vite-plus\""))
        assertTrue(clientCfg.contains("/api"), "proxy config should remain")
    }

    @Test
    fun `dev script uses pnpm filter for pnpm context`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("pnpm --filter ./packages/server dev"))
        assertTrue(pkg.contains("pnpm --filter ./packages/client dev"))
    }

    @Test
    fun `dev script uses npm workspace prefix for npm context`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.NPM))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("npm --workspace packages/server run dev"))
    }

    @Test
    fun `pnpm context refers to shared workspace via workspace protocol`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val serverPkg = files["packages/server/package.json"]!!
        val clientPkg = files["packages/client/package.json"]!!
        assertTrue(serverPkg.contains("\"@app/shared\": \"workspace:*\""))
        assertTrue(clientPkg.contains("\"@app/shared\": \"workspace:*\""))
    }

    @Test
    fun `npm context refers to shared workspace via wildcard`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.NPM))
        val serverPkg = files["packages/server/package.json"]!!
        assertTrue(serverPkg.contains("\"@app/shared\": \"*\""))
    }

    @Test
    fun `top-level docs and CI files are included`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
    }

    @Test
    fun `shared package ships user and api request types`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(files.containsKey("packages/shared/src/Types.res"))
        assertTrue(files.containsKey("packages/shared/src/Api.res"))
        assertTrue(files["packages/shared/src/Api.res"]!!.contains("createUserReq"))
    }

    @Test
    fun `server ships Drizzle schema, db client, and drizzle config`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(files.containsKey("packages/server/src/Schema.res"))
        assertTrue(files.containsKey("packages/server/src/Db.res"))
        assertTrue(files.containsKey("packages/server/drizzle.config.ts"))
        val serverPkg = files["packages/server/package.json"]!!
        assertTrue(serverPkg.contains("\"@libsql/client\""))
        assertTrue(serverPkg.contains("\"drizzle-orm\""))
        assertTrue(serverPkg.contains("\"drizzle-kit\""))
        assertTrue(serverPkg.contains("\"db:generate\""))
    }

    @Test
    fun `server wires users CRUD through Drizzle`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val server = files["packages/server/src/Server.res"]!!
        assertTrue(server.contains("/api/users"))
        assertTrue(server.contains("Db.select"))
        assertTrue(server.contains("Db.insert"))
    }

    @Test
    fun `server validates POST body via Validation and returns 400 on Error`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val server = files["packages/server/src/Server.res"]!!
        assertTrue(server.contains("Validation.parseCreateUserReq"))
        assertTrue(server.contains("Hono.status(400)"))
    }

    @Test
    fun `zod variant ships zod Validation module and zod dependency`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val validation = files["packages/server/src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseCreateUserReq"))
        val pkg = files["packages/server/package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury Validation module and sury dependency`() {
        val files =
            MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM, ValidationLibrary.SURY))
        val validation = files["packages/server/src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        val pkg = files["packages/server/package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `client ships ApiClient using shared types`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(files.containsKey("packages/client/src/ApiClient.res"))
        val apiClient = files["packages/client/src/ApiClient.res"]!!
        assertTrue(apiClient.contains("Shared.Types.user"))
        assertTrue(apiClient.contains("Shared.Api.createUserReq"))
    }

    @Test
    fun `root test script fans out to all workspaces per PM`() {
        val pnpmFiles = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(pnpmFiles["package.json"]!!.contains("\"test\": \"pnpm -r run test\""))

        val npmFiles = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.NPM))
        assertTrue(npmFiles["package.json"]!!.contains("--workspaces run test --if-present"))

        val yarnFiles = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.YARN))
        assertTrue(yarnFiles["package.json"]!!.contains("yarn workspaces foreach -A run test"))

        val bunFiles = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.BUN))
        assertTrue(bunFiles["package.json"]!!.contains("\"test\": \"bun --filter '*' run test\""))
    }

    @Test
    fun `BUN workspaces use workspace protocol and bun --filter per-package scripts`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.BUN))
        val rootPkg = files["package.json"]!!
        assertTrue(
            rootPkg.contains("bun --filter ./packages/server dev"),
            "root dev script should drive the server via bun --filter",
        )
        assertTrue(
            rootPkg.contains("bun --filter ./packages/client dev"),
            "root dev script should drive the client via bun --filter",
        )
        assertTrue(
            rootPkg.contains("\"test:coverage\": \"bun --filter '*' run test:coverage\""),
        )
        val clientPkg = files["packages/client/package.json"]!!
        assertTrue(
            clientPkg.contains("workspace:*"),
            "workspace deps should use the workspace: protocol under BUN",
        )
    }

    @Test
    fun `BUN workspaces note directs users to the package_json workspaces field`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.BUN))
        val readme = files["README.md"]!!
        assertTrue(readme.contains("Bun workspaces"))
        assertTrue(readme.contains("`workspaces` field in `package.json`"))
    }

    @Test
    fun `server and client packages declare test scripts and smoke tests`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val serverPkg = files["packages/server/package.json"]!!
        val clientPkg = files["packages/client/package.json"]!!
        assertTrue(serverPkg.contains("\"test\": \"vitest run\""))
        assertTrue(serverPkg.contains("\"vitest\""))
        assertTrue(clientPkg.contains("\"test\": \"vp test\""))
        assertTrue(clientPkg.contains("\"vitest\""))
        assertTrue(files.containsKey("packages/server/src/__tests__/Server.test.mjs"))
        assertTrue(files.containsKey("packages/client/src/__tests__/ApiClient.test.mjs"))
    }

    @Test
    fun `server package ships env example documenting DATABASE_URL`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(files.containsKey("packages/server/.env.example"))
        assertTrue(files["packages/server/.env.example"]!!.contains("DATABASE_URL"))
    }

    @Test
    fun `wires a global onError handler returning JSON 500`() {
        val server =
            MonorepoTemplateFiles.generate(
                TemplateContext("app", PackageManager.PNPM),
            )["packages/server/src/Server.res"]!!
        assertTrue(server.contains("Hono.onError"))
        assertTrue(server.contains("Internal Server Error"))
        assertTrue(server.contains("Hono.status(500)"))
    }

    @Test
    fun `server smoke test uses app request harness against DB-free route`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val server = files["packages/server/src/__tests__/Server.test.mjs"]!!
        assertTrue(server.contains("import { app } from"))
        assertTrue(server.contains("app.request(\"/api/hello\")"))
    }

    @Test
    fun `README documents the Database section with workspace-filtered drizzle commands`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val readme = files["README.md"]!!
        assertTrue(readme.contains("## Database"))
        assertTrue(readme.contains("DATABASE_URL"))
        assertTrue(readme.contains("Turso"))
        assertTrue(readme.contains("packages/server/src/Schema.res"))
        // pnpm filter resolves to the per-workspace drizzle-kit invocation
        assertTrue(readme.contains("pnpm --filter ./packages/server db:generate"))
        assertTrue(readme.contains("pnpm --filter ./packages/server db:migrate"))
    }

    @Test
    fun `README database commands honor selected package manager (npm)`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.NPM))
        val readme = files["README.md"]!!
        assertTrue(readme.contains("npm --workspace packages/server run db:generate"))
        assertTrue(readme.contains("npm --workspace packages/server run db:migrate"))
    }

    @Test
    fun `server workspace dev script boots rescript watcher alongside node --watch`() {
        // Without rescript -w in the concurrent group, edits to .res files
        // never reach the running API and the user has to remember to run
        // res:dev in a third terminal — guard against regressions.
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val serverPkg = files["packages/server/package.json"]!!
        assertTrue(serverPkg.contains("npm:res:dev"))
        assertTrue(serverPkg.contains("node --watch src/ServerMain.res.mjs"))
        // concurrently is required at the workspace level (not just root) so
        // pnpm's non-hoisted layout still resolves the binary.
        assertTrue(serverPkg.contains("\"concurrently\": \"${TemplateVersions.CONCURRENTLY}\""))
    }

    @Test
    fun `client workspace dev script boots rescript watcher alongside Vite-plus`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        val clientPkg = files["packages/client/package.json"]!!
        assertTrue(clientPkg.contains("npm:res:dev"))
        assertTrue(clientPkg.contains("vp dev"))
        assertTrue(clientPkg.contains("\"concurrently\": \"${TemplateVersions.CONCURRENTLY}\""))
    }

    @Test
    fun `server workspace ships ServerMain res with side-effect-free Server res`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(files.containsKey("packages/server/src/ServerMain.res"))
        assertTrue(files["packages/server/src/ServerMain.res"]!!.contains("Server.start()"))
        val server = files["packages/server/src/Server.res"]!!
        assertTrue(server.contains("let start = () => {"))
        val topLevelLines = server.lines().filter { !it.startsWith("  ") && !it.startsWith("//") }
        assertFalse(
            topLevelLines.any { it.trimStart().startsWith("HonoNodeServer.serve") },
            "Monorepo server's Server.res must wrap serve() inside start()",
        )
    }

    @Test
    fun `server workspace vitest setup pins DATABASE_URL to in-memory`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertTrue(files.containsKey("packages/server/vitest.config.mjs"))
        assertTrue(files.containsKey("packages/server/vitest.setup.mjs"))
        assertTrue(files["packages/server/vitest.setup.mjs"]!!.contains("DATABASE_URL"))
        assertTrue(files["packages/server/vitest.setup.mjs"]!!.contains(":memory:"))
    }

    @Test
    fun `postgres variant swaps server schema, db, drizzle config, and ships compose yaml`() {
        val ctx = TemplateContext("app", PackageManager.PNPM, database = Database.POSTGRES)
        val files = MonorepoTemplateFiles.generate(ctx)
        assertTrue(files["packages/server/src/Schema.res"]!!.contains("pgTable"))
        assertFalse(files["packages/server/src/Schema.res"]!!.contains("sqliteTable"))
        assertTrue(files["packages/server/src/Db.res"]!!.contains("postgres-js"))
        assertTrue(files["packages/server/drizzle.config.ts"]!!.contains("dialect: \"postgresql\""))
        assertTrue(files.containsKey("compose.yaml"))
        val serverPkg = files["packages/server/package.json"]!!
        assertTrue(serverPkg.contains("\"postgres\": \"${TemplateVersions.POSTGRES_JS}\""))
        assertFalse(serverPkg.contains("\"@libsql/client\""))
    }

    @Test
    fun `mysql variant swaps server schema, db, drizzle config, and ships compose yaml`() {
        val ctx = TemplateContext("app", PackageManager.PNPM, database = Database.MYSQL)
        val files = MonorepoTemplateFiles.generate(ctx)
        assertTrue(files["packages/server/src/Schema.res"]!!.contains("mysqlTable"))
        assertTrue(files["packages/server/src/Db.res"]!!.contains("mysql2"))
        assertTrue(files["packages/server/drizzle.config.ts"]!!.contains("dialect: \"mysql\""))
        assertTrue(files.containsKey("compose.yaml"))
        val serverPkg = files["packages/server/package.json"]!!
        assertTrue(serverPkg.contains("\"mysql2\": \"${TemplateVersions.MYSQL2}\""))
    }

    @Test
    fun `libsql variant does not ship a compose yaml`() {
        val files = MonorepoTemplateFiles.generate(TemplateContext("app", PackageManager.PNPM))
        assertFalse(files.containsKey("compose.yaml"))
    }
}
