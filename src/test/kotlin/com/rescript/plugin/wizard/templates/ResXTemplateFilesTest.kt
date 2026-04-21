package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResXTemplateFilesTest {
    private val ctx = TemplateContext("app", PackageManager.PNPM)
    private val suryCtx = TemplateContext("app", PackageManager.PNPM, ValidationLibrary.SURY)

    @Test
    fun `package json pins rescript-x and rescript-bun from TemplateVersions`() {
        val pkg = ResXTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"rescript-x\": \"${TemplateVersions.RESCRIPT_X}\""))
        assertTrue(pkg.contains("\"rescript-bun\": \"${TemplateVersions.RESCRIPT_BUN}\""))
        assertTrue(pkg.contains("\"rescript\": \"${TemplateVersions.RESCRIPT}\""))
        assertTrue(pkg.contains("\"@rescript/core\": \"${TemplateVersions.RESCRIPT_CORE}\""))
    }

    @Test
    fun `package json scripts invoke bun directly for server commands`() {
        val pkg = ResXTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"start\": \"bun run src/App.res.mjs\""))
        assertTrue(pkg.contains("\"dev\": \"bun --watch run src/App.res.mjs\""))
        assertTrue(pkg.contains("\"build\": \"vite build\""))
        assertFalse(pkg.contains("\"start\": \"node "))
    }

    @Test
    fun `rescript_json selects Hjsx jsx module and opens res-x globals`() {
        val config = ResXTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(config.contains("\"name\": \"app\""))
        assertTrue(config.contains("\"module\": \"Hjsx\""))
        assertTrue(config.contains("\"rescript-x\""))
        assertTrue(config.contains("\"rescript-bun\""))
        assertTrue(config.contains("-open ResX.Globals"))
        assertTrue(config.contains("-open RescriptBun.Globals"))
    }

    @Test
    fun `template ships Counter, TodoForm, Layout, Handler and App modules`() {
        val files = ResXTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/App.res"))
        assertTrue(files.containsKey("src/Handler.res"))
        assertTrue(files.containsKey("src/Layout.res"))
        assertTrue(files.containsKey("src/Counter.res"))
        assertTrue(files.containsKey("src/TodoForm.res"))
        assertTrue(files.containsKey("src/Validation.res"))
        assertTrue(files.containsKey("vite.config.js"))
    }

    @Test
    fun `Counter component registers hx-post endpoints for increment and decrement`() {
        val counter = ResXTemplateFiles.generate(ctx)["src/Counter.res"]!!
        assertTrue(counter.contains("hxPost"))
        assertTrue(counter.contains("/counter/increment"))
        assertTrue(counter.contains("/counter/decrement"))
        assertTrue(counter.contains("Handler.handler.hxPost"))
    }

    @Test
    fun `TodoForm calls Validation parseTodoInput and sets 400 on error`() {
        val form = ResXTemplateFiles.generate(ctx)["src/TodoForm.res"]!!
        assertTrue(form.contains("Handler.handler.hxPost"))
        assertTrue(form.contains("/todos"))
        assertTrue(form.contains("Validation.parseTodoInput"))
        assertTrue(form.contains("requestController.setStatus(400)"))
    }

    @Test
    fun `Layout embeds the HTMX script tag using the HTMX_CDN version constant`() {
        val layout = ResXTemplateFiles.generate(ctx)["src/Layout.res"]!!
        assertTrue(layout.contains("htmx.org@${TemplateVersions.HTMX_CDN}"))
        assertTrue(layout.contains("<script"))
    }

    @Test
    fun `zod variant ships zod schema and declares the zod npm dependency`() {
        val files = ResXTemplateFiles.generate(ctx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseTodoInput"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury schema and declares the sury npm dependency`() {
        val files = ResXTemplateFiles.generate(suryCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        assertTrue(validation.contains("parseTodoInput"))
        assertFalse(validation.contains("@module(\"zod\")"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `README describes selected validation library and lists res-x sections`() {
        val zodReadme = ResXTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(zodReadme.contains("zod"))
        assertTrue(zodReadme.contains("## Application"))
        assertTrue(zodReadme.contains("## HTMX"))
        assertTrue(zodReadme.contains("## Project Layout"))

        val suryReadme = ResXTemplateFiles.generate(suryCtx)["README.md"]!!
        assertTrue(suryReadme.contains("sury"))
    }

    @Test
    fun `vite config loads the res-x Vite plugin`() {
        val config = ResXTemplateFiles.generate(ctx)["vite.config.js"]!!
        assertTrue(config.contains("rescript-x/res-x-vite-plugin.mjs"))
        assertTrue(config.contains("resXVitePlugin"))
    }

    @Test
    fun `ships common project files nvmrc, LICENSE, gitignore, editorconfig, CI`() {
        val files = ResXTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files.containsKey("src/__tests__/App.test.mjs"))
    }

    @Test
    fun `package json declares coverage tooling and vite as devDependencies`() {
        val pkg = ResXTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"vite\": \"${TemplateVersions.VITE}\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\": \"${TemplateVersions.VITEST_COVERAGE_V8}\""))
        assertTrue(pkg.contains("\"test:coverage\""))
    }

    @Test
    fun `gitignore extends defaults with build, dist, and res-x cache`() {
        val gitignore = ResXTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains("dist/"))
        assertTrue(gitignore.contains("build/"))
        assertTrue(gitignore.contains(".res-x-cache/"))
        assertTrue(gitignore.contains(".env"))
    }
}
