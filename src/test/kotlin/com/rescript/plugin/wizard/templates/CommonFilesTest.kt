package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommonFilesTest {
    private val pnpmCtx = TemplateContext("demo", PackageManager.PNPM)
    private val npmCtx = TemplateContext("demo", PackageManager.NPM)

    @Test
    fun `gitignore covers node_modules, ReScript artifacts, OS files`() {
        val content = CommonFiles.gitignore()
        assertTrue(content.contains("node_modules/"))
        assertTrue(content.contains("lib/"))
        assertTrue(content.contains("*.res.mjs"))
        assertTrue(content.contains(".DS_Store"))
    }

    @Test
    fun `gitignore appends project-specific patterns when provided`() {
        val content = CommonFiles.gitignore(listOf("dist/", ".next/"))
        assertTrue(content.contains("dist/"))
        assertTrue(content.contains(".next/"))
        assertTrue(content.contains("Project-specific"))
    }

    @Test
    fun `editorconfig declares 2-space indent and LF line endings`() {
        val content = CommonFiles.editorconfig()
        assertTrue(content.contains("indent_size = 2"))
        assertTrue(content.contains("end_of_line = lf"))
        assertTrue(content.startsWith("root = true"))
    }

    @Test
    fun `readme renders install and run commands using the selected package manager`() {
        val readme =
            CommonFiles.readme(
                ctx = pnpmCtx,
                description = "A demo project.",
                scripts = listOf("dev" to "Start dev server", "build" to "Build for production"),
            )
        assertTrue(readme.contains("# demo"))
        assertTrue(readme.contains("A demo project."))
        assertTrue(readme.contains("pnpm install"))
        assertTrue(readme.contains("pnpm dev"))
        assertTrue(readme.contains("pnpm build"))
        assertFalse(readme.contains("npm run dev"), "pnpm readme should not use 'npm run dev'")
    }

    @Test
    fun `readme falls back to res dev when no top-level dev script exists`() {
        val readme =
            CommonFiles.readme(
                ctx = pnpmCtx,
                description = "A library.",
                scripts = listOf("res:dev" to "Watch sources", "res:build" to "Compile"),
            )
        assertTrue(readme.contains("pnpm res:dev"))
    }

    @Test
    fun `readme appends extra sections verbatim`() {
        val readme =
            CommonFiles.readme(
                ctx = pnpmCtx,
                description = "x",
                scripts = emptyList(),
                extraSections = listOf("Deploy" to "Run `wrangler deploy`."),
            )
        assertTrue(readme.contains("## Deploy"))
        assertTrue(readme.contains("wrangler deploy"))
    }

    @Test
    fun `ci workflow installs with pnpm and pins pnpm action setup`() {
        val yaml = CommonFiles.ciWorkflow(pnpmCtx, hasBuild = true, hasTest = false)
        assertTrue(yaml.contains("pnpm/action-setup@v4"))
        assertTrue(yaml.contains("pnpm install"))
        assertTrue(yaml.contains("pnpm exec rescript"))
        assertTrue(yaml.contains("pnpm build"))
        assertFalse(yaml.contains("pnpm test"), "test step should be skipped when hasTest is false")
    }

    @Test
    fun `ci workflow for npm omits pnpm action setup`() {
        val yaml = CommonFiles.ciWorkflow(npmCtx, hasBuild = false, hasTest = true)
        assertFalse(yaml.contains("pnpm/action-setup"))
        assertTrue(yaml.contains("npm install"))
        assertTrue(yaml.contains("npx rescript"))
        assertTrue(yaml.contains("npm run test"))
    }
}
