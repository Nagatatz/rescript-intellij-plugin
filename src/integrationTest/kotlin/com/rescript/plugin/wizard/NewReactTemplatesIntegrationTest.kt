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
 * Focused integration smoke for the four React-framework templates added in
 * this branch (TanStack Start, Remix / React Router v7, Astro, Waku).
 *
 * Skips the validation library axis because these templates set
 * [ProjectTemplate.supportsValidationSelection] to false — running them with
 * either ZOD or SURY produces identical files.
 */
class NewReactTemplatesIntegrationTest {
    private val pnpm: String = System.getProperty("template.test.pnpm", "pnpm")

    @ParameterizedTest(name = "{0} installs and rescript-builds")
    @MethodSource("newReactTemplates")
    fun template(
        template: ProjectTemplate,
        @TempDir tempDir: Path,
    ) {
        IntegrationTestSupport.requireBinary(pnpm)

        val ctx =
            TemplateContext(
                "demo-${template.name.lowercase()}",
                PackageManager.PNPM,
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
            "pnpm install failed for ${template.displayName}\nstdout=${install.stdout}\nstderr=${install.stderr}",
        )

        val rescript = IntegrationTestSupport.exec(tempDir, listOf(pnpm, "exec", "rescript"))
        assertTrue(
            rescript.succeeded,
            "rescript build failed for ${template.displayName}\nstdout=${rescript.stdout}\nstderr=${rescript.stderr}",
        )

        val test = IntegrationTestSupport.exec(tempDir, listOf(pnpm, "test"))
        assertTrue(
            test.succeeded,
            "pnpm test failed for ${template.displayName}\nstdout=${test.stdout}\nstderr=${test.stderr}",
        )
    }

    companion object {
        @JvmStatic
        fun newReactTemplates(): Stream<Arguments> =
            listOf(
                ProjectTemplate.TANSTACK_START,
                ProjectTemplate.REMIX_RR_V7,
                ProjectTemplate.ASTRO,
                ProjectTemplate.WAKU,
            ).stream().map { Arguments.of(it) }
    }
}
