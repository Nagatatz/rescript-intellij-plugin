package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.TemplateContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProjectTemplateTest {
    @Test
    fun `enum has 21 entries`() {
        assertEquals(21, ProjectTemplate.entries.size)
    }

    @Test
    fun `each template has a non-empty displayName`() {
        ProjectTemplate.entries.forEach {
            assertTrue(it.displayName.isNotBlank(), "${it.name} should have non-empty displayName")
        }
    }

    @Test
    fun `each template has a non-empty description`() {
        ProjectTemplate.entries.forEach {
            assertTrue(it.description.isNotBlank(), "${it.name} should have non-empty description")
        }
    }

    @Test
    fun `every template README contains Extending Bindings section`() {
        ProjectTemplate.entries.forEach { template ->
            val readme =
                template.generateFiles("demo")["README.md"]
                    ?: error("${template.name} should generate a README.md")
            assertTrue(
                readme.contains("## Extending Bindings"),
                "${template.name} README should include the Extending Bindings section",
            )
            assertTrue(
                readme.contains("### Pattern: typed fetch wrapper") ||
                    readme.contains("### Pattern: adding a Hono middleware") ||
                    readme.contains("### Pattern: filtering with drizzle-orm"),
                "${template.name} README should include at least one binding recipe",
            )
        }
    }

    @Test
    fun `BASIC template has single source root`() {
        assertEquals(listOf("src"), ProjectTemplate.BASIC.sourceRoots)
    }

    @Test
    fun `MONOREPO template has three source roots`() {
        assertEquals(
            listOf("packages/shared/src", "packages/server/src", "packages/client/src"),
            ProjectTemplate.MONOREPO.sourceRoots,
        )
    }

    @Test
    fun `all templates generate non-empty file maps`() {
        ProjectTemplate.entries.forEach {
            val files = it.generateFiles("test-project")
            assertTrue(files.isNotEmpty(), "${it.name} should generate at least one file")
        }
    }

    @Test
    fun `all templates generate rescript json`() {
        ProjectTemplate.entries.forEach {
            val files = it.generateFiles("test-project")
            assertTrue(
                files.keys.any { k -> k.contains("rescript.json") },
                "${it.name} should have rescript.json",
            )
        }
    }

    @Test
    fun `all templates generate package json`() {
        ProjectTemplate.entries.forEach {
            val files = it.generateFiles("test-project")
            assertTrue(
                files.keys.any { k -> k.endsWith("package.json") },
                "${it.name} should generate package.json",
            )
        }
    }

    @Test
    fun `all templates generate at least one res file`() {
        ProjectTemplate.entries.forEach {
            val files = it.generateFiles("test-project")
            assertTrue(
                files.keys.any { k -> k.endsWith(".res") },
                "${it.name} should generate at least one .res file",
            )
        }
    }

    @Test
    fun `TemplateCategory has 9 entries`() {
        assertEquals(9, TemplateCategory.entries.size)
    }

    @Test
    fun `VITE_REACT template includes vite and react deps`() {
        val files = ProjectTemplate.VITE_REACT.generateFiles("test-project")
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"react\""))
        assertTrue(pkg.contains("\"vite\""))
        assertTrue(files.containsKey("vite.config.mjs"))
        assertTrue(files.containsKey("index.html"))
    }

    @Test
    fun `NEXTJS template includes next dep and genType config`() {
        val files = ProjectTemplate.NEXTJS.generateFiles("test-project")
        assertTrue(files["package.json"]!!.contains("\"next\""))
        assertTrue(files["rescript.json"]!!.contains("gentypeconfig"))
    }

    @Test
    fun `ELECTRON template includes electron dep`() {
        val files = ProjectTemplate.ELECTRON.generateFiles("test-project")
        assertTrue(files["package.json"]!!.contains("\"electron\""))
        assertTrue(files.containsKey("main.cjs"))
    }

    @Test
    fun `HONO template includes hono and node-server deps`() {
        val files = ProjectTemplate.HONO.generateFiles("test-project")
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"hono\""))
        assertTrue(pkg.contains("\"@hono/node-server\""))
    }

    @Test
    fun `CLOUDFLARE_WORKERS template includes wrangler config`() {
        val files = ProjectTemplate.CLOUDFLARE_WORKERS.generateFiles("test-project")
        assertTrue(files.containsKey("wrangler.jsonc"))
        assertTrue(files["src/Server.res"]!!.contains("export default app"))
    }

    @Test
    fun `AWS_LAMBDA template includes esbuild and handler export`() {
        val files = ProjectTemplate.AWS_LAMBDA.generateFiles("test-project")
        assertTrue(files["package.json"]!!.contains("\"esbuild\""))
        assertTrue(files["src/Server.res"]!!.contains("handler"))
    }

    @Test
    fun `GOOGLE_CLOUD_RUN template includes Dockerfile and port 8080`() {
        val files = ProjectTemplate.GOOGLE_CLOUD_RUN.generateFiles("test-project")
        assertTrue(files.containsKey("Dockerfile"))
        assertTrue(files["src/Server.res"]!!.contains("8080"))
    }

    @Test
    fun `REACT_NATIVE template includes expo config`() {
        val files = ProjectTemplate.REACT_NATIVE.generateFiles("test-project")
        assertTrue(files.containsKey("app.json"))
        assertTrue(files["package.json"]!!.contains("\"expo\""))
    }

    @Test
    fun `REACT_NATIVE_CLI template uses Community CLI without expo`() {
        val template = ProjectTemplate.REACT_NATIVE_CLI
        assertEquals("React Native (Community CLI)", template.displayName)
        assertEquals(TemplateCategory.MOBILE, template.category)
        val files = template.generateFiles("test-project")
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"@react-native-community/cli\""))
        assertTrue(pkg.contains("\"react-native\""))
        assertFalse(pkg.contains("\"expo\""))
        assertTrue(files.containsKey("metro.config.js"))
        assertTrue(files.containsKey("babel.config.js"))
        assertTrue(files.containsKey("index.js"))
    }

    @Test
    fun `Expo and Community CLI templates differ in package json deps`() {
        val expoPkg = ProjectTemplate.REACT_NATIVE.generateFiles("t")["package.json"]!!
        val cliPkg = ProjectTemplate.REACT_NATIVE_CLI.generateFiles("t")["package.json"]!!
        assertTrue(expoPkg.contains("\"expo\""))
        assertFalse(cliPkg.contains("\"expo\""))
        assertTrue(cliPkg.contains("\"@react-native-community/cli\""))
        assertFalse(expoPkg.contains("\"@react-native-community/cli\""))
    }

    @Test
    fun `NPM_LIBRARY template includes genType config`() {
        val files = ProjectTemplate.NPM_LIBRARY.generateFiles("test-project")
        assertTrue(files["rescript.json"]!!.contains("gentypeconfig"))
    }

    @Test
    fun `CLI_TOOL template includes bin entry`() {
        val files = ProjectTemplate.CLI_TOOL.generateFiles("test-project")
        assertTrue(files["package.json"]!!.contains("\"bin\""))
    }

    @Test
    fun `MONOREPO template has three packages`() {
        val files = ProjectTemplate.MONOREPO.generateFiles("test-project")
        assertTrue(files.keys.any { it.startsWith("packages/shared/") })
        assertTrue(files.keys.any { it.startsWith("packages/server/") })
        assertTrue(files.keys.any { it.startsWith("packages/client/") })
    }

    @Test
    fun `MONOREPO declares workspaces via pnpm-workspace yaml when defaulting to pnpm`() {
        val files = ProjectTemplate.MONOREPO.generateFiles("test-project")
        assertTrue(files.containsKey("pnpm-workspace.yaml"))
        assertTrue(files["pnpm-workspace.yaml"]!!.contains("packages/*"))
    }

    @Test
    fun `MONOREPO uses npm workspaces field when npm is selected`() {
        val ctx = TemplateContext("test-project", PackageManager.NPM)
        val files = ProjectTemplate.MONOREPO.generateFiles(ctx)
        assertTrue(files["package.json"]!!.contains("\"workspaces\""))
        assertFalse(files.containsKey("pnpm-workspace.yaml"))
    }

    @Test
    fun `MONOREPO root package json has dev script with concurrently`() {
        val files = ProjectTemplate.MONOREPO.generateFiles("test-project")
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("concurrently"))
        assertTrue(pkg.contains("\"dev\""))
    }

    @Test
    fun `MONOREPO client vite config has api proxy`() {
        val files = ProjectTemplate.MONOREPO.generateFiles("test-project")
        val viteConfig = files["packages/client/vite.config.mjs"]!!
        assertTrue(viteConfig.contains("proxy"))
        assertTrue(viteConfig.contains("/api"))
    }

    @Test
    fun `MONOREPO server has dev script with node watch`() {
        val files = ProjectTemplate.MONOREPO.generateFiles("test-project")
        val pkg = files["packages/server/package.json"]!!
        assertTrue(pkg.contains("node --watch"))
    }

    @Test
    fun `HONO shared bindings are consistent across hono-based templates`() {
        val honoFiles = ProjectTemplate.HONO.generateFiles("test")
        val cfFiles = ProjectTemplate.CLOUDFLARE_WORKERS.generateFiles("test")
        val lambdaFiles = ProjectTemplate.AWS_LAMBDA.generateFiles("test")
        val cloudRunFiles = ProjectTemplate.GOOGLE_CLOUD_RUN.generateFiles("test")
        assertEquals(honoFiles["src/Hono.res"], cfFiles["src/Hono.res"])
        assertEquals(honoFiles["src/Hono.res"], lambdaFiles["src/Hono.res"])
        assertEquals(honoFiles["src/Hono.res"], cloudRunFiles["src/Hono.res"])
    }

    @Test
    fun `drizzle Db res is shared across all four drizzle-backed templates`() {
        val hono = ProjectTemplate.HONO.generateFiles("demo")["src/Db.res"]!!
        val graphql = ProjectTemplate.HONO_GRAPHQL.generateFiles("demo")["src/Db.res"]!!
        val fullStack = ProjectTemplate.FULL_STACK.generateFiles("demo")["src/server/Db.res"]!!
        val monorepo = ProjectTemplate.MONOREPO.generateFiles("demo")["packages/server/src/Db.res"]!!
        assertEquals(hono, graphql)
        assertEquals(hono, fullStack)
        assertEquals(hono, monorepo)
    }

    @Test
    fun `shared Db res exposes the new drizzle helpers`() {
        val db = ProjectTemplate.HONO.generateFiles("demo")["src/Db.res"]!!
        listOf(
            "eq",
            "\\\"and\"",
            "or",
            "inArray",
            "where",
            "orderBy",
            "limit",
            "update",
            "set",
            "deleteFrom",
            "asc",
            "desc",
        ).forEach { helper ->
            assertTrue(
                db.contains("external $helper"),
                "Db.res should expose a $helper external",
            )
        }
    }

    @Test
    fun `hono-graphql resolvers use the new helpers and carry no TODO placeholders`() {
        val resolvers =
            ProjectTemplate.HONO_GRAPHQL.generateFiles("demo")["src/Resolvers.res"]
                ?: error("hono-graphql Resolvers.res missing")
        assertTrue(
            resolvers.contains("Db.where(Db.eq(Schema.users[\"id\"], args[\"id\"]))"),
            "userById / deleteUser should filter with Db.where(Db.eq(...))",
        )
        assertTrue(
            resolvers.contains("Db.deleteFrom(Schema.users)"),
            "deleteUser should use Db.deleteFrom",
        )
        assertFalse(resolvers.contains("TODO"), "userById TODO comment should be gone")
        assertFalse(resolvers.contains("Placeholder"), "deleteUser placeholder comment should be gone")
    }

    @Test
    fun `shared Hono bindings expose the hono cors middleware factory`() {
        val hono = ProjectTemplate.HONO.generateFiles("test")["src/Hono.res"]!!
        assertTrue(hono.contains("@module(\"hono/cors\")"), "Hono.res should @module hono/cors")
        assertTrue(hono.contains("external cors"), "Hono.res should expose a cors external")
    }

    @Test
    fun `FULL_STACK server ships a commented-out CORS block referencing Vite dev origin`() {
        val server =
            ProjectTemplate.FULL_STACK.generateFiles("demo")["src/server/Server.res"]
                ?: error("FULL_STACK Server.res missing")
        assertTrue(server.contains("Hono.cors"), "Server.res should reference Hono.cors")
        assertTrue(server.contains("http://localhost:5173"), "Server.res should mention Vite dev origin")
        assertTrue(
            server.contains("// app->Hono.use("),
            "CORS block should be commented out so the Vite proxy handles dev unchanged",
        )
    }

    @Test
    fun `MONOREPO server ships a commented-out CORS block referencing Vite dev origin`() {
        val server =
            ProjectTemplate.MONOREPO.generateFiles("demo")["packages/server/src/Server.res"]
                ?: error("MONOREPO server Server.res missing")
        assertTrue(server.contains("Hono.cors"), "Server.res should reference Hono.cors")
        assertTrue(server.contains("http://localhost:5173"), "Server.res should mention Vite dev origin")
        assertTrue(
            server.contains("// app->Hono.use("),
            "CORS block should be commented out so the Vite proxy handles dev unchanged",
        )
    }

    @Test
    fun `all Hono server templates surface the cors binding in their Server res`() {
        val targets =
            listOf(
                ProjectTemplate.HONO to "src/Server.res",
                ProjectTemplate.HONO_GRAPHQL to "src/Server.res",
                ProjectTemplate.CLOUDFLARE_WORKERS to "src/Server.res",
                ProjectTemplate.AWS_LAMBDA to "src/Server.res",
                ProjectTemplate.GOOGLE_CLOUD_RUN to "src/Server.res",
                ProjectTemplate.FULL_STACK to "src/server/Server.res",
                ProjectTemplate.MONOREPO to "packages/server/src/Server.res",
            )
        targets.forEach { (template, path) ->
            val server =
                template.generateFiles("demo")[path]
                    ?: error("${template.name} missing $path")
            assertTrue(
                server.contains("Hono.cors"),
                "${template.name} $path should mention Hono.cors (pre-wired or commented)",
            )
        }
    }

    @Test
    fun `HONO_INERTIA template surfaces the Inertia middleware and Vite+ scripts`() {
        val files = ProjectTemplate.HONO_INERTIA.generateFiles("demo")
        val pkg = files["package.json"]!!
        assertTrue(pkg.contains("\"@hono/inertia\""))
        assertTrue(pkg.contains("\"@inertiajs/react\""))
        assertTrue(pkg.contains("\"vp dev\""))
        val server = files["src/Server.res"]!!
        // The middleware is registered with a rootView so non-Inertia visits
        // receive a server-rendered HTML host page (no static index.html).
        assertTrue(server.contains("HonoInertia.inertia({rootView"))
        // Inertia template intentionally has no API routes returning JSON shells, so
        // it does not need to surface CORS — the assertion above is sufficient.
    }

    @Test
    fun `React templates include jsx config`() {
        val reactTemplates =
            listOf(
                ProjectTemplate.VITE_REACT,
                ProjectTemplate.NEXTJS,
                ProjectTemplate.ELECTRON,
                ProjectTemplate.REACT_NATIVE,
                ProjectTemplate.REACT_NATIVE_CLI,
                ProjectTemplate.HONO_INERTIA,
                ProjectTemplate.TANSTACK_START,
                ProjectTemplate.REMIX_RR_V7,
                ProjectTemplate.ASTRO,
                ProjectTemplate.WAKU,
            )
        reactTemplates.forEach {
            val rj = it.generateFiles("test")["rescript.json"]!!
            assertTrue(rj.contains("\"jsx\""), "${it.name} should include jsx config")
        }
    }

    @Test
    fun `non-React templates do not include jsx config`() {
        val nonReactTemplates =
            listOf(
                ProjectTemplate.BASIC,
                ProjectTemplate.HONO,
                ProjectTemplate.CLOUDFLARE_WORKERS,
                ProjectTemplate.AWS_LAMBDA,
                ProjectTemplate.GOOGLE_CLOUD_RUN,
                ProjectTemplate.NPM_LIBRARY,
                ProjectTemplate.CLI_TOOL,
            )
        nonReactTemplates.forEach {
            val rj = it.generateFiles("test")["rescript.json"] ?: return@forEach
            assertFalse(rj.contains("\"jsx\""), "${it.name} should not include jsx config")
        }
    }

    @Test
    fun `templates that opt out of validation selection are explicitly listed`() {
        // Add a template here when its constructor sets supportsValidationSelection = false.
        val optOuts =
            setOf(
                ProjectTemplate.TANSTACK_START,
                ProjectTemplate.REMIX_RR_V7,
                ProjectTemplate.ASTRO,
                ProjectTemplate.WAKU,
            )
        ProjectTemplate.entries.forEach { template ->
            val expected = template !in optOuts
            assertEquals(
                expected,
                template.supportsValidationSelection,
                "${template.name} supportsValidationSelection should be $expected",
            )
        }
    }
}
