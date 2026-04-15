package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GoogleCloudRunTemplateFilesTest {
    @Test
    fun `Dockerfile uses pnpm when pnpm is selected`() {
        val ctx = TemplateContext("svc", PackageManager.PNPM)
        val dockerfile =
            GoogleCloudRunTemplateFiles.generate(
                TemplateContext("svc", PackageManager.PNPM),
            )["Dockerfile"]!!
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

    @Test
    fun `server reads PORT env var and exposes POST echo endpoint`() {
        val ctx = TemplateContext("svc", PackageManager.PNPM)
        val server =
            GoogleCloudRunTemplateFiles.generate(
                TemplateContext("svc", PackageManager.PNPM),
            )["src/Server.res"]!!
        assertTrue(server.contains("process.env"))
        assertTrue(server.contains("PORT"))
        assertTrue(server.contains("Hono.post"))
        assertTrue(server.contains("/echo"))
    }

    @Test
    fun `package json declares test script and vitest devDep`() {
        val pkg = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))["package.json"]!!
        assertTrue(pkg.contains("\"test\": \"vitest run\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
    }

    @Test
    fun `ships a vitest smoke test`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        assertTrue(files.containsKey("src/__tests__/Server.test.mjs"))
        assertTrue(files["src/__tests__/Server.test.mjs"]!!.contains("import(\"../Server.res.mjs\")"))
    }

    @Test
    fun `README documents Cloud SQL recipe and environment section`() {
        val readme = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))["README.md"]!!
        assertTrue(readme.contains("## Environment"))
        assertTrue(readme.contains("## Cloud SQL Recipe"))
        assertTrue(readme.contains("@google-cloud/cloud-sql-connector"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("svc"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = GoogleCloudRunTemplateFiles.generate(TemplateContext("svc", PackageManager.PNPM))["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }
}
