package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HonoGraphqlTemplateFilesTest {
    private val ctx = TemplateContext("graphql-svc", PackageManager.PNPM)

    @Test
    fun `package json declares hono and graphql-yoga`() {
        val pkg = HonoGraphqlTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"hono\": \"${TemplateVersions.HONO}\""))
        assertTrue(pkg.contains("\"graphql-yoga\": \"${TemplateVersions.GRAPHQL_YOGA}\""))
        assertTrue(pkg.contains("\"graphql\": \"${TemplateVersions.GRAPHQL}\""))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI`() {
        val files = HonoGraphqlTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
    }

    @Test
    fun `gitignore excludes data and generated graphql docs`() {
        val gitignore = HonoGraphqlTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains("data/"))
        assertTrue(gitignore.contains("docs/schema.md"))
    }
}
