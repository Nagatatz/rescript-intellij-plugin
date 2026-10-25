package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Covers the registered qualifier intention's user-facing identity. */
class RescriptRemoveQualifierIntentionTest {
    @Test
    fun `intention text and family remain consistent`() {
        val intention = RescriptRemoveQualifierIntention()
        assertEquals("Remove redundant qualifier", intention.text)
        assertEquals("Remove redundant qualifier", intention.familyName)
    }
}
