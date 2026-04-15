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
        assertTrue(readme.contains("dist/index.mjs"))
        assertTrue(readme.contains("Lambda"))
    }
}
