package com.rescript.plugin.inspection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptMutabilityInspectionTest {
    @Test
    fun `findRefBindings detects simple ref binding`() {
        val text = "let count = ref(0)"
        val refs = RescriptMutabilityInspection.findRefBindings(text)
        assertEquals(1, refs.size)
        assertEquals("count", refs[0].name)
        assertEquals("0", refs[0].value)
    }

    @Test
    fun `findRefBindings detects multiple ref bindings`() {
        val text =
            """
            let count = ref(0)
            let name = ref("hello")
            """.trimIndent()
        val refs = RescriptMutabilityInspection.findRefBindings(text)
        assertEquals(2, refs.size)
        assertEquals("count", refs[0].name)
        assertEquals("name", refs[1].name)
    }

    @Test
    fun `findRefBindings ignores non-ref bindings`() {
        val text = "let count = 0"
        val refs = RescriptMutabilityInspection.findRefBindings(text)
        assertTrue(refs.isEmpty())
    }

    @Test
    fun `findReassignments detects reassignment`() {
        val text = "count := count.contents + 1"
        val reassignments = RescriptMutabilityInspection.findReassignments(text)
        assertTrue("count" in reassignments)
    }

    @Test
    fun `findReassignments returns empty for no reassignments`() {
        val text = "let x = count.contents + 1"
        val reassignments = RescriptMutabilityInspection.findReassignments(text)
        assertTrue(reassignments.isEmpty())
    }

    @Test
    fun `removeRef replaces ref with value`() {
        val result = RescriptMutabilityInspection.removeRef("let count = ref(0)")
        assertEquals("let count = 0", result)
    }

    @Test
    fun `removeRef handles string value`() {
        val result = RescriptMutabilityInspection.removeRef("""let name = ref("hello")""")
        assertEquals("""let name = "hello"""", result)
    }

    @Test
    fun `removeRef returns null for non-ref binding`() {
        val result = RescriptMutabilityInspection.removeRef("let count = 0")
        assertNull(result)
    }

    @Test
    fun `inspection can be instantiated`() {
        assertNotNull(RescriptMutabilityInspection())
    }

    @Test
    fun `inspection is a LocalInspectionTool`() {
        val subject: Any = RescriptMutabilityInspection()
        assertTrue(subject is com.intellij.codeInspection.LocalInspectionTool)
    }

    @Test
    fun `RefBinding data class stores correct values`() {
        val ref = RescriptMutabilityInspection.Companion.RefBinding("x", "42", 10)
        assertEquals("x", ref.name)
        assertEquals("42", ref.value)
        assertEquals(10, ref.offset)
    }
}
