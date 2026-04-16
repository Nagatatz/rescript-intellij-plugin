package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [RescriptGenerateActionUtil] — utility methods used by Generate actions
 * to find enclosing PSI declarations relative to the caret position.
 *
 * Uses IDE fixtures to create real PSI trees and verifies caret-based declaration lookup.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptGenerateActionUtilTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

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
        return AnActionEvent.createEvent(dataContext, Presentation(), "test", ActionUiKind.NONE, null)
    }

    @Test
    fun testFindEnclosingLetDeclaration() {
        myFixture.configureByText("Test.res", "let x<caret> = 1")
        val event = createActionEvent()
        val result =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                event,
                RescriptElementTypes.LET_DECLARATION,
            )
        assertNotNull(result, "Should find enclosing LET_DECLARATION")
    }

    @Test
    fun testFindEnclosingTypeDeclaration() {
        myFixture.configureByText("Test.res", "type t<caret> = int")
        val event = createActionEvent()
        val result =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                event,
                RescriptElementTypes.TYPE_DECLARATION,
            )
        assertNotNull(result, "Should find enclosing TYPE_DECLARATION")
    }

    @Test
    fun testFindEnclosingReturnsNullForNonRescriptFile() {
        myFixture.configureByText("Test.txt", "let x<caret> = 1")
        val event = createActionEvent()
        val result =
            RescriptGenerateActionUtil.findEnclosingDeclaration(
                event,
                RescriptElementTypes.LET_DECLARATION,
            )
        assertNull(result, "Should return null for non-ReScript file")
    }

    @Test
    fun testIsInsideDeclarationReturnsFalseForNonRescriptFile() {
        myFixture.configureByText("Test.txt", "some text<caret> here")
        val event = createActionEvent()
        val result =
            RescriptGenerateActionUtil.isInsideDeclaration(
                event,
                RescriptElementTypes.LET_DECLARATION,
            )
        assertFalse(result, "Should return false for non-ReScript file")
    }
}
