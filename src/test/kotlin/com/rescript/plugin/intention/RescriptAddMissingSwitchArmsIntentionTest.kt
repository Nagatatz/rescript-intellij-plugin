package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Smoke test for the intention metadata. The behavioural logic is covered by
 * [RescriptMissingArmsBuilderTest]; this test simply guards the user-visible
 * label and family name from accidental regressions.
 */
class RescriptAddMissingSwitchArmsIntentionTest {
    @Test
    fun `getText returns the expected label`() {
        val intention = RescriptAddMissingSwitchArmsIntention()
        assertEquals("Add missing switch arms", intention.text)
    }

    @Test
    fun `getFamilyName matches getText`() {
        val intention = RescriptAddMissingSwitchArmsIntention()
        assertEquals(intention.text, intention.familyName)
    }
}
