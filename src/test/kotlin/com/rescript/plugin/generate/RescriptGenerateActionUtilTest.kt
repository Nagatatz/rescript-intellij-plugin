package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rescript.plugin.lang.psi.RescriptElementTypes

/**
 * Tests for [RescriptGenerateActionUtil] — utility methods used by Generate actions
 * to find enclosing PSI declarations relative to the caret position.
 *
 * Uses IDE fixtures to create real PSI trees and verifies caret-based declaration lookup.
 */
class RescriptGenerateActionUtilTest : BasePlatformTestCase() {
    private fun createActionEvent(): AnActionEvent {
        val editor = myFixture.editor
        val psiFile = myFixture.file
        val dataContext =
            DataContext { dataId ->
                when (dataId) {
                    CommonDataKeys.EDITOR.name -> editor
                    CommonDataKeys.PSI_FILE.name -> psiFile
                    CommonDataKeys.PROJECT.name -> project
                    else -> null
                }
            }
        return AnActionEvent.createFromDataContext("test", Presentation(), dataContext)
    }

    fun testFindEnclosingLetDeclaration() {
        myFixture.configureByText("Test.res", "let x<caret> = 1")
        val event = createActionEvent()
        val result =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                event,
                RescriptElementTypes.LET_DECLARATION,
            )
        assertNotNull("Should find enclosing LET_DECLARATION", result)
    }

    fun testFindEnclosingTypeDeclaration() {
        myFixture.configureByText("Test.res", "type t<caret> = int")
        val event = createActionEvent()
        val result =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                event,
                RescriptElementTypes.TYPE_DECLARATION,
            )
        assertNotNull("Should find enclosing TYPE_DECLARATION", result)
    }

    fun testFindEnclosingReturnsNullForNonRescriptFile() {
        myFixture.configureByText("Test.txt", "let x<caret> = 1")
        val event = createActionEvent()
        val result =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                event,
                RescriptElementTypes.LET_DECLARATION,
            )
        assertNull("Should return null for non-ReScript file", result)
    }

    fun testIsInsideDeclarationReturnsFalseForNonRescriptFile() {
        myFixture.configureByText("Test.txt", "some text<caret> here")
        val event = createActionEvent()
        val result =
            RescriptGenerateActionUtil.isInsideDeclaration(
                event,
                RescriptElementTypes.LET_DECLARATION,
            )
        assertFalse("Should return false for non-ReScript file", result)
    }
}
