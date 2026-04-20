package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AwsLambdaTemplateFilesTest {
    private val ctx = TemplateContext("fn", PackageManager.PNPM)
    private val suryCtx = TemplateContext("fn", PackageManager.PNPM, ValidationLibrary.SURY)

    @Test
    fun `package json includes esbuild from TemplateVersions`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"esbuild\": \"${TemplateVersions.ESBUILD}\""))
    }

    @Test
    fun `build script chains rescript and bundle`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"build\""))
        assertTrue(pkg.contains("rescript &&"))
    }

    @Test
    fun `README documents lambda deploy notes`() {
        val readme = AwsLambdaTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("index.handler"))
        assertTrue(readme.contains("Lambda") || readme.contains("lambda"))
    }

    @Test
    fun `server ships POST + path param orders endpoints`() {
        val server = AwsLambdaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Hono.post"))
        assertTrue(server.contains("/orders"))
        assertTrue(server.contains("paramAt"))
        assertTrue(server.contains("jsonBody"))
    }

    @Test
    fun `server validates POST body via Validation and returns 400 on Error`() {
        val server = AwsLambdaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Validation.parseCreateOrderPayload"))
        assertTrue(server.contains("Hono.status(400)"))
    }

    @Test
    fun `zod variant ships zod Validation module and zod dependency`() {
        val files = AwsLambdaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Validation.res"))
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseCreateOrderPayload"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury Validation module and sury dependency`() {
        val files = AwsLambdaTemplateFiles.generate(suryCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        assertFalse(validation.contains("@module(\"zod\")"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `package json declares test script and vitest devDep`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test\": \"vitest run\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
    }

    @Test
    fun `ships a vitest smoke test`() {
        val files = AwsLambdaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/__tests__/Server.test.mjs"))
        assertTrue(files["src/__tests__/Server.test.mjs"]!!.contains("import(\"../Server.res.mjs\")"))
    }

    @Test
    fun `README includes DynamoDB recipe`() {
        val readme = AwsLambdaTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("DynamoDB"))
        assertTrue(readme.contains("@aws-sdk/lib-dynamodb"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = AwsLambdaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("fn"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = AwsLambdaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }
}
