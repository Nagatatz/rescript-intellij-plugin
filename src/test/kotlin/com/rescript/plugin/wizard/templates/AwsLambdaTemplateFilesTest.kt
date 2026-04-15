package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AwsLambdaTemplateFilesTest {
    private val ctx = TemplateContext("fn", PackageManager.PNPM)

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
}
