package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NextjsTemplateFilesTest {
    private val ctx = TemplateContext("site", PackageManager.PNPM)
    private val suryCtx = TemplateContext("site", PackageManager.PNPM, ValidationLibrary.SURY)

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
    fun `route ts is a one-line re-export shim pointing at GreetRoute`() {
        val files = NextjsTemplateFiles.generate(ctx)
        val route = files["src/app/api/greet/route.ts"]!!
        assertTrue(route.contains("GreetRoute.res.mjs"))
        assertTrue(route.contains("post as POST"))
    }

    @Test
    fun `NextServer bindings and GreetRoute handler ship in ReScript`() {
        val files = NextjsTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/NextServer.res"))
        assertTrue(files.containsKey("src/app/api/greet/GreetRoute.res"))
        val greetRoute = files["src/app/api/greet/GreetRoute.res"]!!
        assertTrue(greetRoute.contains("Validation.parseGreetInput"))
        assertTrue(greetRoute.contains("NextServer.jsonResponseWithInit"))
    }

    @Test
    fun `zod variant ships zod Validation module and zod dependency`() {
        val files = NextjsTemplateFiles.generate(ctx)
        val validation = files["src/app/api/greet/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseGreetInput"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\": \"${TemplateVersions.ZOD}\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships sury Validation module and sury dependency`() {
        val files = NextjsTemplateFiles.generate(suryCtx)
        val validation = files["src/app/api/greet/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `GreetForm res uses useState and posts to slash api slash greet`() {
        val form = NextjsTemplateFiles.generate(ctx)["src/GreetForm.res"]!!
        assertTrue(form.contains("React.useState"))
        assertTrue(form.contains("/api/greet"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = NextjsTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("site"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = NextjsTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `ships strict tsconfig with Next plugin and bundler module resolution`() {
        val files = NextjsTemplateFiles.generate(ctx)
        val tsconfig = files["tsconfig.json"]!!
        assertTrue(tsconfig.contains("\"strict\": true"))
        assertTrue(tsconfig.contains("\"noImplicitAny\": true"))
        assertTrue(tsconfig.contains("\"moduleResolution\": \"bundler\""))
        assertTrue(tsconfig.contains("\"name\": \"next\""))
        assertTrue(files.containsKey("rescript-modules.d.ts"))
        assertTrue(files["rescript-modules.d.ts"]!!.contains("*.res.mjs"))
    }

    @Test
    fun `devDependencies declare typescript, @types react, @types react-dom, @types node`() {
        val pkg = NextjsTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"typescript\": \"${TemplateVersions.TYPESCRIPT}\""))
        assertTrue(pkg.contains("\"@types/react\": \"${TemplateVersions.REACT_TYPES}\""))
        assertTrue(pkg.contains("\"@types/react-dom\": \"${TemplateVersions.REACT_DOM_TYPES}\""))
        assertTrue(pkg.contains("\"@types/node\": \"${TemplateVersions.NODE_TYPES}\""))
    }
}
