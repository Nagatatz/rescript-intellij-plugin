package com.rescript.plugin.imports

import com.intellij.openapi.util.TextRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptOpenExpansionPlanner], the pure-syntax planner that computes the
 * qualifier-insertion offsets and open-statement deletion range for expanding `open M`.
 */
class RescriptOpenExpansionPlannerTest {
    /** Builds the `open <module>` statement range located at the start of [text]. */
    private fun openRange(
        text: String,
        statement: String,
    ): TextRange {
        val start = text.indexOf(statement)
        return TextRange(start, start + statement.length)
    }

    @Test
    fun `inserts a qualifier offset for each unqualified member reference`() {
        val text = "open M\nlet x = foo(bar)\n"
        val plan = RescriptOpenExpansionPlanner.plan(text, "M", setOf("foo", "bar"), openRange(text, "open M"))
        assertEquals(listOf(text.indexOf("foo"), text.indexOf("bar")), plan.prefixInsertOffsets)
    }

    @Test
    fun `deletion range swallows the trailing newline`() {
        val text = "open M\nlet x = foo\n"
        val plan = RescriptOpenExpansionPlanner.plan(text, "M", setOf("foo"), openRange(text, "open M"))
        // "open M" is 0..6, the trailing newline at index 6 is included so no blank line remains.
        assertEquals(TextRange(0, 7), plan.openDeletionRange)
    }

    @Test
    fun `skips occurrences that are already qualified`() {
        val text = "open M\nlet x = Other.foo\n"
        val plan = RescriptOpenExpansionPlanner.plan(text, "M", setOf("foo"), openRange(text, "open M"))
        assertTrue(plan.prefixInsertOffsets.isEmpty())
    }

    @Test
    fun `skips identifiers that are not members of the module`() {
        val text = "open M\nlet x = other\n"
        val plan = RescriptOpenExpansionPlanner.plan(text, "M", setOf("foo"), openRange(text, "open M"))
        assertTrue(plan.prefixInsertOffsets.isEmpty())
    }

    @Test
    fun `excludes a member that is shadowed by a local binding`() {
        val text = "open M\nlet foo = 1\nlet y = foo\n"
        val plan = RescriptOpenExpansionPlanner.plan(text, "M", setOf("foo"), openRange(text, "open M"))
        assertTrue(plan.prefixInsertOffsets.isEmpty())
    }

    @Test
    fun `ignores references appearing before the open statement`() {
        val text = "let before = foo\nopen M\nlet after = foo\n"
        val plan = RescriptOpenExpansionPlanner.plan(text, "M", setOf("foo"), openRange(text, "open M"))
        assertEquals(listOf(text.lastIndexOf("foo")), plan.prefixInsertOffsets)
    }

    @Test
    fun `returns no offsets but a deletion range when member set is empty`() {
        val text = "open M\nlet x = foo\n"
        val plan = RescriptOpenExpansionPlanner.plan(text, "M", emptySet(), openRange(text, "open M"))
        assertTrue(plan.prefixInsertOffsets.isEmpty())
        assertEquals(TextRange(0, 7), plan.openDeletionRange)
    }
}
