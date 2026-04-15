package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
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
}
