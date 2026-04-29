package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AstroTemplateFilesTest {
    private val ctx = TemplateContext("astrosite", PackageManager.PNPM)

    @Test
    fun `package json includes Astro and the React integration`() {
        val pkg = AstroTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"astro\": \"${TemplateVersions.ASTRO}\""))
        assertTrue(pkg.contains("\"@astrojs/react\": \"${TemplateVersions.ASTROJS_REACT}\""))
        assertTrue(pkg.contains("\"@astrojs/node\": \"${TemplateVersions.ASTROJS_NODE}\""))
        assertTrue(pkg.contains("\"vitest\""))
        assertTrue(pkg.contains("\"packageManager\""))
        assertTrue(pkg.contains("\"type\": \"module\""))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI, astro config and tsconfig`() {
        val files = AstroTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("astro.config.mjs"))
        assertTrue(files.containsKey("tsconfig.json"))
    }

    @Test
    fun `astro config wires the React integration`() {
        val cfg = AstroTemplateFiles.generate(ctx)["astro.config.mjs"]!!
        assertTrue(cfg.contains("@astrojs/react"))
        assertTrue(cfg.contains("integrations: [react()]"))
    }

    @Test
    fun `index page hydrates the Counter island with client load`() {
        val page = AstroTemplateFiles.generate(ctx)["src/pages/index.astro"]!!
        assertTrue(page.contains("client:load"))
        assertTrue(page.contains("../components/Counter.res.mjs"))
        assertTrue(page.contains("../components/StaticGreeting.res.mjs"))
        assertTrue(page.contains("\"astrosite\""))
    }

    @Test
    fun `Counter Island and StaticGreeting both ship as react components`() {
        val files = AstroTemplateFiles.generate(ctx)
        assertTrue(files["src/components/Counter.res"]!!.contains("@react.component"))
        assertTrue(files["src/components/StaticGreeting.res"]!!.contains("@react.component"))
    }

    @Test
    fun `gitignore covers the Astro build folder`() {
        val gitignore = AstroTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains(".astro/"))
        assertTrue(gitignore.contains("dist/"))
    }

    @Test
    fun `template never emits a Validation res because validation selection is disabled`() {
        val files = AstroTemplateFiles.generate(ctx)
        assertFalse(files.keys.any { it.endsWith("Validation.res") })
    }
}
