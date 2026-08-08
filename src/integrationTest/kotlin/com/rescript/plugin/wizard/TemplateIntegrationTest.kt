package com.rescript.plugin.wizard

import com.rescript.plugin.wizard.templates.TemplateContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream

/**
 * End-to-end smoke test that materializes each [ProjectTemplate], installs its dependencies
 * with pnpm, and runs `rescript` to verify the generated project compiles.
 *
 * Parameterized over [ValidationLibrary] so both `zod` and `sury` variants of every
 * template are exercised — single-validation runs would silently mask bugs in whichever
 * variant the default context doesn't select.
 *
 * Templates that ship a top-level JS bundle (Vite+React, Electron) additionally run their
 * `build` script. Mobile/native templates that require platform SDKs (React Native) only
 * verify the install step.
 */
class TemplateIntegrationTest {
    private val pnpm: String = System.getProperty("template.test.pnpm", "pnpm")
    private val bun: String = System.getProperty("template.test.bun", "bun")

    @ParameterizedTest(name = "{0} ({1}) generates a working project")
    @MethodSource("templateAndValidationCombinations")
    fun template(
        template: ProjectTemplate,
        validation: ValidationLibrary,
        @TempDir tempDir: Path,
    ) {
        IntegrationTestSupport.requireBinary(pnpm)

        val ctx =
            TemplateContext(
                "demo-${template.name.lowercase()}-${validation.name.lowercase()}",
                PackageManager.PNPM,
                validation,
            )
        val files = template.generateFiles(ctx)
        IntegrationTestSupport.writeFiles(tempDir, files)

        val install =
            IntegrationTestSupport.exec(
                tempDir,
                listOf(pnpm, "install", "--prefer-offline", "--ignore-scripts"),
            )
        assertTrue(
            install.succeeded,
            "pnpm install failed for ${template.displayName} (${validation.name.lowercase()})\nstdout=${install.stdout}\nstderr=${install.stderr}",
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
                        "rescript build failed for ${template.displayName} (${validation.name.lowercase()}) in $dir\n" +
                            "stdout=${res.stdout}\nstderr=${res.stderr}",
                    )
                }
            } else {
                val rescript = IntegrationTestSupport.exec(tempDir, listOf(pnpm, "exec", "rescript"))
                assertTrue(
                    rescript.succeeded,
                    "rescript build failed for ${template.displayName} (${validation.name.lowercase()})\n" +
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
                "pnpm test failed for ${template.displayName} (${validation.name.lowercase()})\n" +
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
                "pnpm build failed for ${template.displayName} (${validation.name.lowercase()})\n" +
                    "stdout=${build.stdout}\nstderr=${build.stderr}",
            )
        }
    }

    /**
     * Same shape as [template], but exercises [PackageManager.BUN]. Auto-skips
     * via JUnit `Assumptions` when `bun` is not on `PATH`, so the suite still
     * runs everywhere — bun coverage just upgrades from `skipped` to `passed`
     * once the binary is installed.
     *
     * BUN-specific quirks compared to the pnpm path:
     * - Workspace install / compile uses `bun install` and `bunx rescript` per
     *   workspace; `bun --filter` is supported but the resolution model
     *   doesn't need a recursive-exec wait between producer/consumer because
     *   bun installs hoist by default (the tradeoff is broader symbol visibility
     *   in transitive deps, which we accept for the test path).
     * - The vitest skip set carries over: import-time side effects are PM-
     *   independent (DB connection, `serve()`).
     */
    @ParameterizedTest(name = "{0} ({1}) generates a working BUN project")
    @MethodSource("bunTemplateAndValidationCombinations")
    fun bunTemplate(
        template: ProjectTemplate,
        validation: ValidationLibrary,
        @TempDir tempDir: Path,
    ) {
        IntegrationTestSupport.requireBinary(bun)

        val ctx =
            TemplateContext(
                "demo-bun-${template.name.lowercase()}-${validation.name.lowercase()}",
                PackageManager.BUN,
                validation,
            )
        val files = template.generateFiles(ctx)
        IntegrationTestSupport.writeFiles(tempDir, files)

        val install =
            IntegrationTestSupport.exec(
                tempDir,
                listOf(bun, "install", "--ignore-scripts"),
            )
        assertTrue(
            install.succeeded,
            "bun install failed for ${template.displayName} (${validation.name.lowercase()})\n" +
                "stdout=${install.stdout}\nstderr=${install.stderr}",
        )

        // bun installs hoist transitive deps so `bunx rescript` resolves the
        // compiler from any workspace. The pnpm path has to invoke
        // `--filter` per-workspace because of its strict layout — bun doesn't.
        val rescriptDirs =
            files.keys
                .filter { it.endsWith("rescript.json") }
                .map { it.removeSuffix("rescript.json").trimEnd('/') }
        val ordered = rescriptDirs.sortedBy { if (it.endsWith("/shared")) 0 else 1 }
        ordered.forEach { dir ->
            val target = if (dir.isEmpty()) tempDir else tempDir.resolve(dir)
            val res = IntegrationTestSupport.exec(target, listOf(bun, "x", "rescript"))
            assertTrue(
                res.succeeded,
                "rescript build failed under bun for ${template.displayName} " +
                    "(${validation.name.lowercase()}) in ${dir.ifEmpty { "<root>" }}\n" +
                    "stdout=${res.stdout}\nstderr=${res.stderr}",
            )
        }
    }

    companion object {
        /**
         * Cartesian product of every [ProjectTemplate] × every [ValidationLibrary].
         * 16 templates × 2 libraries = 32 parameter rows, so the integration suite
         * doubles in runtime — acceptable trade-off for not silently shipping a
         * broken sury variant.
         */
        @JvmStatic
        fun templateAndValidationCombinations(): Stream<Arguments> =
            ProjectTemplate.entries
                .flatMap { template ->
                    ValidationLibrary.entries.map { validation ->
                        Arguments.of(template, validation)
                    }
                }.stream()

        /**
         * BUN parameter source. Excludes templates whose generated `package.json`
         * scripts hardcode `pnpm` / `npx` (no PM support layer was added) and
         * templates that need React Native CLI tooling that doesn't run under
         * bun. The list mirrors what the BUN PackageManager path actually
         * supports today; expand once a template gains BUN compatibility.
         */
        @JvmStatic
        fun bunTemplateAndValidationCombinations(): Stream<Arguments> {
            val bunCompatible =
                ProjectTemplate.entries.filter { it !in templatesNotBunCompatible }
            return bunCompatible
                .flatMap { template ->
                    ValidationLibrary.entries.map { validation ->
                        Arguments.of(template, validation)
                    }
                }.stream()
        }

        // Templates that explicitly opt out of BUN coverage today.
        // - REACT_NATIVE_CLI requires the React Native CLI which assumes
        //   npm/yarn for android/ios scripts; bun isn't a community-supported
        //   replacement.
        // - REACT_NATIVE (Expo) shells out to expo-cli that hits node directly.
        // - MONOREPO root scripts use `pnpm --filter` / `npm --workspace` /
        //   `bun --filter` per PM, but the generator wasn't fully exercised
        //   for BUN before this audit; track separately.
        private val templatesNotBunCompatible: Set<ProjectTemplate> =
            setOf(
                ProjectTemplate.REACT_NATIVE,
                ProjectTemplate.REACT_NATIVE_CLI,
                ProjectTemplate.MONOREPO,
            )

        // Vite+ (vite-plus) is pre-1.0 and currently does not link cleanly with
        // @vitejs/plugin-react via pnpm's nested store layout. The generated
        // `pnpm build` therefore fails with ERR_MODULE_NOT_FOUND on `vite/internal`.
        // Until Vite+ ships a stable release that resolves the @vitejs ecosystem,
        // we only verify that `rescript` builds for the Vite+ templates and skip the
        // top-level bundler invocation. The README of each template documents the
        // pre-1.0 status and the fallback to plain Vite.
        //
        // Astro does not use Vite+, so it was only ever collateral damage of emptying
        // this set. It is back because nothing else in the suite invokes a bundler,
        // and the Astro 7 upgrade moved the template onto Vite 8 / Rolldown — a change
        // that install and `rescript` alone cannot exercise.
        private val templatesWithBundle: Set<ProjectTemplate> = setOf(ProjectTemplate.ASTRO)

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
                // res-x's `test` script invokes `bun test` (its smoke test
                // imports `Bun`-only globals via rescript-bun). The PNPM
                // integration path runs `pnpm test`, which delegates to the
                // template's script — that requires `bun` on PATH. Skip when
                // testing through the pnpm path; the BUN parameterized test
                // (`bunTemplate`) covers res-x natively when bun is installed.
                ProjectTemplate.RES_X,
            )
    }
}
