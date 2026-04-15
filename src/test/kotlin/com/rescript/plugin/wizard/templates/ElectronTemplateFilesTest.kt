package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
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
}
