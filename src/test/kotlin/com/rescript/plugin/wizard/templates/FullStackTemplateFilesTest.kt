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
        assertTrue(files.containsKey("src/server/Main.res"))
        assertTrue(files.containsKey("src/client/App.res"))
        assertTrue(files.containsKey("src/client/Main.res"))
    }
}
