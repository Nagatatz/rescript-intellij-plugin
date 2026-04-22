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
        assertTrue(pkg.contains("\"build\": \"vite build\""))
        assertFalse(pkg.contains("\"start\": \"node "))
    }

    @Test
    fun `dev script launches rescript watcher and bun watcher via concurrently`() {
        val pkg = ResXTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"dev\": \"concurrently"))
        assertTrue(pkg.contains("rescript -w"))
        assertTrue(pkg.contains("bun --watch run src/App.res.mjs"))
    }

    @Test
    fun `compile script invokes bun build for a standalone binary`() {
        val pkg = ResXTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(
            pkg.contains("\"compile\": \"bun build --compile src/App.res.mjs --outfile dist/app\""),
        )
    }

    @Test
    fun `README documents the compile script alongside dev and build`() {
        val readme = ResXTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("pnpm compile"))
        assertTrue(readme.contains("standalone binary"))
    }

    @Test
    fun `package json declares concurrently devDependency`() {
        val pkg = ResXTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"concurrently\": \"${TemplateVersions.CONCURRENTLY}\""))
    }

    @Test
    fun `README mentions the Bun prerequisite`() {
        val readme = ResXTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## Prerequisites"))
        assertTrue(readme.contains("Bun 1.3 or later"))
        assertTrue(readme.contains("https://bun.sh"))
    }

    @Test
    fun `CI workflow installs Bun via oven-sh setup action`() {
        val yaml = ResXTemplateFiles.generate(ctx)[".github/workflows/ci.yml"]!!
        assertTrue(yaml.contains("oven-sh/setup-bun"))
        assertTrue(yaml.contains("bun-version: latest"))
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
    fun `Layout interpolates children through JSX braces rather than as a literal`() {
        val layout = ResXTemplateFiles.generate(ctx)["src/Layout.res"]!!
        assertTrue(layout.contains("{children}"))
        assertFalse(layout.contains("<main> children </main>"))
    }

    @Test
    fun `TodoForm avoids React-style key attributes that Hjsx cannot render`() {
        val form = ResXTemplateFiles.generate(ctx)["src/TodoForm.res"]!!
        assertFalse(form.contains("key={todo.name}"))
        assertFalse(form.contains(" key="))
    }

    @Test
    fun `TodoForm reads form fields through the supported getString helper`() {
        val form = ResXTemplateFiles.generate(ctx)["src/TodoForm.res"]!!
        assertTrue(form.contains("ResX.FormDataHelpers.getString"))
        assertFalse(form.contains("ResX.FormDataHelpers.maybeString"))
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
    fun `zod variant drives length checks through the schema and safeParse`() {
        val validation = ResXTemplateFiles.generate(ctx)["src/Validation.res"]!!
        assertTrue(validation.contains("->trim"))
        assertTrue(validation.contains("->min(1, \"Name must not be empty\")"))
        assertTrue(validation.contains("->max(80, \"Name must be 80 characters or fewer\")"))
        assertTrue(validation.contains("->max(240, \"Description must be 240 characters or fewer\")"))
        assertTrue(validation.contains("safeParse"))
        assertFalse(
            validation.contains("if trimmedName"),
            "length checks should live in the schema, not the app layer",
        )
        assertFalse(validation.contains("String.length(trimmedName)"))
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
    fun `sury variant constructs payload via a ReScript object literal, not raw template`() {
        val validation = ResXTemplateFiles.generate(suryCtx)["src/Validation.res"]!!
        assertFalse(validation.contains("%raw(`"))
        assertTrue(validation.contains("\"name\": name"))
        assertTrue(validation.contains("\"description\": description"))
    }

    @Test
    fun `sury variant drives length checks through the schema via S trim and min and max`() {
        val validation = ResXTemplateFiles.generate(suryCtx)["src/Validation.res"]!!
        assertTrue(validation.contains("S.trim"))
        assertTrue(validation.contains("S.min(1, ~message=\"Name must not be empty\")"))
        assertTrue(validation.contains("S.max(80, ~message=\"Name must be 80 characters or fewer\")"))
        assertTrue(
            validation.contains(
                "S.max(240, ~message=\"Description must be 240 characters or fewer\")",
            ),
        )
        assertFalse(
            validation.contains("if trimmedName"),
            "length checks should live in the schema, not the app layer",
        )
        assertFalse(validation.contains("String.length(trimmedName)"))
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
    fun `vite config leaves clientDirs empty to match the generated project layout`() {
        val config = ResXTemplateFiles.generate(ctx)["vite.config.js"]!!
        assertTrue(config.contains("clientDirs: []"))
        assertFalse(config.contains("clientDirs: [\"client\"]"))
    }

    @Test
    fun `rescript_json uses the non-deprecated dependencies and compiler-flags fields`() {
        val config = ResXTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(config.contains("\"dependencies\""))
        assertTrue(config.contains("\"compiler-flags\""))
        assertFalse(config.contains("\"bs-dependencies\""))
        assertFalse(config.contains("\"bsc-flags\""))
    }

    @Test
    fun `template ships a Dockerfile based on the oven bun image`() {
        val files = ResXTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("Dockerfile"))
        val dockerfile = files["Dockerfile"]!!
        assertTrue(dockerfile.contains("FROM oven/bun:1"))
        assertTrue(dockerfile.contains("bunx rescript"))
        assertTrue(dockerfile.contains("CMD [\"bun\", \"run\", \"src/App.res.mjs\"]"))
        assertTrue(dockerfile.contains("EXPOSE 4444"))
    }

    @Test
    fun `README documents the Deploy section with docker build instructions`() {
        val readme = ResXTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## Deploy"))
        assertTrue(readme.contains("docker build"))
        assertTrue(readme.contains("docker run"))
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
