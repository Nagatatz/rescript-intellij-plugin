package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptFlattenNestedSwitchIntention].
 *
 * Note: Full intention integration (availability against a real PSI tree and the
 * `invoke()` document rewrite) requires IntelliJ Platform test fixtures; the
 * flattening logic itself is covered by [RescriptNestedSwitchFlattenerTest].
 */
class RescriptFlattenNestedSwitchIntentionTest {
    @Test
    fun `intention text and family name are correct`() {
        val intention = RescriptFlattenNestedSwitchIntention()
        assertEquals("Flatten nested switch", intention.text)
        assertEquals("Flatten nested switch", intention.familyName)
    }
}
