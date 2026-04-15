package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CliToolTemplateFilesTest {
    private val ctx = TemplateContext("hello-cli", PackageManager.PNPM)

    @Test
    fun `package json declares bin entry, vitest, and packageManager metadata`() {
        val pkg = CliToolTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"bin\": \"./src/Cli.res.mjs\""))
        assertTrue(pkg.contains("\"vitest\""))
        assertTrue(pkg.contains("\"packageManager\""))
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
        assertTrue(files.containsKey("src/Commands/Greet.res"))
        assertTrue(files.containsKey("src/Commands/Init.res"))
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
}
