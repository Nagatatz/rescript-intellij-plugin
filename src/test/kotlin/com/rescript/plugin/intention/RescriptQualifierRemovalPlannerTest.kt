package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Covers local module identity, declaration order, lexical boundaries and refusal proofs. */
class RescriptQualifierRemovalPlannerTest {
    @Test
    fun `preceding local module and open remove only the matching qualifier`() {
        val before = "module A = { let value = 1 }\nopen A\nlet result = <caret>A.value"
        assertEquals("module A = { let value = 1 }\nopen A\nlet result = value", replacement(before))
    }

    @Test
    fun `source comments and trivia between qualifier tokens remain unchanged`() {
        val before = "module A = { let value = 1 }\nopen A\nlet result = <caret>A /* keep */ . /* read */ value"
        assertEquals(
            "module A = { let value = 1 }\nopen A\nlet result =  /* keep */  /* read */ value",
            replacement(before),
        )
    }

    @Test
    fun `module members may be used in nested calls and arithmetic`() {
        val before = "module A = { let value = 1 }\nopen A\nlet result = Int.toString((<caret>A.value + 2))"
        assertEquals(before.replace("<caret>A.value", "value"), replacement(before))
    }

    @Test
    fun `unique scalar exports retain the opened binding identity`() {
        listOf("true", "-2", "1.5", "\"text\"", "'a'", "()").forEach { scalar ->
            val before = "module A = { let value = $scalar }\nopen A\nlet result = <caret>A.value"
            assertEquals(before.replace("<caret>A.value", "value"), replacement(before), scalar)
        }
        val before = "module A = {\nlet value = 1\nlet other = 2\n}\nopen A\nlet result = <caret>A.value + other"
        assertEquals(before.replace("<caret>A.value", "value"), replacement(before))
    }

    @Test
    fun `caret outside the code identifier has no edit`() {
        val source = prefix + "let result = A.value"
        assertNull(RescriptQualifierRemovalPlanner.plan(source, -1))
        assertNull(RescriptQualifierRemovalPlanner.plan(source, source.length))
        assertNull(RescriptQualifierRemovalPlanner.plan(source, source.length + 1))
    }

    @Test
    fun `comments and strings do not introduce opens or references`() {
        refused(
            "module A = { let value = 1 }\n/*\nopen A\n*/\nlet result = <caret>A.value",
            "module A = { let value = 1 }\nlet text = \"open A\"\nlet result = <caret>A.value",
            "module A = { let value = 1 }\nopen A\nlet text = \"<caret>A.value\"",
            "module A = { let value = 1 }\nopen A\n// <caret>A.value\nlet result = 1",
        )
    }

    @Test
    fun `open or module declarations after a reference never prove identity`() {
        refused(
            "module A = { let value = 1 }\nlet result = <caret>A.value\nopen A",
            "open A\nlet result = <caret>A.value\nmodule A = { let value = 1 }",
        )
    }

    @Test
    fun `shadowing values or module names prevent removal`() {
        refused(
            prefix + "let value = 2\nlet result = <caret>A.value",
            "let value = 2\n" + prefix + "let result = <caret>A.value",
            prefix + "let result = <caret>A.value\nlet value = 2",
            prefix + "module A = { let value = 2 }\nlet result = <caret>A.value",
            "module A = { let value = 1\nlet value = 2 }\nopen A\nlet result = <caret>A.value",
        )
    }

    @Test
    fun `external modules aliases and reexports have no local symbol proof`() {
        refused(
            "open Array\nlet result = <caret>Array.length(items)",
            "module A = Other\nopen A\nlet result = <caret>A.value",
            "module A = { include Other }\nopen A\nlet result = <caret>A.value",
            "module A = { external value: int = \"value\" }\nopen A\nlet result = <caret>A.value",
        )
    }

    @Test
    fun `competing opens and include are refused regardless of order`() {
        refused(
            "open Other\n" + prefix + "let result = <caret>A.value",
            prefix + "open Other\nlet result = <caret>A.value",
            prefix + "let result = <caret>A.value\nopen Other",
            prefix + "include Other\nlet result = <caret>A.value",
        )
    }

    @Test
    fun `nested scopes parameters and typed or ambiguous bindings are refused`() {
        refused(
            prefix + "let run = () => { <caret>A.value }",
            prefix + "let run = value => <caret>A.value",
            prefix + "module B = { let result = <caret>A.value }",
            prefix + "let result: int = <caret>A.value",
            prefix + "let result = <caret>A.value a b",
            prefix + "let result = {value: <caret>A.value}",
        )
    }

    @Test
    fun `only a complete direct exported value path can lose its prefix`() {
        refused(
            prefix + "let result = <caret>A.unknown",
            prefix + "let result = <caret>A.Nested.value",
            prefix + "let result = Other.<caret>A.value",
            prefix + "let result = A.<caret>value",
        )
    }

    @Test
    fun `unfinished literals comments and unbalanced delimiters are refused`() {
        refused(
            prefix + "let result = (<caret>A.value]",
            prefix + "let result = <caret>A.value\n/* unfinished",
            prefix + "let result = <caret>A.value\nlet text = \"unfinished",
            "module A = { let value = unknown(1) }\nopen A\nlet result = <caret>A.value",
        )
    }

    @Test
    fun `prepared proof refuses changes anywhere in the input`() {
        val source = prefix + "let result = <caret>A.value"
        val offset = source.indexOf("<caret>")
        val text = source.replace("<caret>", "")
        val plan = RescriptQualifierRemovalPlanner.plan(text, offset)!!
        assertNull(plan.replacementFor(text + "\n// changed"))
        assertNull(plan.replacementFor(text.replace("let value = 1", "let value = 2")))
        assertEquals(text.replace("let result = A.value", "let result = value"), plan.replacementFor(text))
    }

    private fun replacement(source: String): String? {
        val offset = source.indexOf("<caret>")
        val text = source.replace("<caret>", "")
        return RescriptQualifierRemovalPlanner.plan(text, offset)?.replacementFor(text)
    }

    private fun refused(vararg sources: String) {
        sources.forEach { assertNull(replacement(it), it) }
    }

    companion object {
        private val prefix = "module A = { let value = 1 }\nopen A\n"
    }
}
