package com.rescript.plugin.imports

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptModuleMemberExtractor], the pure-syntax extractor of a module's
 * brace-depth-0 declaration names.
 */
class RescriptModuleMemberExtractorTest {
    @Test
    fun `extracts let and let rec bindings`() {
        val names =
            RescriptModuleMemberExtractor.extractTopLevelNames(
                """
                let foo = 1
                let rec bar = () => bar()
                """.trimIndent(),
            )
        assertEquals(setOf("foo", "bar"), names)
    }

    @Test
    fun `extracts type module external and exception names`() {
        val names =
            RescriptModuleMemberExtractor.extractTopLevelNames(
                """
                type point = {x: int, y: int}
                module Inner = {}
                external raw: string => unit = "raw"
                exception MyError
                """.trimIndent(),
            )
        assertTrue(names.containsAll(setOf("point", "Inner", "raw", "MyError")))
    }

    @Test
    fun `excludes declarations nested inside a submodule body`() {
        val names =
            RescriptModuleMemberExtractor.extractTopLevelNames(
                """
                module Inner = {
                  let hidden = 1
                }
                let visible = 2
                """.trimIndent(),
            )
        assertTrue(names.contains("Inner"))
        assertTrue(names.contains("visible"))
        assertFalse(names.contains("hidden"))
    }

    @Test
    fun `excludes bindings nested inside a function body`() {
        val names =
            RescriptModuleMemberExtractor.extractTopLevelNames(
                """
                let outer = () => {
                  let inner = 1
                  inner
                }
                """.trimIndent(),
            )
        assertTrue(names.contains("outer"))
        assertFalse(names.contains("inner"))
    }

    @Test
    fun `returns empty set for blank input`() {
        assertTrue(RescriptModuleMemberExtractor.extractTopLevelNames("").isEmpty())
        assertTrue(RescriptModuleMemberExtractor.extractTopLevelNames("// just a comment").isEmpty())
    }
}
