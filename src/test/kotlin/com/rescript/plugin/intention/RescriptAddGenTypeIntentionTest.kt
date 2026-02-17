package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptAddGenTypeIntentionTest {
    @Test
    fun testIntentionText() {
        val intention = RescriptAddGenTypeIntention()
        assertEquals("Add @genType annotation", intention.text)
    }

    @Test
    fun testIntentionFamilyName() {
        val intention = RescriptAddGenTypeIntention()
        assertEquals("Add @genType annotation", intention.familyName)
    }
}
