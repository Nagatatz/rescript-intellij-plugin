package com.rescript.plugin.wizard.templates

import com.rescript.plugin.wizard.PackageManager
import com.rescript.plugin.wizard.ValidationLibrary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for the shared wizard template frame.
 *
 * Byte equivalence with the pre-scaffold per-template code is enforced
 * separately by [TemplateGoldenTest]; these tests pin the scaffold's
 * own contract (key sets, ordering, variant resolution, dependency
 * switching) so failures point at the frame instead of a template.
 */
class TemplateScaffoldTest {
    private val ctx = TemplateContext("demo-app", PackageManager.PNPM, year = 2026)

    @Test
    fun `commonTail produces the seven housekeeping files in insertion order`() {
        val tail = TemplateScaffold.commonTail(ctx, readme = "# Demo")
        assertEquals(
            listOf(
                ".nvmrc",
                "LICENSE",
                ".github/dependabot.yml",
                "README.md",
                ".gitignore",
                ".editorconfig",
                ".github/workflows/ci.yml",
            ),
            tail.keys.toList(),
        )
        assertEquals("# Demo", tail["README.md"])
        assertTrue(tail["LICENSE"]!!.contains("demo-app"))
    }

    @Test
    fun `commonTail forwards gitignore extras and ci flags`() {
        val tail =
            TemplateScaffold.commonTail(
                ctx,
                readme = "# Demo",
                gitignoreExtra = listOf("dist/"),
                ciHasBuild = true,
                ciHasTest = true,
            )
        assertTrue(tail[".gitignore"]!!.contains("dist/"))
        assertEquals(tail[".gitignore"], CommonFiles.gitignore(listOf("dist/")))
        assertEquals(
            CommonFiles.ciWorkflow(ctx, hasBuild = true, hasTest = true),
            tail[".github/workflows/ci.yml"],
        )
    }

    @Test
    fun `resourceFiles loads each key relative to the resource root`() {
        val files =
            TemplateScaffold.resourceFiles(
                "basic",
                listOf("src/Args.res", "src/Files.res"),
            )
        assertEquals(listOf("src/Args.res", "src/Files.res"), files.keys.toList())
        assertEquals(TemplateResourceLoader.load("basic/src/Args.res"), files["src/Args.res"])
    }

    @Test
    fun `validationVariant resolves the zod and sury variant paths`() {
        val zod = TemplateScaffold.validationVariant(ctx, "basic")
        assertEquals("src/Validation.res", zod.first)
        assertEquals(TemplateResourceLoader.load("basic/variants/zod/src/Validation.res"), zod.second)

        val sury =
            TemplateScaffold.validationVariant(
                ctx.copy(validationLibrary = ValidationLibrary.SURY),
                "basic",
            )
        assertEquals(TemplateResourceLoader.load("basic/variants/sury/src/Validation.res"), sury.second)
    }

    @Test
    fun `validationDependency switches between zod and sury`() {
        assertEquals("zod" to TemplateVersions.ZOD, TemplateScaffold.validationDependency(ctx))
        assertEquals(
            "sury" to TemplateVersions.SURY,
            TemplateScaffold.validationDependency(ctx.copy(validationLibrary = ValidationLibrary.SURY)),
        )
    }

    @Test
    fun `standardDependencies lists the compiler trio then the validation library`() {
        val deps = TemplateScaffold.standardDependencies(ctx)
        assertEquals(listOf("rescript", "@rescript/core", "@rescript/runtime", "zod"), deps.keys.toList())
        assertEquals(TemplateVersions.RESCRIPT, deps["rescript"])
    }
}
