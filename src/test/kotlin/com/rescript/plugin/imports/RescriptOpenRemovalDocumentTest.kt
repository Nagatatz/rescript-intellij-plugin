package com.rescript.plugin.imports

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.inspection.RescriptDuplicateOpenInspection
import com.rescript.plugin.settings.RescriptConfigurable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.Container
import javax.swing.JCheckBox

/** Applies the actual optimizer and inspection quick fix to documents and verifies undo. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptOpenRemovalDocumentTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    private val source = "module A = {let value = 1}\nopen A\nopen A\nConsole.log(value)"

    @Test
    fun `optimizer removes proven duplicate and undoes once`() {
        myFixture.configureByText("Opens.res", source)
        val action = RescriptImportOptimizer().processFile(myFixture.file)
        WriteCommandAction.runWriteCommandAction(project) { action.run() }
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(myFixture.editor.document)
        assertEquals(1, Regex("open A").findAll(myFixture.editor.document.text).count())
        assertEquals("Removed 1 duplicate open statement(s)", action.userNotificationInfo)
        undoToOriginal()
    }

    @Test
    fun `inspection quick fix shares proof and is undoable`() {
        myFixture.configureByText("Opens.res", source)
        val holder = ProblemsHolder(InspectionManager.getInstance(project), myFixture.file, true)
        RescriptDuplicateOpenInspection().buildVisitor(holder, true).visitFile(myFixture.file)
        assertEquals(1, holder.results.size)
        val descriptor = holder.results.single()
        val fix = descriptor.fixes!!.single()
        WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(myFixture.editor.document)
        assertEquals(1, Regex("open A").findAll(myFixture.editor.document.text).count())
        undoToOriginal()
    }

    @Test
    fun `collected optimization is discarded when document changes`() {
        for (commit in listOf(false, true)) {
            myFixture.configureByText("Opens.res", source)
            val action = RescriptImportOptimizer().processFile(myFixture.file)
            WriteCommandAction.runWriteCommandAction(project) {
                myFixture.editor.document.insertString(0, "// new snapshot\n")
            }
            if (commit) PsiDocumentManager.getInstance(project).commitDocument(myFixture.editor.document)
            val changed = myFixture.editor.document.text
            WriteCommandAction.runWriteCommandAction(project) { action.run() }
            assertEquals(changed, myFixture.editor.document.text)
            assertEquals("No open statements to remove", action.userNotificationInfo)
        }
    }

    private fun undoToOriginal(expected: String = source) {
        val undo = UndoManager.getInstance(project)
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(myFixture.file.virtualFile)
        assertTrue(undo.isUndoAvailable(editor))
        undo.undo(editor)
        assertEquals(expected, myFixture.editor.document.text)
    }

    @Test
    fun `optimization keeps trailing comments and following expressions`() {
        val original = "module A = {let value = 1}\nopen A\nopen A // keep this comment\nConsole.log(value)"
        myFixture.configureByText("Comments.res", original)
        val action = RescriptImportOptimizer().processFile(myFixture.file)
        WriteCommandAction.runWriteCommandAction(project) { action.run() }
        val edited = myFixture.editor.document.text
        assertEquals(1, Regex("open A").findAll(edited).count())
        assertTrue(edited.contains("// keep this comment"))
        assertTrue(edited.contains("Console.log(value)"))
        undoToOriginal(original)
    }

    @Test
    fun `reopens do not produce duplicate inspection warnings`() {
        myFixture.configureByText(
            "Reopens.res",
            "module A = {let value = 1}\nmodule B = {let value = 2}\nopen A\nopen B\nopen A",
        )
        val holder = ProblemsHolder(InspectionManager.getInstance(project), myFixture.file, true)
        RescriptDuplicateOpenInspection().buildVisitor(holder, true).visitFile(myFixture.file)
        assertTrue(holder.results.isEmpty())
        val original = myFixture.editor.document.text
        val action = RescriptImportOptimizer().processFile(myFixture.file)
        WriteCommandAction.runWriteCommandAction(project) { action.run() }
        assertEquals(original, myFixture.editor.document.text)
    }

    @Test
    fun `quick fix rechecks module identity after intervening open is added`() {
        myFixture.configureByText("Opens.res", source)
        val holder = ProblemsHolder(InspectionManager.getInstance(project), myFixture.file, true)
        RescriptDuplicateOpenInspection().buildVisitor(holder, true).visitFile(myFixture.file)
        val descriptor = holder.results.single()
        val fix = descriptor.fixes!!.single()
        val offset = descriptor.psiElement.textRange.startOffset
        WriteCommandAction.runWriteCommandAction(project) {
            myFixture.editor.document.insertString(offset, "open External\n")
        }
        PsiDocumentManager.getInstance(project).commitDocument(myFixture.editor.document)
        val changed = myFixture.editor.document.text
        WriteCommandAction.runWriteCommandAction(project) { fix.applyFix(project, descriptor) }
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(myFixture.editor.document)
        assertEquals(changed, myFixture.editor.document.text)
    }

    @Test
    fun `settings show unused open cleanup as unavailable`() {
        val configurable = RescriptConfigurable(project)
        try {
            val panel = configurable.createComponent()!!

            fun checkboxes(container: Container): List<JCheckBox> =
                container.components.flatMap {
                    when (it) {
                        is JCheckBox -> listOf(it)
                        is Container -> checkboxes(it)
                        else -> emptyList()
                    }
                }
            val checkbox = checkboxes(panel).single { it.text.startsWith("Remove unused open statements") }
            assertTrue(checkbox.text.contains("currently unavailable"))
            assertTrue(!checkbox.isEnabled)
        } finally {
            configurable.disposeUIResources()
        }
    }
}
