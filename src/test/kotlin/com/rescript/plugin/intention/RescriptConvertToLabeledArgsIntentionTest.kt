package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [RescriptConvertToLabeledArgsIntention] utility methods.
 *
 * Note: Full intention integration tests require IntelliJ Platform test fixtures.
 * The LSP-dependent invoke() behavior is tested via manual QA.
 */
class RescriptConvertToLabeledArgsIntentionTest {
    @Test
    fun `intention text is correct`() {
        val intention = RescriptConvertToLabeledArgsIntention()
        assertEquals("Convert to labeled arguments", intention.text)
        assertEquals("Convert to labeled arguments", intention.familyName)
    }
}
