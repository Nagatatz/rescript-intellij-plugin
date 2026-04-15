package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ViteReactTemplateFilesTest {
    private val ctx = TemplateContext("demo", PackageManager.PNPM)

    @Test
    fun `vite config imports defineConfig from vite-plus`() {
        val cfg = ViteReactTemplateFiles.generate(ctx)["vite.config.mjs"]!!
        assertTrue(cfg.contains("from \"vite-plus\""))
        assertTrue(cfg.contains("react()"))
    }

    @Test
    fun `package json declares vite-plus dependencies`() {
        val pkg = ViteReactTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"vite-plus\""))
        assertTrue(pkg.contains("\"@voidzero-dev/vite-plus-core\""))
        assertTrue(pkg.contains("\"vitest\""))
    }

    @Test
    fun `npm scripts use vp dev, vp build, vp test`() {
        val pkg = ViteReactTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"dev\": \"vp dev\""))
        assertTrue(pkg.contains("\"build\": \"vp build\""))
        assertTrue(pkg.contains("\"test\": \"vp test\""))
    }

    @Test
    fun `template includes README, gitignore, editorconfig, CI, and a vitest sample`() {
        val files = ViteReactTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("src/__tests__/App.test.mjs"))
    }

    @Test
    fun `README warns about Vite Plus pre-1 status`() {
        val readme = ViteReactTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("Vite+"))
        assertTrue(readme.contains("pre-1.0"))
    }

    @Test
    fun `rescript json enables JSX v4 and depends on rescript-react`() {
        val rj = ViteReactTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("\"jsx\""))
        assertTrue(rj.contains("@rescript/react"))
    }

    @Test
    fun `App demonstrates useState + form + fetch`() {
        val files = ViteReactTemplateFiles.generate(ctx)
        val app = files["src/App.res"]!!
        assertTrue(files.containsKey("src/Api.res"))
        assertTrue(app.contains("React.useState"))
        assertTrue(app.contains("onSubmit"))
        assertTrue(app.contains("Api.greet"))
        assertTrue(files["src/Api.res"]!!.contains("fetch"))
        assertTrue(files["src/Api.res"]!!.contains("/api/greet"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = ViteReactTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("demo"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = ViteReactTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }
}
