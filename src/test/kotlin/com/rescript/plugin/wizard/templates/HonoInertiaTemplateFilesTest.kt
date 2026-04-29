package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HonoInertiaTemplateFilesTest {
    private val ctx = TemplateContext("svc", PackageManager.PNPM)
    private val suryCtx = TemplateContext("svc", PackageManager.PNPM, ValidationLibrary.SURY)

    @Test
    fun `package json declares hono, inertia, react, and Vite+ trio`() {
        val pkg = HonoInertiaTemplateFiles.generate(ctx)["package.json"]!!
        assertTrue(pkg.contains("\"hono\": \"${TemplateVersions.HONO}\""))
        assertTrue(pkg.contains("\"@hono/node-server\": \"${TemplateVersions.HONO_NODE_SERVER}\""))
        assertTrue(pkg.contains("\"@hono/inertia\": \"${TemplateVersions.HONO_INERTIA}\""))
        assertTrue(pkg.contains("\"@inertiajs/react\": \"${TemplateVersions.INERTIA_REACT}\""))
        assertTrue(pkg.contains("\"react\": \"${TemplateVersions.REACT}\""))
        assertTrue(pkg.contains("\"react-dom\": \"${TemplateVersions.REACT_DOM}\""))
        assertTrue(pkg.contains("\"@rescript/react\": \"${TemplateVersions.RESCRIPT_REACT}\""))
        assertTrue(pkg.contains("\"vite\": \"${TemplateVersions.VITE}\""))
        assertTrue(pkg.contains("\"vite-plus\": \"${TemplateVersions.VITE_PLUS}\""))
        assertTrue(pkg.contains("\"@voidzero-dev/vite-plus-core\": \"${TemplateVersions.VITE_PLUS_CORE}\""))
    }

    @Test
    fun `package json scripts use Vite+ vp commands and omit standalone vitest, eslint, prettier`() {
        val pkg = HonoInertiaTemplateFiles.generate(ctx)["package.json"]!!
        // Vite+ unified scripts (the whole point of choosing this stack).
        assertTrue(pkg.contains("\"dev\": \"vp dev\""))
        assertTrue(pkg.contains("\"build\": \"vp build\""))
        assertTrue(pkg.contains("\"test\": \"vp test\""))
        assertTrue(pkg.contains("\"check\": \"vp check\""))
        // Standalone test/lint/format toolchains must NOT be re-introduced.
        assertFalse(pkg.contains("\"vitest\""))
        assertFalse(pkg.contains("\"eslint\""))
        assertFalse(pkg.contains("\"prettier\""))
    }

    @Test
    fun `template ships rescript, vite, drizzle, README, and CI scaffolding`() {
        val files = HonoInertiaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("rescript.json"))
        assertTrue(files.containsKey("vite.config.mjs"))
        assertTrue(files.containsKey("drizzle.config.ts"))
        assertTrue(files.containsKey("index.html"))
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey(".env.example"))
    }

    @Test
    fun `rescript json enables JSX so Inertia React pages compile`() {
        val rj = HonoInertiaTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("\"jsx\""))
        assertTrue(rj.contains("@rescript/react"))
    }

    @Test
    fun `vite config uses Vite+ defineConfig and registers the inertiaPages plugin`() {
        val cfg = HonoInertiaTemplateFiles.generate(ctx)["vite.config.mjs"]!!
        assertTrue(cfg.contains("from \"vite-plus\""))
        assertTrue(cfg.contains("inertiaPages"))
        assertTrue(cfg.contains("@vitejs/plugin-react"))
    }

    @Test
    fun `server wires the Inertia middleware before defining routes`() {
        val server = HonoInertiaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("HonoInertia.inertia()"))
        assertTrue(server.contains("Routes.Pages.register(app)"))
        // Middleware order is critical: inertia() must precede route registration.
        val inertiaIdx = server.indexOf("HonoInertia.inertia()")
        val routesIdx = server.indexOf("Routes.Pages.register(app)")
        assertTrue(inertiaIdx in 0 until routesIdx)
    }

    @Test
    fun `routes call HonoInertia render with named pages and structured props`() {
        val routes = HonoInertiaTemplateFiles.generate(ctx)["src/Routes.res"]!!
        assertTrue(routes.contains("HonoInertia.render(") && routes.contains("\"Home\""))
        assertTrue(routes.contains("\"About\""))
        assertTrue(routes.contains("Validation.parseGreetForm"))
    }

    @Test
    fun `inertia bindings expose createInertiaApp, Link, and usePage`() {
        val bindings = HonoInertiaTemplateFiles.generate(ctx)["src/InertiaBindings.res"]!!
        assertTrue(bindings.contains("@module(\"@inertiajs/react\")"))
        assertTrue(bindings.contains("createInertiaApp"))
        assertTrue(bindings.contains("module Link"))
        assertTrue(bindings.contains("external usePage"))
    }

    @Test
    fun `pages js shim uses import meta glob and re-exports under default`() {
        val shim = HonoInertiaTemplateFiles.generate(ctx)["src/client/pages.js"]!!
        assertTrue(shim.contains("import.meta.glob"))
        assertTrue(shim.contains("./Pages/"))
        // Inertia reads `.default`, ReScript exports `make` — the shim bridges them.
        assertTrue(shim.contains("default"))
        assertTrue(shim.contains("make"))
    }

    @Test
    fun `client Main wires resolvePage into createInertiaApp setup`() {
        val main = HonoInertiaTemplateFiles.generate(ctx)["src/client/Main.res"]!!
        assertTrue(main.contains("@module(\"./pages.js\")"))
        assertTrue(main.contains("InertiaBindings.createInertiaApp"))
        assertTrue(main.contains("ReactDOM.Client.createRoot"))
    }

    @Test
    fun `ships sample Home and About pages plus a shared layout`() {
        val files = HonoInertiaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/client/MainLayout.res"))
        assertTrue(files.containsKey("src/client/Pages/Home.res"))
        assertTrue(files.containsKey("src/client/Pages/About.res"))
        assertTrue(files["src/client/MainLayout.res"]!!.contains("InertiaBindings.usePage"))
        assertTrue(files["src/client/Pages/Home.res"]!!.contains("@react.component"))
    }

    @Test
    fun `ships Drizzle schema, libsql client, and drizzle config`() {
        val files = HonoInertiaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/Schema.res"))
        assertTrue(files.containsKey("src/Db.res"))
        assertTrue(files.containsKey("drizzle.config.ts"))
        assertTrue(files["src/Schema.res"]!!.contains("sqliteTable"))
        assertTrue(files["src/Db.res"]!!.contains("@libsql/client"))
        assertTrue(files["drizzle.config.ts"]!!.contains("dialect: \"sqlite\""))
    }

    @Test
    fun `zod variant ships a zod-based Validation module`() {
        val validation = HonoInertiaTemplateFiles.generate(ctx)["src/Validation.res"]!!
        assertTrue(validation.contains("@module(\"zod\")"))
        assertTrue(validation.contains("parseGreetForm"))
    }

    @Test
    fun `sury variant ships a sury-based Validation module`() {
        val validation = HonoInertiaTemplateFiles.generate(suryCtx)["src/Validation.res"]!!
        assertTrue(validation.contains("S.object"))
        assertTrue(validation.contains("S.parseOrThrow"))
        assertTrue(validation.contains("parseGreetForm"))
        assertFalse(validation.contains("@module(\"zod\")"))
    }

    @Test
    fun `package json swaps zod for sury when sury is selected`() {
        val pkg = HonoInertiaTemplateFiles.generate(suryCtx)["package.json"]!!
        assertTrue(pkg.contains("\"sury\": \"${TemplateVersions.SURY}\""))
        assertFalse(pkg.contains("\"zod\":"))
    }

    @Test
    fun `Server res keeps serve inside a start function so tests can import without binding a port`() {
        val server = HonoInertiaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("let start = () => {"))
        val topLevelLines = server.lines().filter { !it.startsWith("  ") && !it.startsWith("//") }
        assertFalse(
            topLevelLines.any { it.trimStart().startsWith("HonoNodeServer.serve") },
            "Server.res must wrap serve() inside start = () => ... so it stays import-safe",
        )
    }

    @Test
    fun `ServerMain res calls Server start as the production entry point`() {
        val files = HonoInertiaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/ServerMain.res"))
        assertTrue(files["src/ServerMain.res"]!!.contains("Server.start()"))
    }

    @Test
    fun `README documents the four Vite+ subcommands and the Inertia frontend recipe`() {
        val readme = HonoInertiaTemplateFiles.generate(ctx)["README.md"]!!
        assertTrue(readme.contains("vp dev"))
        assertTrue(readme.contains("vp build"))
        assertTrue(readme.contains("vp test"))
        assertTrue(readme.contains("vp check"))
        assertTrue(readme.contains("## API"))
        assertTrue(readme.contains("## Frontend"))
        assertTrue(readme.contains("## About Vite+"))
    }

    @Test
    fun `Hono shared bindings stay byte-identical with the plain Hono template`() {
        val base = HonoTemplateFiles.generate(ctx)["src/Hono.res"]!!
        val inertia = HonoInertiaTemplateFiles.generate(ctx)["src/Hono.res"]!!
        // Sharing the same external surface lets us bump bindings centrally.
        assert(base == inertia) {
            "src/Hono.res must match HonoTemplateFiles output to share bindings"
        }
    }
}
