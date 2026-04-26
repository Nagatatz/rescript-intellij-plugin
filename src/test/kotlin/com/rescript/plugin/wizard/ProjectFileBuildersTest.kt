package com.rescript.plugin.wizard

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProjectFileBuildersTest {
    @Test
    fun `rescriptJson includes project name`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"name\": \"my-app\""))
    }

    @Test
    fun `rescriptJson includes default sources config`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"dir\": \"src\""))
        assertTrue(json.contains("\"subdirs\": true"))
    }

    @Test
    fun `rescriptJson includes package-specs and suffix`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"module\": \"esmodule\""))
        assertTrue(json.contains("\"suffix\": \".res.mjs\""))
    }

    @Test
    fun `rescriptJson includes default dependencies under the modern key`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"@rescript/core\""))
        // Modern key required: ReScript prints a deprecation warning on every
        // build for the legacy bs-dependencies alias.
        assertTrue(json.contains("\"dependencies\":"))
        assertFalse(json.contains("\"bs-dependencies\""))
    }

    @Test
    fun `rescriptJson with jsx includes jsx section`() {
        val json = ProjectFileBuilders.rescriptJson("my-app", includeJsx = true)
        assertTrue(json.contains("\"jsx\""))
        assertTrue(json.contains("\"version\": 4"))
    }

    @Test
    fun `rescriptJson without jsx excludes jsx section`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertFalse(json.contains("\"jsx\""))
    }

    @Test
    fun `rescriptJson with genType includes gentypeconfig`() {
        val json = ProjectFileBuilders.rescriptJson("my-app", includeGenType = true)
        assertTrue(json.contains("\"gentypeconfig\""))
    }

    @Test
    fun `rescriptJson includes compiler-flags under the modern key`() {
        val json = ProjectFileBuilders.rescriptJson("my-app")
        assertTrue(json.contains("\"-open RescriptCore\""))
        assertTrue(json.contains("\"compiler-flags\":"))
        assertFalse(json.contains("\"bsc-flags\""))
    }

    @Test
    fun `packageJson includes name and version`() {
        val json = ProjectFileBuilders.packageJson("my-app")
        assertTrue(json.contains("\"name\": \"my-app\""))
        assertTrue(json.contains("\"version\": \"0.1.0\""))
    }

    @Test
    fun `packageJson includes default scripts`() {
        val json = ProjectFileBuilders.packageJson("my-app")
        assertTrue(json.contains("\"res:build\": \"rescript\""))
    }

    @Test
    fun `packageJson with dependencies`() {
        val json =
            ProjectFileBuilders.packageJson(
                "my-app",
                dependencies = linkedMapOf("rescript" to "^12.0.0"),
            )
        assertTrue(json.contains("\"rescript\": \"^12.0.0\""))
    }

    @Test
    fun `packageJson with devDependencies`() {
        val json =
            ProjectFileBuilders.packageJson(
                "my-app",
                devDependencies = linkedMapOf("vite" to "^6.0.0"),
            )
        assertTrue(json.contains("\"devDependencies\""))
        assertTrue(json.contains("\"vite\": \"^6.0.0\""))
    }

    @Test
    fun `packageJson without devDependencies omits section`() {
        val json = ProjectFileBuilders.packageJson("my-app")
        assertFalse(json.contains("\"devDependencies\""))
    }

    @Test
    fun `packageJson with single bin entry renders as an object keyed by executable name`() {
        val json =
            ProjectFileBuilders.packageJson(
                "my-app",
                bin = linkedMapOf("my-app" to "./bin/cli.mjs"),
            )
        assertTrue(json.contains("\"bin\""))
        assertTrue(json.contains("\"my-app\": \"./bin/cli.mjs\""))
    }

    @Test
    fun `packageJson with multiple bin entries renders all executables`() {
        val json =
            ProjectFileBuilders.packageJson(
                "my-app",
                bin =
                    linkedMapOf(
                        "my-app" to "./bin/cli.mjs",
                        "my-app-init" to "./bin/init.mjs",
                    ),
            )
        assertTrue(json.contains("\"my-app\": \"./bin/cli.mjs\""))
        assertTrue(json.contains("\"my-app-init\": \"./bin/init.mjs\""))
    }

    @Test
    fun `packageJson with workspaces`() {
        val json = ProjectFileBuilders.packageJson("my-app", workspaces = listOf("packages/*"))
        assertTrue(json.contains("\"workspaces\""))
    }

    @Test
    fun `packageJson with type module`() {
        val json = ProjectFileBuilders.packageJson("my-app", type = "module")
        assertTrue(json.contains("\"type\": \"module\""))
    }

    @Test
    fun `packageJson with private flag`() {
        val json = ProjectFileBuilders.packageJson("my-app", isPrivate = true)
        assertTrue(json.contains("\"private\": true"))
    }

    @Test
    fun `packageJson renders main, types, exports, and files when supplied`() {
        val json =
            ProjectFileBuilders.packageJson(
                "my-lib",
                type = "module",
                main = "./src/Index.res.mjs",
                types = "./src/Index.gen.d.ts",
                exports =
                    linkedMapOf(
                        "." to
                            linkedMapOf(
                                "types" to "./src/Index.gen.d.ts",
                                "import" to "./src/Index.res.mjs",
                            ),
                    ),
                files = listOf("src/**/*.res.mjs", "src/**/*.gen.d.ts"),
            )
        assertTrue(json.contains("\"main\": \"./src/Index.res.mjs\""))
        assertTrue(json.contains("\"types\": \"./src/Index.gen.d.ts\""))
        assertTrue(json.contains("\"exports\""))
        assertTrue(json.contains("\".\""))
        assertTrue(json.contains("\"import\": \"./src/Index.res.mjs\""))
        assertTrue(json.contains("\"files\": [\"src/**/*.res.mjs\", \"src/**/*.gen.d.ts\"]"))
    }

    @Test
    fun `packageJson omits main, types, exports, and files when not supplied`() {
        val json = ProjectFileBuilders.packageJson("my-app")
        assertFalse(json.contains("\"main\""))
        assertFalse(json.contains("\"types\""))
        assertFalse(json.contains("\"exports\""))
        assertFalse(json.contains("\"files\""))
    }

    @Test
    fun `defaultScripts returns three entries`() {
        assertEquals(3, ProjectFileBuilders.defaultScripts().size)
    }

    @Test
    fun `starterModule generates console log`() {
        assertTrue(ProjectFileBuilders.starterModule().contains("Console.log"))
    }

    @Test
    fun `reactComponent generates valid component`() {
        val code = ProjectFileBuilders.reactComponent()
        assertTrue(code.contains("@react.component"))
        assertTrue(code.contains("let make"))
    }

    @Test
    fun `honoBindings generates Hono types`() {
        val code = ProjectFileBuilders.honoBindings()
        assertTrue(code.contains("type app"))
        assertTrue(code.contains("type context"))
        assertTrue(code.contains("createApp"))
    }

    @Test
    fun `honoNodeServerBindings generates serve binding`() {
        val code = ProjectFileBuilders.honoNodeServerBindings()
        assertTrue(code.contains("serve"))
        assertTrue(code.contains("port: int"))
    }

    @Test
    fun `honoNodeServerBindings matches the modern hono node-server v1 signature`() {
        val code = ProjectFileBuilders.honoNodeServerBindings()
        // v1 API: serve({fetch, port?, hostname?, ...}, listeningListener?).
        // The previous binding `serve(app, {port})` matched a removed API
        // and would TypeError at runtime ('listeningListener is not a function').
        assertTrue(
            code.contains("type serveOptions<'fetch> = {fetch: 'fetch, port: int}"),
            "serveOptions must thread `fetch` so callers pass `app->honoFetch`",
        )
        assertTrue(
            code.contains("external honoFetch: Hono.app => 'fetch"),
            "honoFetch accessor is required so call sites can extract `app.fetch`",
        )
        assertTrue(
            code.contains("external serve: serveOptions<'fetch> => unit = \"serve\""),
            "serve takes a single options record; the second-arg listener is unused",
        )
        // Old shape must not creep back in.
        assertFalse(code.contains("serve: (Hono.app, serveOptions)"))
    }

    @Test
    fun `viteConfigWithProxy includes proxy configuration`() {
        val config = ProjectFileBuilders.viteConfigWithProxy()
        assertTrue(config.contains("proxy"))
        assertTrue(config.contains("/api"))
        assertTrue(config.contains("localhost:3000"))
        assertTrue(config.contains("changeOrigin"))
    }

    @Test
    fun `viteConfigWithProxy with custom target and path`() {
        val config =
            ProjectFileBuilders.viteConfigWithProxy(
                proxyTarget = "http://localhost:8080",
                proxyPath = "/backend",
            )
        assertTrue(config.contains("localhost:8080"))
        assertTrue(config.contains("/backend"))
    }

    @Test
    fun `viteConfigWithProxy includes defineConfig import`() {
        val config = ProjectFileBuilders.viteConfigWithProxy()
        assertTrue(config.contains("defineConfig"))
        assertTrue(config.contains("import react"))
    }
}
