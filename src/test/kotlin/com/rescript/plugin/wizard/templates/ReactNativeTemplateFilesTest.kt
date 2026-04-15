package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
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
}
