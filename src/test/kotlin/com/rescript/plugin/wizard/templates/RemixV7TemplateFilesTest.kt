package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RemixV7TemplateFilesTest {
    private val ctx = TemplateContext("rrapp", PackageManager.PNPM)

    @Test
    fun `package json includes react-router and the dev plugin`() {
        val pkg = RemixV7TemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"react-router\": \"${TemplateVersions.REACT_ROUTER}\""))
        assertTrue(pkg.contains("\"@react-router/dev\": \"${TemplateVersions.REACT_ROUTER_DEV}\""))
        assertTrue(pkg.contains("\"@react-router/node\": \"${TemplateVersions.REACT_ROUTER_NODE}\""))
        assertTrue(pkg.contains("\"@react-router/serve\": \"${TemplateVersions.REACT_ROUTER_SERVE}\""))
        assertTrue(pkg.contains("\"packageManager\""))
        assertTrue(pkg.contains("\"type\": \"module\""))
    }

    @Test
    fun `template includes README, gitignore, editorconfig, CI, vite config and routes config`() {
        val files = RemixV7TemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("vite.config.ts"))
        assertTrue(files.containsKey("react-router.config.ts"))
        assertTrue(files.containsKey("tsconfig.json"))
    }

    @Test
    fun `routes config mounts the home file as the index route`() {
        val routes = RemixV7TemplateFiles.generate(ctx)["app/routes.ts"]!!
        assertTrue(routes.contains("index(\"routes/home.tsx\")"))
        assertTrue(routes.contains("@react-router/dev/routes"))
    }

    @Test
    fun `home route consumes the ReScript loader and component`() {
        val home = RemixV7TemplateFiles.generate(ctx)["app/routes/home.tsx"]!!
        assertTrue(home.contains("../components/Greet.res.mjs"))
        assertTrue(home.contains("../loaders/HomeLoader.res.mjs"))
        assertTrue(home.contains("\"rrapp\""))
    }

    @Test
    fun `ReScript Greet component and HomeLoader live under app`() {
        val files = RemixV7TemplateFiles.generate(ctx)
        assertTrue(files["app/components/Greet.res"]!!.contains("@react.component"))
        assertTrue(files["app/loaders/HomeLoader.res"]!!.contains("homeLoader"))
    }

    @Test
    fun `gitignore covers the React Router build folder`() {
        val gitignore = RemixV7TemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains(".react-router/"))
        assertTrue(gitignore.contains("build/"))
    }

    @Test
    fun `rescript json points sources at the app directory and enables JSX`() {
        val rj = RemixV7TemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("\"dir\": \"app\""))
        assertTrue(rj.contains("\"jsx\""))
    }

    @Test
    fun `template never emits a Validation res because validation selection is disabled`() {
        val files = RemixV7TemplateFiles.generate(ctx)
        assertFalse(files.keys.any { it.endsWith("Validation.res") })
    }
}
