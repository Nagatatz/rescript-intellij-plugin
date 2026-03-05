package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptInsertLabeledArgsIntention] utility methods.
 *
 * Note: Full intention integration tests require IntelliJ Platform test fixtures.
 * The LSP-dependent invoke() behavior is tested via manual QA.
 */
class RescriptInsertLabeledArgsIntentionTest {
    @Test
    fun `intention text is correct`() {
        val intention = RescriptInsertLabeledArgsIntention()
        assertEquals("Insert labeled arguments", intention.text)
        assertEquals("Insert labeled arguments", intention.familyName)
    }
}
