package com.rescript.plugin.structure

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Regression tests for navigation from Structure View elements.
 *
 * `Navigatable.navigate` became `@ApiStatus.OverrideOnly` in 2026.3, so the element
 * routes through `OpenSourceUtil` instead of invoking the wrapped PSI element directly.
 * These tests pin the observable behaviour: the caret lands on the wrapped declaration.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptStructureViewElementNavigationTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun testNavigateMovesCaretToWrappedDeclaration() {
        val source = "let first = 1\n\nlet second = 2\n"
        val psiFile = myFixture.configureByText("Nav.res", source)
        val root = RescriptStructureViewElement(psiFile)
        val children = root.children.filterIsInstance<RescriptStructureViewElement>()
        assertEquals(2, children.size, "expected both let declarations in the structure view")

        children[1].navigate(true)
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        assertNotNull(editor, "navigation should leave an editor selected")
        assertEquals(source.indexOf("let second"), editor!!.caretModel.offset)
    }

    @Test
    fun testNavigateOpensFileThatIsNotYetOpen() {
        val other = myFixture.addFileToProject("Other.res", "let a = 1\n\nlet target = 2\n")
        myFixture.configureByText("Current.res", "let here = 0\n")
        val root = RescriptStructureViewElement(other)
        val target = root.children.filterIsInstance<RescriptStructureViewElement>().last()

        target.navigate(true)
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue()

        val manager = FileEditorManager.getInstance(project)
        assertEquals(other.virtualFile, manager.selectedFiles.firstOrNull())
        assertEquals(other.text.indexOf("let target"), manager.selectedTextEditor?.caretModel?.offset)
    }
}
