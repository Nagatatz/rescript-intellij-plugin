package com.rescript.plugin.intention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        val element = stubPsiElement()
        val project = stubProject()
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

    // -- Helper stubs --

    private fun stubPsiElement(): com.intellij.psi.PsiElement =
        java.lang.reflect.Proxy.newProxyInstance(
            com.intellij.psi.PsiElement::class.java.classLoader,
            arrayOf(com.intellij.psi.PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getContainingFile" -> null
                "toString" -> "StubElement"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as com.intellij.psi.PsiElement

    private fun stubProject(): com.intellij.openapi.project.Project =
        java.lang.reflect.Proxy.newProxyInstance(
            com.intellij.openapi.project.Project::class.java.classLoader,
            arrayOf(com.intellij.openapi.project.Project::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "StubProject"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as com.intellij.openapi.project.Project
}
