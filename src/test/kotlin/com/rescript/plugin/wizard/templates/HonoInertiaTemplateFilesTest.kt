package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.Database
import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HonoInertiaTemplateFilesTest {
    private val ctx = TemplateContext("svc", PackageManager.PNPM)
    private val suryCtx = TemplateContext("svc", PackageManager.PNPM, ValidationLibrary.SURY)
    private val postgresCtx = TemplateContext("svc", PackageManager.PNPM, database = Database.POSTGRES)
    private val mysqlCtx = TemplateContext("svc", PackageManager.PNPM, database = Database.MYSQL)

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
    fun `template ships rescript, vite, drizzle, README, and CI scaffolding without a static index html`() {
        val files = HonoInertiaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("rescript.json"))
        assertTrue(files.containsKey("vite.config.mjs"))
        assertTrue(files.containsKey("drizzle.config.ts"))
        assertTrue(files.containsKey("README.md"))
        assertTrue(files.containsKey(".gitignore"))
        assertTrue(files.containsKey(".editorconfig"))
        assertTrue(files.containsKey(".github/workflows/ci.yml"))
        assertTrue(files.containsKey(".env.example"))
        // The HTML host page is rendered server-side by inertia()'s rootView,
        // so a static index.html is intentionally omitted.
        assertFalse(files.containsKey("index.html"))
    }

    @Test
    fun `rescript json enables JSX so Inertia React pages compile`() {
        val rj = HonoInertiaTemplateFiles.generate(ctx)["rescript.json"]!!
        assertTrue(rj.contains("\"jsx\""))
        assertTrue(rj.contains("@rescript/react"))
    }

    @Test
    fun `vite config uses Vite+ defineConfig and does not register the inertiaPages plugin`() {
        val cfg = HonoInertiaTemplateFiles.generate(ctx)["vite.config.mjs"]!!
        assertTrue(cfg.contains("from \"vite-plus\""))
        assertTrue(cfg.contains("@vitejs/plugin-react"))
        // inertiaPages() generates a TS-only `pages.gen.ts` that ReScript does
        // not consume; including it confuses end users and wires up a plugin
        // configured against `app/pages` defaults that don't match the layout.
        assertFalse(cfg.contains("inertiaPages"))
        assertFalse(cfg.contains("@hono/inertia/vite"))
    }

    @Test
    fun `server wires the Inertia middleware with a rootView before defining routes`() {
        val server = HonoInertiaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("HonoInertia.inertia({rootView"))
        assertTrue(server.contains("Routes.Pages.register(app)"))
        // Middleware order is critical: inertia() must precede route registration.
        val inertiaIdx = server.indexOf("HonoInertia.inertia(")
        val routesIdx = server.indexOf("Routes.Pages.register(app)")
        assertTrue(inertiaIdx in 0 until routesIdx)
    }

    @Test
    fun `Server res defines a rootView that embeds the page JSON and the client entry`() {
        val server = HonoInertiaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("let rootView: HonoInertia.rootView"))
        assertTrue(server.contains("HonoInertia.serializePage"))
        assertTrue(server.contains("data-page="))
        assertTrue(server.contains("/src/client/Main.res.mjs"))
        // Apostrophe escape is required since serializePage only escapes `/`.
        assertTrue(server.contains("&#39;"))
    }

    @Test
    fun `routes call HonoInertia render synchronously without await for GET handlers`() {
        val routes = HonoInertiaTemplateFiles.generate(ctx)["src/Routes.res"]!!
        assertTrue(routes.contains("HonoInertia.render(") && routes.contains("\"Home\""))
        assertTrue(routes.contains("\"About\""))
        assertTrue(routes.contains("Validation.parseGreetForm"))
        // GET handlers must not await render: render returns a Hono Response
        // synchronously per @hono/inertia v0.2 typings.
        assertFalse(routes.contains("await ctx->HonoInertia.render"))
    }

    @Test
    fun `HonoInertia bindings declare a non-promise render and serializePage`() {
        val bindings = HonoInertiaTemplateFiles.generate(ctx)["src/HonoInertia.res"]!!
        assertTrue(bindings.contains("@module(\"@hono/inertia\")"))
        assertTrue(bindings.contains("external inertia: options =>"))
        assertTrue(bindings.contains("type rootView"))
        assertTrue(bindings.contains("external serializePage"))
        // render must return the Response directly, not promise<Response>.
        assertTrue(
            bindings.contains("external render: (Hono.context, string, 'props) => 'response"),
            "render binding must return the Response synchronously",
        )
        assertFalse(
            bindings.contains("=> promise<'response>"),
            "render binding must not wrap the response in a promise",
        )
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
    fun `pages js shim resolves make and throws on missing exports`() {
        val shim = HonoInertiaTemplateFiles.generate(ctx)["src/client/pages.js"]!!
        assertTrue(shim.contains("import.meta.glob"))
        assertTrue(shim.contains("./Pages/"))
        // Inertia reads `.default`, ReScript exports `make` — the shim bridges them.
        assertTrue(shim.contains("default: mod.make"))
        // Missing `make` must surface an explicit error rather than silently
        // returning an unrelated module export.
        assertTrue(shim.contains("if (!mod.make)"))
    }

    @Test
    fun `client Main wires resolvePage into createInertiaApp setup with hydration`() {
        val main = HonoInertiaTemplateFiles.generate(ctx)["src/client/Main.res"]!!
        assertTrue(main.contains("@module(\"./pages.js\")"))
        assertTrue(main.contains("InertiaBindings.createInertiaApp"))
        // SSR is the default; the entry must hydrate the server-rendered DOM
        // rather than throwing it away with a fresh `createRoot` mount.
        assertTrue(main.contains("hydrateRoot"))
        assertFalse(main.contains("ReactDOM.Client.createRoot"))
    }

    @Test
    fun `Ssr res is generated and registers Home and About in a sync switch`() {
        val ssr = HonoInertiaTemplateFiles.generate(ctx)["src/Ssr.res"]!!
        assertTrue(ssr.contains("@module(\"react-dom/server\")"))
        assertTrue(ssr.contains("renderToString"))
        // Both bundled pages must appear in the SSR switch so adding a page
        // without registering it surfaces as a build error rather than a 500.
        assertTrue(ssr.contains("\"Home\""))
        assertTrue(ssr.contains("\"About\""))
        // Pages call `usePage()` via MainLayout, so SSR must wrap them in
        // Inertia's `<App>` to provide the provider context.
        assertTrue(ssr.contains("@module(\"@inertiajs/react\")"))
        assertTrue(ssr.contains("InertiaApp"))
    }

    @Test
    fun `Server res rootView embeds the SSR-rendered body inside the mount point`() {
        val server = HonoInertiaTemplateFiles.generate(ctx)["src/Server.res"]!!
        assertTrue(server.contains("Ssr.renderInertia"))
        // The SSR body must land inside `<div id=\"app\">`, not just appended.
        assertTrue(server.contains("<div id=\"app\" data-page='\${pageJson}'>\${body}</div>"))
    }

    @Test
    fun `ships sample Home and About pages plus a shared layout without redundant prop ascriptions`() {
        val files = HonoInertiaTemplateFiles.generate(ctx)
        assertTrue(files.containsKey("src/client/MainLayout.res"))
        assertTrue(files.containsKey("src/client/Pages/Home.res"))
        assertTrue(files.containsKey("src/client/Pages/About.res"))
        assertTrue(files["src/client/MainLayout.res"]!!.contains("InertiaBindings.usePage"))
        val home = files["src/client/Pages/Home.res"]!!
        assertTrue(home.contains("@react.component"))
        // The dead `type props` + `let _ = ...` pair was a workaround that
        // shadowed the PPX-synthesized props type; both must be gone.
        assertFalse(home.contains("type props"))
        assertFalse(home.contains("let _ = ("))
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

    @Test
    fun `postgres variant swaps Schema, Db, drizzle config, and ships compose yaml`() {
        val files = HonoInertiaTemplateFiles.generate(postgresCtx)
        assertTrue(files["src/Schema.res"]!!.contains("pgTable"))
        assertFalse(files["src/Schema.res"]!!.contains("sqliteTable"))
        assertTrue(files["src/Db.res"]!!.contains("postgres-js"))
        assertTrue(files["drizzle.config.ts"]!!.contains("dialect: \"postgresql\""))
        assertTrue(files.containsKey("compose.yaml"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"postgres\": \"${TemplateVersions.POSTGRES_JS}\""))
        assertFalse(pkg.contains("\"@libsql/client\""))
    }

    @Test
    fun `mysql variant swaps Schema, Db, drizzle config, and ships compose yaml`() {
        val files = HonoInertiaTemplateFiles.generate(mysqlCtx)
        assertTrue(files["src/Schema.res"]!!.contains("mysqlTable"))
        assertTrue(files["src/Db.res"]!!.contains("mysql2"))
        assertTrue(files["drizzle.config.ts"]!!.contains("dialect: \"mysql\""))
        assertTrue(files.containsKey("compose.yaml"))
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"mysql2\": \"${TemplateVersions.MYSQL2}\""))
    }

    @Test
    fun `libsql variant does not ship a compose yaml`() {
        assertFalse(HonoInertiaTemplateFiles.generate(ctx).containsKey("compose.yaml"))
    }
}
