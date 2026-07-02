package com.rescript.plugin.quickfix

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptTypeHoleQuickFixTest {
    @Test
    fun `detectTypeHoles finds type annotation hole`() {
        val source = "let x: _ = 42"
        val holes = RescriptTypeHoleQuickFix.detectTypeHoles(source)
        assertEquals(1, holes.size)
    }

    @Test
    fun `detectTypeHoles finds multiple holes`() {
        val source =
            """
            let x: _ = 42
            let y: _ = "hello"
            """.trimIndent()
        val holes = RescriptTypeHoleQuickFix.detectTypeHoles(source)
        assertEquals(2, holes.size)
    }

    @Test
    fun `detectTypeHoles finds type declaration hole`() {
        val source = "type t = _"
        val holes = RescriptTypeHoleQuickFix.detectTypeHoles(source)
        assertEquals(1, holes.size)
    }

    @Test
    fun `detectTypeHoles returns empty for no holes`() {
        val source = "let x: int = 42"
        val holes = RescriptTypeHoleQuickFix.detectTypeHoles(source)
        assertTrue(holes.isEmpty())
    }

    @Test
    fun `detectTypeHoles reports correct line numbers`() {
        val source =
            """
            let x = 42
            let y: _ = "hello"
            """.trimIndent()
        val holes = RescriptTypeHoleQuickFix.detectTypeHoles(source)
        assertEquals(1, holes.size)
        assertEquals(2, holes[0].line)
    }

    @Test
    fun `suggestTypes returns common types`() {
        val types = RescriptTypeHoleQuickFix.suggestTypes()
        assertTrue(types.contains("string"))
        assertTrue(types.contains("int"))
        assertTrue(types.contains("float"))
        assertTrue(types.contains("bool"))
        assertTrue(types.contains("unit"))
    }

    @Test
    fun `suggestTypes includes generic types`() {
        val types = RescriptTypeHoleQuickFix.suggestTypes()
        assertTrue(types.any { it.contains("option") })
        assertTrue(types.any { it.contains("array") })
    }

    @Test
    fun `COMMON_TYPES has expected count`() {
        assertEquals(8, RescriptTypeHoleQuickFix.COMMON_TYPES.size)
    }

    @Test
    fun `TypeHole data class holds values`() {
        val hole = RescriptTypeHoleQuickFix.Companion.TypeHole(10, 1, ": _ =")
        assertEquals(10, hole.offset)
        assertEquals(1, hole.line)
        assertEquals(": _ =", hole.fullMatch)
    }

    @Test
    fun `inspection can be instantiated`() {
        val inspection: Any = RescriptTypeHoleQuickFix()
        assertTrue(inspection is com.intellij.codeInspection.LocalInspectionTool)
    }

    @Test
    fun `detectTypeHoles produces correct offsets for multi-line multi-hole source`() {
        // "let x: _ = 42\nlet y: _ = 0"
        // line 0: "let x: _ = 42"  (length 13, '\n' at index 13)
        //   `: _ =` matches at line index 5; `_` is at match offset 2 → absolute offset 7
        // line 1: "let y: _ = 0"  starts at absolute offset 14 (13 + 1 for '\n')
        //   `: _ =` matches at line index 5; `_` is at match offset 2 → absolute offset 21
        val source = "let x: _ = 42\nlet y: _ = 0"
        val holes = RescriptTypeHoleQuickFix.detectTypeHoles(source)
        assertEquals(2, holes.size)
        assertEquals(7, holes[0].offset)
        assertEquals(21, holes[1].offset)
        // Sanity-check: the character at each offset really is `_`
        assertEquals('_', source[holes[0].offset])
        assertEquals('_', source[holes[1].offset])
    }
}
