package com.rescript.plugin.intention

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.LightVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Shape tests for [ConstructorOccurrenceKind] and [RescriptConstructorOccurrence]
 * — the value objects passed between the classifier, finder, and rename intention.
 */
class RescriptConstructorOccurrenceTest {
    @Test
    fun `ConstructorOccurrenceKind has the expected four constants in declared order`() {
        assertEquals(
            listOf("CONSTRUCTOR", "PATTERN", "MODULE_QUALIFIED_TAIL", "OTHER"),
            ConstructorOccurrenceKind.entries.map { it.name },
        )
    }

    @Test
    fun `RescriptConstructorOccurrence retains every property verbatim`() {
        val file = LightVirtualFile("Main.res")
        val range = TextRange(10, 13)
        val occ = RescriptConstructorOccurrence(file, range, ConstructorOccurrenceKind.CONSTRUCTOR)
        assertEquals(file, occ.file)
        assertEquals(range, occ.range)
        assertEquals(ConstructorOccurrenceKind.CONSTRUCTOR, occ.kind)
    }

    @Test
    fun `RescriptConstructorOccurrence equality is by all fields`() {
        val file = LightVirtualFile("Main.res")
        val range = TextRange(0, 3)
        val a = RescriptConstructorOccurrence(file, range, ConstructorOccurrenceKind.PATTERN)
        val b = RescriptConstructorOccurrence(file, range, ConstructorOccurrenceKind.PATTERN)
        val differentKind = a.copy(kind = ConstructorOccurrenceKind.OTHER)
        assertEquals(a, b)
        assertNotEquals(a, differentKind)
    }
}
