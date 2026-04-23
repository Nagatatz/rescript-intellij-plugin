package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CliToolTemplateFilesTest {
    private val ctx = TemplateContext("hello-cli", PackageManager.PNPM)

    @Test
    fun `package json declares bin object, vitest, and packageManager metadata`() {
        val pkg = CliToolTemplateFiles.generate(ctx)["package.json"]!!
        // Object-form bin keyed by project name so multi-binary packages extend cleanly.
        assertTrue(pkg.contains("\"bin\""))
        assertTrue(pkg.contains("\"hello-cli\": \"./bin/cli.mjs\""))
        assertTrue(pkg.contains("\"vitest\""))
        assertTrue(pkg.contains("\"packageManager\""))
    }

    @Test
    fun `ships bin wrapper with node shebang pointing at compiled Cli res mjs`() {
        val files = CliToolTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("bin/cli.mjs"))
        val wrapper = files["bin/cli.mjs"]!!
        assertTrue(wrapper.startsWith("#!/usr/bin/env node"))
        assertTrue(wrapper.contains("../src/Cli.res.mjs"))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI, and a vitest sample`() {
        val files = CliToolTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("src/__tests__/Args.test.mjs"))
    }

    @Test
    fun `README documents npm link instructions`() {
        val readme = CliToolTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("npm link"))
        assertTrue(readme.contains("hello-cli"))
    }

    @Test
    fun `ships subcommand dispatcher with Args, Greet, and Init`() {
        val files = CliToolTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Args.res"))
        assertTrue(files.containsKey("src/Cli.res"))
        assertTrue(files.containsKey("src/Commands.res"))
        assertTrue(files["src/Commands.res"]!!.contains("module Greet"))
        assertTrue(files["src/Commands.res"]!!.contains("module Init"))
        assertTrue(files["src/Cli.res"]!!.contains("Commands.Greet.run"))
        assertTrue(files["src/Cli.res"]!!.contains("Commands.Init.run"))
    }

    @Test
    fun `README documents available subcommands`() {
        val readme = CliToolTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("greet Alice"))
        assertTrue(readme.contains("init "))
        assertTrue(readme.contains("--shout"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = CliToolTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("hello-cli"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = CliToolTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `zod variant ships a zod Validation res and pins the zod dependency`() {
        val zodCtx = ctx.copy(validationLibrary = ValidationLibrary.ZOD)
        val files = CliToolTemplateFiles.generate(zodCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseInitOptions"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships a sury Validation res and pins the sury dependency`() {
        val suryCtx = ctx.copy(validationLibrary = ValidationLibrary.SURY)
        val files = CliToolTemplateFiles.generate(suryCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.parseOrThrow"))
        assertTrue(validation.contains("parseInitOptions"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\""))
        assertFalse(pkg.contains("\"zod\""))
    }

    @Test
    fun `Commands res wires the init subcommand to Validation parseInitOptions`() {
        val commands = CliToolTemplateFiles.generate(ctx)["src/Commands.res"]!!
        assertTrue(commands.contains("Validation.parseInitOptions"))
        assertTrue(commands.contains("--name"))
        assertTrue(commands.contains("--dir"))
    }

    @Test
    fun `README usage section mentions the selected validation library`() {
        val readme = CliToolTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("zod"))
        assertTrue(readme.contains("--name"))
        assertTrue(readme.contains("--dir"))
    }
}
