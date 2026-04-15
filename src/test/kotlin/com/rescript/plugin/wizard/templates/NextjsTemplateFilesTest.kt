package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NextjsTemplateFilesTest {
    private val ctx = TemplateContext("site", PackageManager.PNPM)

    @Test
    fun `package json includes Next, vitest, and packageManager metadata`() {
        val pkg = NextjsTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"next\": \"${TemplateVersions.NEXTJS}\""))
        assertTrue(pkg.contains("\"vitest\""))
        assertTrue(pkg.contains("\"packageManager\""))
    }

    @Test
    fun `template includes README, gitignore, editorconfig, CI, and a vitest sample`() {
        val files = NextjsTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("src/__tests__/App.test.mjs"))
    }

    @Test
    fun `gitignore covers the Next dot next folder`() {
        val gitignore = NextjsTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains(".next/"))
    }

    @Test
    fun `ships Server Component, Client Component, and POST route handler`() {
        val files = NextjsTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/app/page.tsx"))
        assertTrue(files.containsKey("src/app/client/GreetForm.tsx"))
        assertTrue(files.containsKey("src/app/api/greet/route.ts"))
        assertTrue(files["src/app/client/GreetForm.tsx"]!!.contains("\"use client\""))
        assertTrue(files["src/app/api/greet/route.ts"]!!.contains("POST"))
    }

    @Test
    fun `GreetForm res uses useState and posts to slash api slash greet`() {
        val form = NextjsTemplateFiles.generate(ctx)["src/GreetForm.res"]!!
        assertTrue(form.contains("React.useState"))
        assertTrue(form.contains("/api/greet"))
    }
}
