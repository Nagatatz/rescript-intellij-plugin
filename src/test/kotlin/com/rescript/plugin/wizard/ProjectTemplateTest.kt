package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.TemplateContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProjectTemplateTest {
    @Test
    fun `enum has 12 entries`() {
        assertEquals(12, ProjectTemplate.entries.size)
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
    fun `React templates include jsx config`() {
        val reactTemplates =
            listOf(
                ProjectTemplate.VITE_REACT,
                ProjectTemplate.NEXTJS,
                ProjectTemplate.ELECTRON,
                ProjectTemplate.REACT_NATIVE,
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
}
