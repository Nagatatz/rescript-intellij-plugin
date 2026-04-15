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
    fun `wrangler config declares the GREETINGS KV binding`() {
        val cfg = CloudflareWorkersTemplateFiles.generate(ctx)["wrangler.jsonc"]!!
        assertTrue(cfg.contains("kv_namespaces"))
        assertTrue(cfg.contains("GREETINGS"))
    }
}
