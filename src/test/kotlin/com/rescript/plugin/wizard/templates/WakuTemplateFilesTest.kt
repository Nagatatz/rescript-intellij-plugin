package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WakuTemplateFilesTest {
    private val ctx = TemplateContext("wakuapp", PackageManager.PNPM)

    @Test
    fun `package json includes Waku and packageManager metadata`() {
        val pkg = WakuTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"waku\": \"${TemplateVersions.WAKU}\""))
        assertTrue(pkg.contains("\"vitest\""))
        assertTrue(pkg.contains("\"packageManager\""))
        assertTrue(pkg.contains("\"type\": \"module\""))
    }

    @Test
    fun `template ships README, gitignore, editorconfig, CI and tsconfig`() {
        val files = WakuTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("tsconfig.json"))
    }

    @Test
    fun `home page is a Server Component that embeds the Greet ReScript module`() {
        val page = WakuTemplateFiles.generate(ctx)["src/pages/index.tsx"]!!
        assertTrue(page.contains("../components/Greet.res.mjs"))
        assertTrue(page.contains("\"wakuapp\""))
        assertTrue(page.contains("CounterClient"))
        // Must not declare a "use client" directive — pages are Server Components.
        assertFalse(page.startsWith("\"use client\""))
    }

    @Test
    fun `Greet Server Component and Counter Client Component body are written in ReScript`() {
        val files = WakuTemplateFiles.generate(ctx)
        assertTrue(files["src/components/Greet.res"]!!.contains("@react.component"))
        assertTrue(files["src/components/Counter.res"]!!.contains("@react.component"))
    }

    @Test
    fun `CounterClient TSX wrapper declares the use client boundary`() {
        val wrapper =
            WakuTemplateFiles.generate(ctx)["src/components/CounterClient.tsx"]
                ?: error("CounterClient.tsx missing")
        assertTrue(wrapper.startsWith("\"use client\""))
        assertTrue(wrapper.contains("./Counter.res.mjs"))
    }

    @Test
    fun `gitignore covers the Waku build folder`() {
        val gitignore = WakuTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains(".waku/"))
        assertTrue(gitignore.contains("dist/"))
    }

    @Test
    fun `template never emits a Validation res because validation selection is disabled`() {
        val files = WakuTemplateFiles.generate(ctx)
        assertFalse(files.keys.any { it.endsWith("Validation.res") })
    }
}
