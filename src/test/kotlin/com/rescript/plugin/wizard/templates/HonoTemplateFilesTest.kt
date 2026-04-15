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
}
