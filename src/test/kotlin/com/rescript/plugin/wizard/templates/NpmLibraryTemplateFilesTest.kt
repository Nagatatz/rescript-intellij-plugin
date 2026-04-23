package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NpmLibraryTemplateFilesTest {
    private val ctx = TemplateContext("util-lib", PackageManager.PNPM)

    @Test
    fun `rescript json enables genType`() {
        val rj = NpmLibraryTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("gentypeconfig"))
    }

    @Test
    fun `package json includes vitest, typescript, and prepare hook`() {
        val pkg = NpmLibraryTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
        assertTrue(pkg.contains("\"typescript\": \"${TemplateVersions.TYPESCRIPT}\""))
        assertTrue(pkg.contains("\"prepare\": \"rescript\""))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI, and a vitest sample`() {
        val files = NpmLibraryTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("src/__tests__/Index.test.mjs"))
    }

    @Test
    fun `README documents publish flow`() {
        val readme = NpmLibraryTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## Publish"))
        assertTrue(readme.contains("npm publish"))
    }

    @Test
    fun `ships multi-module API with sync, async, and generic helpers`() {
        val files = NpmLibraryTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/ListUtils.res"))
        assertTrue(files.containsKey("src/Fetcher.res"))
        assertTrue(files["src/ListUtils.res"]!!.contains("chunk"))
        assertTrue(files["src/ListUtils.res"]!!.contains("partitionMap"))
        assertTrue(files["src/Fetcher.res"]!!.contains("fetchWithTimeout"))
        assertTrue(files["src/Fetcher.res"]!!.contains("AbortController"))
    }

    @Test
    fun `ships a Vitest file per module`() {
        val files = NpmLibraryTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/__tests__/Index.test.mjs"))
        assertTrue(files.containsKey("src/__tests__/ListUtils.test.mjs"))
        assertTrue(files.containsKey("src/__tests__/Fetcher.test.mjs"))
    }

    @Test
    fun `README lists the API surface`() {
        val readme = NpmLibraryTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("## API Surface"))
        assertTrue(readme.contains("chunk"))
        assertTrue(readme.contains("fetchWithTimeout"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = NpmLibraryTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("util-lib"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = NpmLibraryTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `zod variant ships Validation res and pins the zod dependency`() {
        val zodCtx = ctx.copy(validationLibrary = ValidationLibrary.ZOD)
        val files = NpmLibraryTemplateFiles.generate(zodCtx)
        assertTrue(files["src/Validation.res"]!!.contains("@module(\"zod\")"))
        assertTrue(files["src/Validation.res"]!!.contains("parseGreetInput"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships Validation res and pins the sury dependency`() {
        val suryCtx = ctx.copy(validationLibrary = ValidationLibrary.SURY)
        val files = NpmLibraryTemplateFiles.generate(suryCtx)
        assertTrue(files["src/Validation.res"]!!.contains("S.parseOrThrow"))
        assertTrue(files["src/Validation.res"]!!.contains("parseGreetInput"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\""))
        assertFalse(pkg.contains("\"zod\""))
    }

    @Test
    fun `Index res exposes greetChecked that goes through Validation`() {
        val index = NpmLibraryTemplateFiles.generate(ctx)["src/Index.res"]!!
        assertTrue(index.contains("greetChecked"))
        assertTrue(index.contains("Validation.parseGreetInput"))
    }

    @Test
    fun `ships strict tsconfig with declaration support`() {
        val tsconfig = NpmLibraryTemplateFiles.generate(ctx)["tsconfig.json"]!!
        assertTrue(tsconfig.contains("\"strict\": true"))
        assertTrue(tsconfig.contains("\"noImplicitAny\": true"))
        assertTrue(tsconfig.contains("\"declaration\": true"))
    }
}
