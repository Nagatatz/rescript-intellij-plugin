package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TauriTemplateFilesTest {
    private val ctx = TemplateContext("tauri-app", PackageManager.PNPM)

    @Test
    fun `package json declares rescript-tauri core and tauri-apps deps`() {
        val pkg = TauriTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"@rescript-tauri/core\""))
        assertTrue(pkg.contains("\"@tauri-apps/api\""))
        assertTrue(pkg.contains("\"@tauri-apps/cli\""))
    }

    @Test
    fun `rescript json lists rescript-tauri core under dependencies`() {
        val rj = TauriTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("\"@rescript-tauri/core\""))
        assertTrue(rj.contains("\"@rescript/react\""))
        assertTrue(rj.contains("\"jsx\""))
    }

    @Test
    fun `package json exposes tauri dev and build scripts`() {
        val pkg = TauriTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"tauri\": \"tauri\""))
        assertTrue(pkg.contains("\"tauri:dev\": \"tauri dev\""))
        assertTrue(pkg.contains("\"tauri:build\": \"tauri build\""))
        assertTrue(pkg.contains("\"dev\": \"vp dev\""))
        assertTrue(pkg.contains("\"build\": \"vp build\""))
    }

    @Test
    fun `template ships rust skeleton in src-tauri folder`() {
        val files = TauriTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src-tauri/Cargo.toml"))
        assertTrue(files.containsKey("src-tauri/build.rs"))
        assertTrue(files.containsKey("src-tauri/src/main.rs"))
        assertTrue(files.containsKey("src-tauri/tauri.conf.json"))
        assertTrue(files.containsKey("src-tauri/.gitignore"))
        assertTrue(files["src-tauri/Cargo.toml"]!!.contains("tauri-build"))
        assertTrue(files["src-tauri/src/main.rs"]!!.contains("#[tauri::command]"))
        assertTrue(files["src-tauri/src/main.rs"]!!.contains("generate_handler![greet, get_info]"))
    }

    @Test
    fun `tauri conf json substitutes project name and PM-aware before commands for pnpm`() {
        val conf = TauriTemplateFiles.generate(ctx)["src-tauri/tauri.conf.json"]!!
        assertTrue(conf.contains("\"productName\": \"tauri-app\""))
        // pnpm => "pnpm dev" / "pnpm build"
        assertTrue(conf.contains("\"beforeDevCommand\": \"pnpm dev\""))
        assertTrue(conf.contains("\"beforeBuildCommand\": \"pnpm build\""))
        assertTrue(conf.contains("\"devUrl\": \"http://localhost:5173\""))
        assertTrue(conf.contains("\"frontendDist\": \"../dist\""))
        // CSP must surface the ipc: + asset: rules that Tauri 2.x requires.
        assertTrue(conf.contains("default-src 'self'"))
        assertTrue(conf.contains("connect-src ipc:"))
    }

    @Test
    fun `tauri conf json adapts before commands when npm is selected`() {
        val npmCtx = ctx.copy(packageManager = PackageManager.NPM)
        val conf = TauriTemplateFiles.generate(npmCtx)["src-tauri/tauri.conf.json"]!!
        assertTrue(conf.contains("\"beforeDevCommand\": \"npm run dev\""))
        assertTrue(conf.contains("\"beforeBuildCommand\": \"npm run build\""))
    }

    @Test
    fun `tauri conf json adapts before commands when bun is selected`() {
        val bunCtx = ctx.copy(packageManager = PackageManager.BUN)
        val conf = TauriTemplateFiles.generate(bunCtx)["src-tauri/tauri.conf.json"]!!
        assertTrue(conf.contains("\"beforeDevCommand\": \"bun run dev\""))
        assertTrue(conf.contains("\"beforeBuildCommand\": \"bun run build\""))
    }

    @Test
    fun `tauri conf json adapts before commands when yarn is selected`() {
        val yarnCtx = ctx.copy(packageManager = PackageManager.YARN)
        val conf = TauriTemplateFiles.generate(yarnCtx)["src-tauri/tauri.conf.json"]!!
        assertTrue(conf.contains("\"beforeDevCommand\": \"yarn dev\""))
        assertTrue(conf.contains("\"beforeBuildCommand\": \"yarn build\""))
    }

    @Test
    fun `cargo toml uses project name as crate name`() {
        val cargo = TauriTemplateFiles.generate(ctx)["src-tauri/Cargo.toml"]!!
        assertTrue(cargo.contains("name = \"tauri-app\""))
    }

    @Test
    fun `zod variant ships Validation res and pins the zod dependency`() {
        val zodCtx = ctx.copy(validationLibrary = ValidationLibrary.ZOD)
        val files = TauriTemplateFiles.generate(zodCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseInfo"))
        // The struct exposed by Rust is { name, version, platform, arch } — not Electron's electronVersion.
        assertTrue(validation.contains("version: string"))
        assertFalse(validation.contains("electronVersion"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"zod\""))
        assertFalse(pkg.contains("\"sury\""))
    }

    @Test
    fun `sury variant ships Validation res and pins the sury dependency`() {
        val suryCtx = ctx.copy(validationLibrary = ValidationLibrary.SURY)
        val files = TauriTemplateFiles.generate(suryCtx)
        val validation = files["src/Validation.res"]!!
        assertTrue(validation.contains("S.parseOrThrow"))
        assertTrue(validation.contains("parseInfo"))
        assertTrue(validation.contains("version: string"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"sury\""))
        assertFalse(pkg.contains("\"zod\""))
    }

    @Test
    fun `App res wires Tauri invoke through Validation parseInfo`() {
        val files = TauriTemplateFiles.generate(ctx)
        val tauriBindings = files["src/Tauri.res"]!!
        assertTrue(tauriBindings.contains("RescriptTauriCore.Core.Raw.invoke"))
        assertTrue(tauriBindings.contains("getInfoRaw"))
        val app = files["src/App.res"]!!
        assertTrue(app.contains("Tauri.getInfoRaw"))
        assertTrue(app.contains("Validation.parseInfo"))
        assertTrue(app.contains("Tauri.greet"))
    }

    @Test
    fun `README ships IPC and Production sections referencing the security model`() {
        val readme = TauriTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("Renderer ↔ Rust IPC"))
        assertTrue(readme.contains("Production Bundling"))
        assertTrue(readme.contains("invoke_handler"))
        assertTrue(readme.contains("Adding a new Tauri command"))
        assertTrue(readme.contains("tauri icon"))
        // Prerequisites must mention Rust + platform deps.
        assertTrue(readme.contains("Rust toolchain"))
    }

    @Test
    fun `template includes README, gitignore, editorconfig, CI`() {
        val files = TauriTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey(".nvmrc"))
        assertTrue(files.containsKey("LICENSE"))
        assertTrue(files.containsKey(".github/dependabot.yml"))
    }

    @Test
    fun `gitignore excludes rust build artifacts at the project root`() {
        val gitignore = TauriTemplateFiles.generate(ctx)[".gitignore"]!!
        assertTrue(gitignore.contains("src-tauri/target/"))
        assertTrue(gitignore.contains("dist/"))
    }

    @Test
    fun `ships a vitest smoke test`() {
        val files = TauriTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/__tests__/App.test.mjs"))
        assertTrue(files["src/__tests__/App.test.mjs"]!!.contains("import(\"../App.res.mjs\")"))
    }

    @Test
    fun `package json declares test coverage script and provider`() {
        val pkg = TauriTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"test:coverage\""))
        assertTrue(pkg.contains("\"@vitest/coverage-v8\""))
    }
}
