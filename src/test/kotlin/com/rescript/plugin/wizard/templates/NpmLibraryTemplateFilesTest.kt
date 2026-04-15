package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NpmLibraryTemplateFilesTest {
    private val ctx = TemplateContext("util-lib", PackageManager.PNPM)

    @Test
    fun `rescript json enables genType`() {
        val rj = NpmLibraryTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("gentypeconfig"))
    }

    @Test
    fun `package json includes vitest, typescript, and prepare hook`() {
        val pkg = NpmLibraryTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
        assertTrue(pkg.contains("\"typescript\": \"${TemplateVersions.TYPESCRIPT}\""))
        assertTrue(pkg.contains("\"prepare\": \"rescript\""))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI, and a vitest sample`() {
        val files = NpmLibraryTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("src/__tests__/Index.test.mjs"))
    }

    @Test
    fun `README documents publish flow`() {
        val readme = NpmLibraryTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## Publish"))
        assertTrue(readme.contains("npm publish"))
    }
}
