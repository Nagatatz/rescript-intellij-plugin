package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElectronTemplateFilesTest {
    private val ctx = TemplateContext("desk-app", PackageManager.PNPM)

    @Test
    fun `vite config imports defineConfig from vite-plus`() {
        val cfg = ElectronTemplateFiles.generate(ctx)["vite.config.mjs"]!!
        assertTrue(cfg.contains("from \"vite-plus\""))
        assertTrue(cfg.contains("base: \"./\""))
    }

    @Test
    fun `package json declares electron and vite-plus dev deps with vp scripts`() {
        val pkg = ElectronTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"electron\""))
        assertTrue(pkg.contains("\"vite-plus\""))
        assertTrue(pkg.contains("\"dev\": \"vp dev\""))
        assertTrue(pkg.contains("\"start\": \"vp build && electron .\""))
    }

    @Test
    fun `template includes README, gitignore, editorconfig, CI`() {
        val files = ElectronTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey("main.cjs"))
    }

    @Test
    fun `package json declares test script and vitest devDep`() {
        val pkg = ElectronTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test\": \"vp test\""))
        assertTrue(pkg.contains("\"vitest\": \"${TemplateVersions.VITEST}\""))
    }

    @Test
    fun `ships a vitest smoke test`() {
        val files = ElectronTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/__tests__/App.test.mjs"))
        assertTrue(files["src/__tests__/App.test.mjs"]!!.contains("import(\"../App.res.mjs\")"))
    }

    @Test
    fun `wires IPC through preload and renderer bindings`() {
        val files = ElectronTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("preload.cjs"))
        assertTrue(files.containsKey("src/Electron.res"))
        assertTrue(files["preload.cjs"]!!.contains("contextBridge"))
        assertTrue(files["preload.cjs"]!!.contains("ipcRenderer.invoke"))
        assertTrue(files["main.cjs"]!!.contains("ipcMain.handle"))
        assertTrue(files["main.cjs"]!!.contains("preload: path.join"))
        assertTrue(files["src/Electron.res"]!!.contains("electronAPI"))
        assertTrue(files["src/App.res"]!!.contains("Electron.getInfo"))
    }

    @Test
    fun `ships nvmrc, LICENSE, and dependabot config`() {
        val files = ElectronTemplateFiles.generate(ctx)
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
        assertTrue(files[".nvmrc"]!!.contains(TemplateVersions.NODE_MAJOR))
        assertTrue(files["LICENSE"]!!.contains("MIT License"))
        assertTrue(files["LICENSE"]!!.contains("desk-app"))
        assertTrue(files[".github/dependabot.yml"]!!.contains("package-ecosystem: \"npm\""))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = ElectronTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }

    @Test
    fun `zod variant ships Validation res and pins the zod dependency`() {
        val zodCtx = ctx.copy(validationLibrary = ValidationLibrary.ZOD)
        val files = ElectronTemplateFiles.generate(zodCtx)
        assertTrue(files["src/Validation.res"]!!.contains("@module(\"zod\")"))
        assertTrue(files["src/Validation.res"]!!.contains("parseInfo"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships Validation res and pins the sury dependency`() {
        val suryCtx = ctx.copy(validationLibrary = ValidationLibrary.SURY)
        val files = ElectronTemplateFiles.generate(suryCtx)
        assertTrue(files["src/Validation.res"]!!.contains("S.parseOrThrow"))
        assertTrue(files["src/Validation.res"]!!.contains("parseInfo"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\""))
        assertFalse(pkg.contains("\"zod\""))
    }

    @Test
    fun `App res validates IPC responses through Validation parseInfo`() {
        val files = ElectronTemplateFiles.generate(ctx)
        assertTrue(files["src/Electron.res"]!!.contains("getInfoRaw"))
        assertTrue(files["src/Electron.res"]!!.contains("JSON.t"))
        assertTrue(files["src/App.res"]!!.contains("Validation.parseInfo"))
        assertTrue(files["src/App.res"]!!.contains("Electron.getInfoRaw"))
    }

    @Test
    fun `README ships an IPC section documenting the security model and channel naming`() {
        val readme = ElectronTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("Renderer ↔ Main IPC"))
        assertTrue(readme.contains("contextIsolation"))
        assertTrue(readme.contains("nodeIntegration"))
        assertTrue(readme.contains("contextBridge"))
        // The naming-convention guide must call out the domain:action shape.
        assertTrue(readme.contains("domain:action"))
        // Step-by-step "add a new channel" walkthrough must touch all three layers.
        assertTrue(readme.contains("Adding a new IPC channel"))
        assertTrue(readme.contains("main.cjs"))
        assertTrue(readme.contains("preload.cjs"))
        assertTrue(readme.contains("Electron.res"))
    }
}
