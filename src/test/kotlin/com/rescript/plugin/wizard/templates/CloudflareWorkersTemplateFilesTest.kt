package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CloudflareWorkersTemplateFilesTest {
    private val ctx = TemplateContext("worker", PackageManager.PNPM)

    @Test
    fun `package json includes wrangler from TemplateVersions`() {
        val pkg = CloudflareWorkersTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"wrangler\": \"${TemplateVersions.WRANGLER}\""))
    }

    @Test
    fun `gitignore covers wrangler artifacts`() {
        val gitignore = CloudflareWorkersTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains(".wrangler/"))
    }

    @Test
    fun `template includes README with Deploy section`() {
        val readme = CloudflareWorkersTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## Deploy"))
        assertTrue(readme.contains("wrangler login"))
    }
}
