package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.ApiStrategy
import com.rescript.plugin.wizard.Database
import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ProjectTemplate
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * Characterization (golden) test for all 22 wizard project templates.
 *
 * For every (template, context) combination it digests the generated files into a
 * stable `SHA-256  fileKey` manifest and compares it against a checked-in golden
 * file, acting as a byte-equivalence regression detector for later refactoring.
 *
 * Golden update mode: when the system property `wizard.golden.update=true` (or the
 * environment variable `WIZARD_GOLDEN_UPDATE=true`) is set, each combo's manifest is
 * written to the source-tree `testData` directory instead of being asserted, and the
 * test passes without failing. Regenerate the goldens with:
 *
 *   WIZARD_GOLDEN_UPDATE=true ./gradlew test \
 *     --tests "com.rescript.plugin.wizard.templates.TemplateGoldenTest"
 *
 * The LICENSE copyright year is pinned to [GOLDEN_YEAR] so the output stays
 * deterministic across calendar years.
 */
class TemplateGoldenTest {
    private companion object {
        /** Pinned copyright year so golden manifests do not drift across calendar years. */
        const val GOLDEN_YEAR = 2026

        /** Relative location of the golden manifests within the source tree. */
        const val GOLDEN_DIR = "src/test/testData/wizard-golden"

        /** Returns true when golden regeneration is requested via property or env var. */
        fun isUpdateMode(): Boolean =
            System.getProperty("wizard.golden.update") == "true" ||
                System.getenv("WIZARD_GOLDEN_UPDATE") == "true"
    }

    /**
     * A single golden combination: a template plus a fully-resolved context and the
     * file-name-safe key used to locate its golden manifest.
     */
    private data class Combo(
        val template: ProjectTemplate,
        val comboKey: String,
        val ctx: TemplateContext,
    ) {
        /** File name of the golden manifest for this combo. */
        fun goldenFileName(): String = "${template.name}__$comboKey.txt"
    }

    @TestFactory
    fun `template output matches golden manifest`(): List<DynamicTest> {
        val goldenDir = resolveGoldenDir()
        return buildCombos().map { combo ->
            DynamicTest.dynamicTest("${combo.template.name} [${combo.comboKey}]") {
                val manifest = manifestFor(combo)
                val goldenFile = goldenDir.resolve(combo.goldenFileName())
                if (isUpdateMode()) {
                    Files.createDirectories(goldenDir)
                    Files.writeString(goldenFile, manifest)
                    return@dynamicTest
                }
                if (!Files.exists(goldenFile)) {
                    throw AssertionError(
                        "Missing golden file: ${combo.goldenFileName()}. " +
                            "Regenerate with: WIZARD_GOLDEN_UPDATE=true ./gradlew test " +
                            "--tests \"com.rescript.plugin.wizard.templates.TemplateGoldenTest\"",
                    )
                }
                val expected = Files.readString(goldenFile)
                assertEquals(
                    expected,
                    manifest,
                    "Golden mismatch for ${combo.goldenFileName()}. " +
                        "If this change is intentional, regenerate with: " +
                        "WIZARD_GOLDEN_UPDATE=true ./gradlew test " +
                        "--tests \"com.rescript.plugin.wizard.templates.TemplateGoldenTest\"",
                )
            }
        }
    }

    /**
     * Builds the full combo matrix programmatically from each template's declared
     * capability flags rather than a hardcoded list.
     */
    private fun buildCombos(): List<Combo> {
        val combos = mutableListOf<Combo>()
        for (template in ProjectTemplate.entries) {
            // Baseline: every template gets pnpm+zod and bun+zod.
            combos += combo(template, "pnpm-zod", PackageManager.PNPM)
            combos += combo(template, "bun-zod", PackageManager.BUN)

            if (template.supportsValidationSelection) {
                combos +=
                    combo(
                        template,
                        "pnpm-sury",
                        PackageManager.PNPM,
                        validation = ValidationLibrary.SURY,
                    )
            }

            if (template.supportsDatabaseSelection) {
                combos +=
                    combo(
                        template,
                        "pnpm-zod-postgres",
                        PackageManager.PNPM,
                        database = Database.POSTGRES,
                    )
                combos +=
                    combo(
                        template,
                        "pnpm-zod-mysql",
                        PackageManager.PNPM,
                        database = Database.MYSQL,
                    )
            }

            if (template == ProjectTemplate.FULL_STACK) {
                combos +=
                    combo(
                        template,
                        "pnpm-zod-graphql",
                        PackageManager.PNPM,
                        apiStrategy = ApiStrategy.GRAPHQL,
                    )
            }

            if (template == ProjectTemplate.MONOREPO) {
                combos += combo(template, "npm-zod", PackageManager.NPM)
            }
        }
        return combos
    }

    /** Constructs a [Combo] with a deterministic, year-pinned context. */
    private fun combo(
        template: ProjectTemplate,
        comboKey: String,
        packageManager: PackageManager,
        validation: ValidationLibrary = ValidationLibrary.ZOD,
        apiStrategy: ApiStrategy = ApiStrategy.REST,
        database: Database = Database.LIBSQL,
    ): Combo =
        Combo(
            template,
            comboKey,
            TemplateContext(
                projectName = "demo",
                packageManager = packageManager,
                validationLibrary = validation,
                apiStrategy = apiStrategy,
                database = database,
                year = GOLDEN_YEAR,
            ),
        )

    /**
     * Generates the template files and renders them as a sorted `SHA-256  fileKey`
     * manifest (two-space separator, trailing newline).
     */
    private fun manifestFor(combo: Combo): String {
        val files = combo.template.generateFiles(combo.ctx)
        return files.keys
            .sorted()
            .joinToString(separator = "\n", postfix = "\n") { key ->
                "${sha256Hex(files.getValue(key))}  $key"
            }
    }

    /** Returns the lowercase hex SHA-256 digest of [content] (UTF-8 bytes). */
    private fun sha256Hex(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(content.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Resolves the golden directory relative to the working directory. Walks up a few
     * parent levels because the test JVM working directory can be the project root or a
     * Gradle-managed subdirectory, looking for the source tree that owns the goldens.
     */
    private fun resolveGoldenDir(): Path {
        val start = Path.of(System.getProperty("user.dir")).toAbsolutePath()
        var dir: Path? = start
        repeat(6) {
            val base = dir ?: return@repeat
            // The source tree is identified by the presence of src/test alongside it.
            if (Files.isDirectory(base.resolve("src/test"))) {
                return base.resolve(GOLDEN_DIR)
            }
            dir = base.parent
        }
        // Source tree not found: in update mode this is fatal; otherwise fall back to
        // the working-directory-relative path so read-mode failures stay descriptive.
        if (isUpdateMode()) {
            throw IllegalStateException(
                "Cannot locate '$GOLDEN_DIR' from working directory '$start'. " +
                    "Run the test from the project root so the source tree is reachable.",
            )
        }
        return start.resolve(GOLDEN_DIR)
    }
}
