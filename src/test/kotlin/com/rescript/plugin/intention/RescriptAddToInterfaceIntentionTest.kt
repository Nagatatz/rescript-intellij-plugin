package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RescriptAddToInterfaceIntentionTest {
    @Test
    fun testText() {
        val intention = RescriptAddToInterfaceIntention()
        assertEquals("Add declaration to interface file", intention.text)
    }

    @Test
    fun testFamilyName() {
        val intention = RescriptAddToInterfaceIntention()
        assertEquals("Add declaration to interface file", intention.familyName)
    }

    @Test
    fun testStartInWriteAction() {
        val intention = RescriptAddToInterfaceIntention()
        assertEquals(true, intention.startInWriteAction())
    }
}
