package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BasicTemplateFilesTest {
    private val pnpmCtx = TemplateContext("demo", PackageManager.PNPM)
    private val npmCtx = TemplateContext("demo", PackageManager.NPM)

    @Test
    fun `generates README, gitignore, editorconfig, and CI workflow`() {
        val files = BasicTemplateFiles.generate(pnpmCtx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
    }

    @Test
    fun `package json declares packageManager and engines for the selected PM`() {
        val pkg = BasicTemplateFiles.generate(pnpmCtx)["package.json"]!!
        assertTrue(pkg.contains("\"packageManager\": \"pnpm@${TemplateVersions.PNPM}\""))
        assertTrue(pkg.contains("\"engines\""))
        assertTrue(pkg.contains("\"node\": \"${TemplateVersions.NODE_ENGINE}\""))
    }

    @Test
    fun `package json reflects npm when npm is selected`() {
        val pkg = BasicTemplateFiles.generate(npmCtx)["package.json"]!!
        assertTrue(pkg.contains("\"packageManager\": \"npm@${TemplateVersions.NPM}\""))
    }

    @Test
    fun `dependencies pull from TemplateVersions`() {
        val pkg = BasicTemplateFiles.generate(pnpmCtx)["package.json"]!!
        assertTrue(pkg.contains("\"rescript\": \"${TemplateVersions.RESCRIPT}\""))
        assertTrue(pkg.contains("\"@rescript/core\": \"${TemplateVersions.RESCRIPT_CORE}\""))
    }

    @Test
    fun `README mentions selected package manager commands`() {
        val readme = BasicTemplateFiles.generate(pnpmCtx)["README.md"]!!
        assertTrue(readme.contains("# demo"))
        assertTrue(readme.contains("pnpm install"))
        assertTrue(readme.contains("pnpm res:dev"))
    }

    @Test
    fun `legacy generate(projectName) still works and defaults to pnpm`() {
        val files = BasicTemplateFiles.generate("legacy")
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"name\": \"legacy\""))
        assertTrue(pkg.contains("pnpm@"))
    }
}
