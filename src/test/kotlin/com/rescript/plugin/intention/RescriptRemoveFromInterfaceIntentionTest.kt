package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RescriptRemoveFromInterfaceIntentionTest {
    @Test
    fun testText() {
        val intention = RescriptRemoveFromInterfaceIntention()
        assertEquals("Remove declaration from interface", intention.text)
    }

    @Test
    fun testFamilyName() {
        val intention = RescriptRemoveFromInterfaceIntention()
        assertEquals("Remove declaration from interface", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptRemoveFromInterfaceIntention()
        assertEquals(true, intention.startInWriteAction())
    }
}
