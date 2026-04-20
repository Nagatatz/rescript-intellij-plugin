package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CloudflareWorkersTemplateFilesTest {
    private val ctx = TemplateContext("worker", PackageManager.PNPM)
    private val suryCtx = TemplateContext("worker", PackageManager.PNPM, ValidationLibrary.SURY)

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

    @Test
    fun `server uses KV namespace for POST and GET`() {
        val files = CloudflareWorkersTemplateFiles.generate(ctx)
        val server = files["src/Server.res"]!!
        assertTrue(files.containsKey("src/Kv.res"))
        assertTrue(server.contains("Hono.post"))
        assertTrue(server.contains("Kv.put"))
        assertTrue(server.contains("Kv.list"))
    }

    @Test
    fun `server validates POST body via Validation and returns 400 on Error`() {
        val server = CloudflareWorkersTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Validation.parseGreetingPayload"))
        assertTrue(server.contains("Hono.status(400)"))
    }

    @Test
    fun `zod variant ships zod Validation module and zod dependency`() {
        val files = CloudflareWorkersTemplateFiles.generate(ctx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseGreetingPayload"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury Validation module and sury dependency`() {
        val files = CloudflareWorkersTemplateFiles.generate(suryCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `package json declares test script and vitest devDep`() {
        val pkg = CloudflareWorkersTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test\": \"vitest run\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
    }

    @Test
    fun `ships a vitest smoke test`() {
        val files = CloudflareWorkersTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/__tests__/Server.test.mjs"))
        assertTrue(files["src/__tests__/Server.test.mjs"]!!.contains("import(\"../Server.res.mjs\")"))
    }

    @Test
    fun `wrangler config declares the GREETINGS KV binding`() {
        val cfg = CloudflareWorkersTemplateFiles.generate(ctx)["wrangler.jsonc"]!!
        assertTrue(cfg.contains("kv_namespaces"))
        assertTrue(cfg.contains("GREETINGS"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = CloudflareWorkersTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("worker"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = CloudflareWorkersTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }
}
