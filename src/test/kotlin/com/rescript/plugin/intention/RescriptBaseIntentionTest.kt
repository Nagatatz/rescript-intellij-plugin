package com.rescript.plugin.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptBaseIntentionTest {
    @Test
    fun testGetFamilyNameDelegatesToGetText() {
        val intention =
            object : RescriptBaseIntention() {
                override fun getText(): String = "Test action"

                override fun isAvailableInRescript(
                    project: Project,
                    editor: Editor?,
                    element: PsiElement,
                ): Boolean = true

                override fun invoke(
                    project: Project,
                    editor: Editor?,
                    element: PsiElement,
                ) {}
            }
        assertEquals("Test action", intention.familyName)
    }

    @Test
    fun testGetFamilyNameFollowsDynamicTextChanges() {
        var dynamicText = "Initial"
        val intention =
            object : RescriptBaseIntention() {
                override fun getText(): String = dynamicText

                override fun isAvailableInRescript(
                    project: Project,
                    editor: Editor?,
                    element: PsiElement,
                ): Boolean = true

                override fun invoke(
                    project: Project,
                    editor: Editor?,
                    element: PsiElement,
                ) {}
            }
        assertEquals("Initial", intention.familyName)
        dynamicText = "Changed"
        assertEquals("Changed", intention.familyName)
    }

    @Test
    fun testAllConcreteIntentionsExtendBaseIntention() {
        val intentions =
            listOf(
                RescriptAddIgnoreIntention(),
                RescriptAddGenTypeIntention(),
                RescriptAddToInterfaceIntention(),
                RescriptAddTypeAnnotationIntention(),
                RescriptAddUnderscorePrefixIntention(),
                RescriptCaseSplitIntention(),
                RescriptConvertFunctionCallToPipeIntention(),
                RescriptConvertPipeToFunctionCallIntention(),
                RescriptConvertToLabeledArgsIntention(),
                RescriptExpandDestructuringIntention(),
                RescriptFilterMapChainIntention(),
                RescriptFixIdentifierCaseIntention(),
                RescriptGenerateDocCommentIntention(),
                RescriptInsertLabeledArgsIntention(),
                RescriptMergeSwitchCasesIntention(),
                RescriptRemoveFromInterfaceIntention(),
                RescriptRemoveParenthesesIntention(),
                RescriptRemoveQualifierIntention(),
                RescriptRemoveRedundantBracesIntention(),
                RescriptWrapWithSomeIntention(),
                RescriptWrapWithOkIntention(),
                RescriptWrapWithErrorIntention(),
            )
        for (intention in intentions) {
            assertTrue(
                intention is RescriptBaseIntention,
                "${intention::class.simpleName} should extend RescriptBaseIntention",
            )
        }
    }

    @Test
    fun testFixIdentifierCaseHasDistinctFamilyName() {
        val intention = RescriptFixIdentifierCaseIntention()
        assertEquals("Fix identifier case", intention.familyName)
    }

    @Test
    fun testFamilyNameMatchesTextForStandardIntentions() {
        val intentions =
            listOf(
                RescriptAddIgnoreIntention(),
                RescriptAddGenTypeIntention(),
                RescriptAddToInterfaceIntention(),
                RescriptRemoveFromInterfaceIntention(),
                RescriptRemoveParenthesesIntention(),
                RescriptRemoveQualifierIntention(),
            )
        for (intention in intentions) {
            assertEquals(
                intention.text,
                intention.familyName,
                "${intention::class.simpleName} should have matching text and familyName",
            )
        }
    }
}
