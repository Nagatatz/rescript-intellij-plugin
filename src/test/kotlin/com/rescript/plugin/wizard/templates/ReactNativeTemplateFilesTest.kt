package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReactNativeTemplateFilesTest {
    private val ctx = TemplateContext("mobile", PackageManager.PNPM)

    @Test
    fun `package json includes expo and react-native pinned via TemplateVersions`() {
        val pkg = ReactNativeTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"expo\": \"${TemplateVersions.EXPO}\""))
        assertTrue(pkg.contains("\"react-native\": \"${TemplateVersions.REACT_NATIVE}\""))
    }

    @Test
    fun `template includes README, gitignore, editorconfig, and CI`() {
        val files = ReactNativeTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("App.tsx"))
        assertTrue(files.containsKey("app.json"))
    }

    @Test
    fun `app json substitutes project name`() {
        val appJson = ReactNativeTemplateFiles.generate(ctx)["app.json"]!!
        assertTrue(appJson.contains("\"name\": \"mobile\""))
        assertTrue(appJson.contains("\"slug\": \"mobile\""))
    }

    @Test
    fun `App res demonstrates useState + FlatList + TextInput`() {
        val app = ReactNativeTemplateFiles.generate(ctx)["src/App.res"]!!
        assertTrue(app.contains("React.useState"))
        assertTrue(app.contains("ReactNative.FlatList"))
        assertTrue(app.contains("ReactNative.TextInput"))
        assertTrue(app.contains("ReactNative.Button"))
    }

    @Test
    fun `package json declares test script and vitest devDep`() {
        val pkg = ReactNativeTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test\": \"vitest run\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
    }

    @Test
    fun `ships a source-level smoke test`() {
        val files = ReactNativeTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/__tests__/App.test.mjs"))
        val test = files["src/__tests__/App.test.mjs"]!!
        assertTrue(
            test.contains("existsSync"),
            "RN test should avoid loading react-native and check filesystem instead",
        )
    }

    @Test
    fun `ReactNative bindings include FlatList, TextInput, Button, Style`() {
        val bindings = ReactNativeTemplateFiles.generate(ctx)["src/ReactNative.res"]!!
        assertTrue(bindings.contains("module FlatList"))
        assertTrue(bindings.contains("module TextInput"))
        assertTrue(bindings.contains("module Button"))
        assertTrue(bindings.contains("module Style"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = ReactNativeTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("mobile"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = ReactNativeTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `zod variant ships Validation res and pins the zod dependency`() {
        val zodCtx = ctx.copy(validationLibrary = ValidationLibrary.ZOD)
        val files = ReactNativeTemplateFiles.generate(zodCtx)
        assertTrue(files["src/Validation.res"]!!.contains("@module(\"zod\")"))
        assertTrue(files["src/Validation.res"]!!.contains("parseDraftTodo"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships Validation res and pins the sury dependency`() {
        val suryCtx = ctx.copy(validationLibrary = ValidationLibrary.SURY)
        val files = ReactNativeTemplateFiles.generate(suryCtx)
        assertTrue(files["src/Validation.res"]!!.contains("S.parseOrThrow"))
        assertTrue(files["src/Validation.res"]!!.contains("parseDraftTodo"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\""))
        assertFalse(pkg.contains("\"zod\""))
    }

    @Test
    fun `App res validates drafts through Validation parseDraftTodo`() {
        val app = ReactNativeTemplateFiles.generate(ctx)["src/App.res"]!!
        assertTrue(app.contains("Validation.parseDraftTodo"))
    }
}
