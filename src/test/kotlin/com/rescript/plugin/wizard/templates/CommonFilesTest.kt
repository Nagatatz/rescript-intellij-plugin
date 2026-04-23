package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommonFilesTest {
    private val pnpmCtx = TemplateContext("demo", PackageManager.PNPM)
    private val npmCtx = TemplateContext("demo", PackageManager.NPM)
    private val bunCtx = TemplateContext("demo", PackageManager.BUN)

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
    fun `readme swaps Corepack wording for Bun install guidance when PM is BUN`() {
        val readme =
            CommonFiles.readme(
                ctx = bunCtx,
                description = "x",
                scripts = emptyList(),
            )
        assertTrue(readme.contains("- Bun ${TemplateVersions.BUN} or later"))
        assertTrue(readme.contains("https://bun.sh"))
        assertFalse(readme.contains("Bun (managed via Corepack)"))
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
    fun `readme appends extra prerequisites when provided`() {
        val readme =
            CommonFiles.readme(
                ctx = pnpmCtx,
                description = "x",
                scripts = emptyList(),
                extraPrerequisites = listOf("Bun 1.3 or later (install from https://bun.sh)"),
            )
        val bunIdx = readme.indexOf("- Bun 1.3 or later")
        val gettingStartedIdx = readme.indexOf("## Getting Started")
        assertTrue(bunIdx > 0, "extra prerequisite should appear in the Prerequisites section")
        assertTrue(
            bunIdx < gettingStartedIdx,
            "extra prerequisite must be emitted before Getting Started",
        )
    }

    @Test
    fun `readme omits extra prerequisites section when list is empty`() {
        val readme =
            CommonFiles.readme(
                ctx = pnpmCtx,
                description = "x",
                scripts = emptyList(),
            )
        assertFalse(readme.contains("- Bun"))
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
    fun `readme appends Extending Bindings section with recipes`() {
        val readme =
            CommonFiles.readme(
                ctx = pnpmCtx,
                description = "x",
                scripts = emptyList(),
            )
        assertTrue(readme.contains("## Extending Bindings"))
        assertTrue(readme.contains("### Binding attributes at a glance"))
        assertTrue(readme.contains("### Pattern: typed fetch wrapper"))
        assertTrue(readme.contains("### Pattern: adding a Hono middleware"))
        assertTrue(readme.contains("### Pattern: filtering with drizzle-orm"))
        assertTrue(readme.contains("@rescript/webapi"))
    }

    @Test
    fun `Extending Bindings is placed between extra sections and Learn More`() {
        val readme =
            CommonFiles.readme(
                ctx = pnpmCtx,
                description = "x",
                scripts = emptyList(),
                extraSections = listOf("Deploy" to "Run `wrangler deploy`."),
            )
        val deployIdx = readme.indexOf("## Deploy")
        val extendingIdx = readme.indexOf("## Extending Bindings")
        val learnMoreIdx = readme.indexOf("## Learn More")
        assertTrue(deployIdx in 0 until extendingIdx, "Extending Bindings should follow extra sections")
        assertTrue(extendingIdx in 0 until learnMoreIdx, "Learn More should follow Extending Bindings")
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
    fun `ci workflow emits oven-sh setup-bun when setupBun is true`() {
        val yaml = CommonFiles.ciWorkflow(pnpmCtx, setupBun = true)
        assertTrue(yaml.contains("oven-sh/setup-bun@v2"))
        assertTrue(yaml.contains("bun-version: latest"))
    }

    @Test
    fun `ci workflow omits setup-bun by default`() {
        val yaml = CommonFiles.ciWorkflow(pnpmCtx)
        assertFalse(yaml.contains("oven-sh/setup-bun"))
    }

    @Test
    fun `ci workflow auto-enables setup-bun when the PackageManager is BUN`() {
        val yaml = CommonFiles.ciWorkflow(bunCtx)
        assertTrue(yaml.contains("oven-sh/setup-bun@v2"))
        assertFalse(
            yaml.contains("pnpm/action-setup"),
            "BUN selection should not trigger pnpm setup step",
        )
    }

    @Test
    fun `ci workflow for npm omits pnpm action setup`() {
        val yaml = CommonFiles.ciWorkflow(npmCtx, hasBuild = false, hasTest = true)
        assertFalse(yaml.contains("pnpm/action-setup"))
        assertTrue(yaml.contains("npm install"))
        assertTrue(yaml.contains("npx rescript"))
        assertTrue(yaml.contains("npm run test"))
    }

    @Test
    fun `nvmrc returns the node major version with trailing newline`() {
        val content = CommonFiles.nvmrc()
        assertTrue(content.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(content.endsWith("\n"))
    }

    @Test
    fun `mitLicense contains the standard MIT wording, year, and holder`() {
        val content = CommonFiles.mitLicense(year = 2026, holder = "Acme Inc.")
        assertTrue(content.startsWith("MIT License"))
        assertTrue(content.contains("Copyright (c) 2026 Acme Inc."))
        assertTrue(content.contains("Permission is hereby granted"))
        assertTrue(content.contains("WITHOUT WARRANTY OF ANY KIND"))
    }

    @Test
    fun `dependabot yaml declares npm and github-actions ecosystems`() {
        val content = CommonFiles.dependabotYaml()
        assertTrue(content.contains("version: 2"))
        assertTrue(content.contains("package-ecosystem: \"npm\""))
        assertTrue(content.contains("package-ecosystem: \"github-actions\""))
        assertTrue(content.contains("interval: \"weekly\""))
    }

    @Test
    fun `envExample renders comment lines followed by key-value pairs`() {
        val content =
            CommonFiles.envExample(
                listOf(
                    "SQLite file or Turso libsql URL" to "DATABASE_URL=file:./data/app.db",
                    "" to "PORT=3000",
                ),
            )
        assertTrue(content.contains("# SQLite file or Turso libsql URL"))
        assertTrue(content.contains("DATABASE_URL=file:./data/app.db"))
        assertTrue(content.contains("PORT=3000"))
    }
}
