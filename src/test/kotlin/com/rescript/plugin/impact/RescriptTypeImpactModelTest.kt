package com.rescript.plugin.impact

import com.intellij.testFramework.LightVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Shape tests for the Type Impact panel's domain model: [TypeTarget],
 * [TypeRefKind] and [ReferenceEntry]. These guard against enum reordering
 * and verify the data classes the panel renders.
 */
class RescriptTypeImpactModelTest {
    @Test
    fun `TypeTarget retains every property verbatim`() {
        val file = LightVirtualFile("Types.res")
        val target =
            TypeTarget(
                name = "User.t",
                localName = "t",
                declarationFile = file,
                declarationOffset = 128,
            )
        assertEquals("User.t", target.name)
        assertEquals("t", target.localName)
        assertEquals(file, target.declarationFile)
        assertEquals(128, target.declarationOffset)
    }

    @Test
    fun `TypeTarget equality is by all fields`() {
        val file = LightVirtualFile("Types.res")
        val a = TypeTarget("User.t", "t", file, 10)
        val b = TypeTarget("User.t", "t", file, 10)
        val differentOffset = a.copy(declarationOffset = 11)
        assertEquals(a, b)
        assertNotEquals(a, differentOffset)
    }

    @Test
    fun `TypeRefKind has the expected five constants in declared order`() {
        assertEquals(
            listOf("TYPE_REF", "CONSTRUCTOR", "PATTERN", "FIELD_ACCESS", "UNKNOWN"),
            TypeRefKind.entries.map { it.name },
        )
    }

    @Test
    fun `ReferenceEntry retains every property verbatim`() {
        val file = LightVirtualFile("Main.res")
        val entry =
            ReferenceEntry(
                file = file,
                offset = 5,
                lineNumber = 2,
                previewLine = "let u : User.t = ...",
                kind = TypeRefKind.TYPE_REF,
            )
        assertEquals(file, entry.file)
        assertEquals(5, entry.offset)
        assertEquals(2, entry.lineNumber)
        assertEquals("let u : User.t = ...", entry.previewLine)
        assertEquals(TypeRefKind.TYPE_REF, entry.kind)
    }

    @Test
    fun `ReferenceEntry equality is by all fields`() {
        val file = LightVirtualFile("Main.res")
        val a = ReferenceEntry(file, 0, 1, "x", TypeRefKind.TYPE_REF)
        val b = ReferenceEntry(file, 0, 1, "x", TypeRefKind.TYPE_REF)
        val different = a.copy(kind = TypeRefKind.PATTERN)
        assertEquals(a, b)
        assertNotEquals(a, different)
    }
}
