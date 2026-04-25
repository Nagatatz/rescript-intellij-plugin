package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.TemplateContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Path

/**
 * End-to-end smoke test that materializes each [ProjectTemplate], installs its dependencies
 * with pnpm, and runs `rescript` to verify the generated project compiles.
 *
 * Templates that ship a top-level JS bundle (Vite+React, Electron) additionally run their
 * `build` script. Mobile/native templates that require platform SDKs (React Native) only
 * verify the install step.
 */
class TemplateIntegrationTest {
    private val pnpm: String = System.getProperty("template.test.pnpm", "pnpm")

    @ParameterizedTest(name = "{0} generates a working project")
    @EnumSource(ProjectTemplate::class)
    fun template(
        template: ProjectTemplate,
        @TempDir tempDir: Path,
    ) {
        IntegrationTestSupport.requireBinary(pnpm)

        val ctx = TemplateContext("demo-${template.name.lowercase()}", PackageManager.PNPM)
        val files = template.generateFiles(ctx)
        IntegrationTestSupport.writeFiles(tempDir, files)

        val install =
            IntegrationTestSupport.exec(
                tempDir,
                listOf(pnpm, "install", "--prefer-offline", "--ignore-scripts"),
            )
        assertTrue(
            install.succeeded,
            "pnpm install failed for ${template.displayName}\nstdout=${install.stdout}\nstderr=${install.stderr}",
        )

        // Compile every workspace that ships a `rescript.json`, not just the
        // project root. Monorepo templates have per-package configs and no
        // root config, so a naive `it == "rescript.json"` check would skip
        // them entirely and silently mask broken client/server ReScript.
        val rescriptDirs =
            files.keys
                .filter { it.endsWith("rescript.json") }
                .map { it.removeSuffix("rescript.json").trimEnd('/') }
        if (rescriptDirs.isNotEmpty()) {
            val isWorkspace = files.keys.any { it == "pnpm-workspace.yaml" }
            if (isWorkspace) {
                // pnpm's recursive-exec topological order doesn't actually wait for
                // a producer to finish before consumers start (even with
                // `--workspace-concurrency=1`), so we run rescript per-workspace
                // explicitly. Build the bs-dependency `shared` first; everything
                // that depends on it runs afterwards in declaration order.
                val ordered = rescriptDirs.sortedBy { if (it.endsWith("/shared")) 0 else 1 }
                ordered.forEach { dir ->
                    val res =
                        IntegrationTestSupport.exec(
                            tempDir,
                            listOf(pnpm, "--filter", "./$dir", "exec", "rescript"),
                        )
                    assertTrue(
                        res.succeeded,
                        "rescript build failed for ${template.displayName} in $dir\n" +
                            "stdout=${res.stdout}\nstderr=${res.stderr}",
                    )
                }
            } else {
                val rescript = IntegrationTestSupport.exec(tempDir, listOf(pnpm, "exec", "rescript"))
                assertTrue(
                    rescript.succeeded,
                    "rescript build failed for ${template.displayName}\n" +
                        "stdout=${rescript.stdout}\nstderr=${rescript.stderr}",
                )
            }
        }

        if (template !in templatesSkipTest) {
            // Run the bundled vitest smoke tests so a broken `Server.test.mjs`
            // / `App.test.mjs` doesn't ship to a user. Unit tests on the
            // generator side only verify the test files were *emitted*, not
            // that the assertions actually pass at runtime.
            val test = IntegrationTestSupport.exec(tempDir, listOf(pnpm, "test"))
            assertTrue(
                test.succeeded,
                "pnpm test failed for ${template.displayName}\n" +
                    "stdout=${test.stdout}\nstderr=${test.stderr}",
            )
        }

        if (template in templatesWithBundle) {
            val build =
                IntegrationTestSupport.exec(
                    tempDir,
                    listOf(pnpm, "build"),
                )
            assertTrue(
                build.succeeded,
                "pnpm build failed for ${template.displayName}\n" +
                    "stdout=${build.stdout}\nstderr=${build.stderr}",
            )
        }
    }

    companion object {
        // Vite+ (vite-plus) is pre-1.0 and currently does not link cleanly with
        // @vitejs/plugin-react via pnpm's nested store layout. The generated
        // `pnpm build` therefore fails with ERR_MODULE_NOT_FOUND on `vite/internal`.
        // Until Vite+ ships a stable release that resolves the @vitejs ecosystem,
        // we only verify that `rescript` builds for these templates and skip the
        // top-level bundler invocation. The README of each template documents the
        // pre-1.0 status and the fallback to plain Vite.
        private val templatesWithBundle: Set<ProjectTemplate> = emptySet()

        // Templates whose `test` script needs setup the smoke-import alone
        // can't provide in a temp dir. Each entry below documents why; drop
        // an entry once the upstream constraint is lifted.
        private val templatesSkipTest: Set<ProjectTemplate> =
            setOf(
                // Vite+ wraps Vitest as `vp test`. Pre-1.0 vite-plus inherits
                // the same plugin-resolution issue that blocks `vp build` and
                // surfaces it for the test command too.
                ProjectTemplate.VITE_REACT,
                ProjectTemplate.ELECTRON,
                // The bundled smoke test does `import("../Server.res.mjs")`,
                // which runs `Db.res`'s top-level `createClient(...)`. That
                // tries to open `./data/app.db` and fails in the temp dir.
                // Re-enable once smoke tests gain a vitest setup file that
                // pins `DATABASE_URL` to `:memory:` before module load.
                ProjectTemplate.HONO,
                ProjectTemplate.HONO_GRAPHQL,
                ProjectTemplate.MONOREPO,
                ProjectTemplate.FULL_STACK,
                // Server.res calls `HonoNodeServer.serve(...)` at module
                // load. The smoke import therefore tries to bind a real port,
                // and `@hono/node-server`'s current API ends up calling our
                // options object as a `listeningListener`. Re-enable after
                // splitting `Server.res` into a side-effect-free `app`
                // definition + a separate entry-point that calls `serve`.
                ProjectTemplate.GOOGLE_CLOUD_RUN,
                // res-x's runtime imports `rescript-bun` / `rescript-x`,
                // which reference the global `Bun` object. Vitest runs under
                // Node where `Bun` is undefined. The smoke test would have
                // to run under `bun test` instead.
                ProjectTemplate.RES_X,
            )
    }
}
