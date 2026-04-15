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
}
