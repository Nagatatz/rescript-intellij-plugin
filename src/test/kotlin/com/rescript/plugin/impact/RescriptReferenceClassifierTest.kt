package com.rescript.plugin.impact

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Drives [RescriptReferenceClassifier.classify] against representative
 * snippets to lock in the token-based heuristic. Each test feeds a
 * small ReScript fragment and asserts the kind detected at the
 * reference offset.
 */
class RescriptReferenceClassifierTest {
    private fun offsetOf(
        source: String,
        marker: String,
    ): Int = source.indexOf(marker).also { require(it >= 0) }

    @Test
    fun `colon prefix means type annotation`() {
        val source = "let x: t = 0"
        assertEquals(TypeRefKind.TYPE_REF, RescriptReferenceClassifier.classify(source, offsetOf(source, "t = 0")))
    }

    @Test
    fun `dot prefix means field access`() {
        val source = "let n = user.t"
        // The 't' we want is the last character of the source.
        val offset = source.length - 1
        assertEquals(TypeRefKind.FIELD_ACCESS, RescriptReferenceClassifier.classify(source, offset))
    }

    @Test
    fun `pipe prefix on uppercase identifier with paren is constructor`() {
        val source = "switch v {\n| Some(x) => x\n| None => 0\n}"
        val offset = source.indexOf("Some")
        assertEquals(TypeRefKind.CONSTRUCTOR, RescriptReferenceClassifier.classify(source, offset))
    }

    @Test
    fun `pipe prefix on lowercase identifier is pattern`() {
        val source = "switch v {\n| name => name\n| _ => empty\n}"
        val offset = source.indexOf("name")
        assertEquals(TypeRefKind.PATTERN, RescriptReferenceClassifier.classify(source, offset))
    }

    @Test
    fun `bare uppercase identifier with paren outside switch is constructor`() {
        val source = "let result = Ok(42)"
        assertEquals(TypeRefKind.CONSTRUCTOR, RescriptReferenceClassifier.classify(source, offsetOf(source, "Ok")))
    }

    @Test
    fun `lowercase identifier without recognisable prefix is unknown`() {
        val source = "let value = compute(x)"
        // 'compute' has whitespace before it → no structural prefix → fallback
        assertEquals(TypeRefKind.UNKNOWN, RescriptReferenceClassifier.classify(source, offsetOf(source, "compute")))
    }

    @Test
    fun `start of file falls back to unknown`() {
        val source = "type t = int"
        assertEquals(TypeRefKind.UNKNOWN, RescriptReferenceClassifier.classify(source, 0))
    }

    @Test
    fun `colon then space then identifier still type ref`() {
        val source = "let f = (x:    User) => x"
        assertEquals(TypeRefKind.TYPE_REF, RescriptReferenceClassifier.classify(source, offsetOf(source, "User")))
    }
}
