package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TanstackStartTemplateFilesTest {
    private val ctx = TemplateContext("startapp", PackageManager.PNPM)

    @Test
    fun `package json includes TanStack Start, router and packageManager metadata`() {
        val pkg = TanstackStartTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"@tanstack/react-start\": \"${TemplateVersions.TANSTACK_REACT_START}\""))
        assertTrue(pkg.contains("\"@tanstack/react-router\": \"${TemplateVersions.TANSTACK_REACT_ROUTER}\""))
        assertTrue(pkg.contains("\"@tanstack/router-plugin\": \"${TemplateVersions.TANSTACK_ROUTER_PLUGIN}\""))
        assertTrue(pkg.contains("\"vite\""))
        assertTrue(pkg.contains("\"@vitejs/plugin-react\""))
        assertTrue(pkg.contains("\"vitest\""))
        assertTrue(pkg.contains("\"packageManager\""))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI, vite config, and tsconfig`() {
        val files = TanstackStartTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("vite.config.ts"))
        assertTrue(files.containsKey("tsconfig.json"))
        assertTrue(files.containsKey("rescript-modules.d.ts"))
    }

    @Test
    fun `routes directory exposes a root layout and an index route`() {
        val files = TanstackStartTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("app/routes/__root.tsx"))
        assertTrue(files.containsKey("app/routes/index.tsx"))
        assertTrue(files["app/routes/__root.tsx"]!!.contains("createRootRoute"))
        assertTrue(files["app/routes/index.tsx"]!!.contains("createFileRoute"))
    }

    @Test
    fun `index route reads the project name from the loader`() {
        val index = TanstackStartTemplateFiles.generate(ctx)["app/routes/index.tsx"]!!
        // Loader greets with the project name baked into the loader call.
        assertTrue(index.contains("\"startapp\""))
    }

    @Test
    fun `ReScript Greeting component is shipped under app components`() {
        val files = TanstackStartTemplateFiles.generate(ctx)
        val greeting =
            files["app/components/Greeting.res"]
                ?: error("Greeting.res missing")
        assertTrue(greeting.contains("@react.component"))
        assertTrue(greeting.contains("let make"))
    }

    @Test
    fun `Server Function sample uses createServerFn from TanStack Start`() {
        val server =
            TanstackStartTemplateFiles.generate(ctx)["app/server/Greet.res"]
                ?: error("Greet.res missing")
        assertTrue(server.contains("createServerFn"))
        assertTrue(server.contains("@module(\"@tanstack/react-start\")"))
    }

    @Test
    fun `gitignore covers TanStack and Nitro build folders`() {
        val gitignore = TanstackStartTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains(".tanstack/"))
        assertTrue(gitignore.contains(".output/"))
        assertTrue(gitignore.contains(".nitro/"))
    }

    @Test
    fun `rescript json points sources at the app directory and enables JSX`() {
        val rj = TanstackStartTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("\"dir\": \"app\""))
        assertTrue(rj.contains("\"jsx\""))
    }

    @Test
    fun `template never emits a Validation res because validation selection is disabled`() {
        val files = TanstackStartTemplateFiles.generate(ctx)
        assertFalse(files.keys.any { it.endsWith("Validation.res") })
    }

    @Test
    fun `legacy generate by name still works`() {
        val files = TanstackStartTemplateFiles.generate("startapp")
        assertEquals(true, files.containsKey("package.json"))
    }
}
