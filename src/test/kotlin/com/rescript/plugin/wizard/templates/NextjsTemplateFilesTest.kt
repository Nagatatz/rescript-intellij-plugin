package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NextjsTemplateFilesTest {
    private val ctx = TemplateContext("site", PackageManager.PNPM)

    @Test
    fun `package json includes Next, vitest, and packageManager metadata`() {
        val pkg = NextjsTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"next\": \"${TemplateVersions.NEXTJS}\""))
        assertTrue(pkg.contains("\"vitest\""))
        assertTrue(pkg.contains("\"packageManager\""))
    }

    @Test
    fun `template includes README, gitignore, editorconfig, CI, and a vitest sample`() {
        val files = NextjsTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("src/__tests__/App.test.mjs"))
    }

    @Test
    fun `gitignore covers the Next dot next folder`() {
        val gitignore = NextjsTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains(".next/"))
    }
}
