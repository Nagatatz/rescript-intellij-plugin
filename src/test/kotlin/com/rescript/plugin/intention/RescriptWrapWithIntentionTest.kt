package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptWrapWithIntentionTest {
    @Test
    fun testSomeIntentionText() {
        val intention = RescriptWrapWithSomeIntention()
        assertEquals("Wrap with Some(...)", intention.text)
    }

    @Test
    fun testSomeIntentionFamilyName() {
        val intention = RescriptWrapWithSomeIntention()
        assertEquals("Wrap with Some(...)", intention.familyName)
    }

    @Test
    fun testOkIntentionText() {
        val intention = RescriptWrapWithOkIntention()
        assertEquals("Wrap with Ok(...)", intention.text)
    }

    @Test
    fun testOkIntentionFamilyName() {
        val intention = RescriptWrapWithOkIntention()
        assertEquals("Wrap with Ok(...)", intention.familyName)
    }

    @Test
    fun testErrorIntentionText() {
        val intention = RescriptWrapWithErrorIntention()
        assertEquals("Wrap with Error(...)", intention.text)
    }

    @Test
    fun testErrorIntentionFamilyName() {
        val intention = RescriptWrapWithErrorIntention()
        assertEquals("Wrap with Error(...)", intention.familyName)
    }
}
