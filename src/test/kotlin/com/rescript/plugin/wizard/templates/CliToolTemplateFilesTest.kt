package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CliToolTemplateFilesTest {
    private val ctx = TemplateContext("hello-cli", PackageManager.PNPM)

    @Test
    fun `package json declares bin entry, vitest, and packageManager metadata`() {
        val pkg = CliToolTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"bin\": \"./src/Cli.res.mjs\""))
        assertTrue(pkg.contains("\"vitest\""))
        assertTrue(pkg.contains("\"packageManager\""))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI, and a vitest sample`() {
        val files = CliToolTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("src/__tests__/Cli.test.mjs"))
    }

    @Test
    fun `README documents npm link instructions`() {
        val readme = CliToolTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("npm link"))
        assertTrue(readme.contains("hello-cli"))
    }
}
