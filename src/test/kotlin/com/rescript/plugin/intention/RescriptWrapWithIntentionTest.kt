package com.rescript.plugin.intention

import com.rescript.plugin.RescriptTestUtils
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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

    // -- startInWriteAction tests --

    @Test
    fun testSomeStartInWriteAction() {
        val intention = RescriptWrapWithSomeIntention()
        assertTrue(intention.startInWriteAction())
    }

    @Test
    fun testOkStartInWriteAction() {
        val intention = RescriptWrapWithOkIntention()
        assertTrue(intention.startInWriteAction())
    }

    @Test
    fun testErrorStartInWriteAction() {
        val intention = RescriptWrapWithErrorIntention()
        assertTrue(intention.startInWriteAction())
    }

    // -- isAvailable returns false for null editor --

    @Test
    fun testIsAvailableReturnsFalseForNullEditor() {
        val intention = RescriptWrapWithSomeIntention()
        val element = RescriptTestUtils.stubPsiElement(RescriptElementTypes.LET_DECLARATION)
        val project = RescriptTestUtils.stubProject()
        assertFalse(intention.isAvailable(project, null, element))
    }

    // -- Text and family name consistency --

    @Test
    fun testTextAndFamilyNameAreConsistentForSome() {
        val intention = RescriptWrapWithSomeIntention()
        assertEquals(intention.text, intention.familyName)
    }

    @Test
    fun testTextAndFamilyNameAreConsistentForOk() {
        val intention = RescriptWrapWithOkIntention()
        assertEquals(intention.text, intention.familyName)
    }

    @Test
    fun testTextAndFamilyNameAreConsistentForError() {
        val intention = RescriptWrapWithErrorIntention()
        assertEquals(intention.text, intention.familyName)
    }
}
