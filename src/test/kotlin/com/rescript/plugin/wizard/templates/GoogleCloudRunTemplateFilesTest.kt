package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GoogleCloudRunTemplateFilesTest {
    @Test
    fun `Dockerfile uses pnpm when pnpm is selected`() {
        val ctx = TemplateContext("svc", PackageManager.PNPM)
        val dockerfile = GoogleCloudRunTemplateFiles.generate(ctx)["Dockerfile"]!!
        assertTrue(dockerfile.contains("corepack enable && pnpm install"))
        assertTrue(dockerfile.contains("pnpm exec rescript"))
    }

    @Test
    fun `Dockerfile uses npm when npm is selected`() {
        val ctx = TemplateContext("svc", PackageManager.NPM)
        val dockerfile = GoogleCloudRunTemplateFiles.generate(ctx)["Dockerfile"]!!
        assertTrue(dockerfile.contains("npm install --omit=dev"))
        assertTrue(dockerfile.contains("npx rescript"))
    }

    @Test
    fun `template ships dockerignore, README, and CI workflow`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        assertTrue(files.containsKey(".dockerignore"))
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        val readme = files["README.md"]!!
        assertTrue(readme.contains("gcloud run deploy"))
    }
}
