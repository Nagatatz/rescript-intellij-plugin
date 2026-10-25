package com.rescript.plugin.intention

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.behavior.TransformationBehaviorFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/** Exercises registered qualifier actions, one-step undo, unsupported invoke and stale proofs. */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptRemoveQualifierIntentionEditorTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @BeforeEach
    fun configureProjectContentRoot() {
        PsiTestUtil.addContentRoot(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
    }

    @AfterEach
    fun removeProjectContentRoot() {
        PsiTestUtil.removeContentEntry(myFixture.module, myFixture.tempDirFixture.findOrCreateDir(""))
    }

    @Test
    fun `registered intention preserves local value identity and undo restores the source`() {
        val before = "module A = { let value = 1 }\nopen A\nlet result = <caret>A.value + 2"
        myFixture.configureByText("Qualifier.res", before)
        TransformationBehaviorFixture(myFixture).registeredIntentionAndUndo(
            "Remove redundant qualifier",
            before.replace("<caret>A.value", "value"),
        )
    }

    @Test
    fun `unsafe qualified values remain unchanged even when invoked directly`() {
        listOf(
            "module A = { let value = 1 }\nlet result = <caret>A.value\nopen A",
            "module A = { let value = 1 }\nopen A\nlet value = 2\nlet result = <caret>A.value",
            "module A = { let value = 1 }\nopen A\nlet run = value => <caret>A.value",
            "module A = { let value = 1 }\n/*\nopen A\n*/\nlet result = <caret>A.value",
            "open Array\nlet result = <caret>Array.length(items)",
        ).forEach { source ->
            myFixture.configureByText("Unsafe.res", source)
            val intention = RescriptRemoveQualifierIntention()
            val before = myFixture.editor.document.text
            assertFalse(intention.isAvailable(project, myFixture.editor, myFixture.file))
            WriteCommandAction.runWriteCommandAction(project) {
                intention.invoke(project, myFixture.editor, myFixture.file)
            }
            assertEquals(before, myFixture.editor.document.text)
        }
    }

    @Test
    fun `prepared proof refuses document changes outside the qualified reference`() {
        myFixture.configureByText(
            "Stale.res",
            "module A = { let value = 1 }\nopen A\nlet result = <caret>A.value",
        )
        val intention = RescriptRemoveQualifierIntention()
        assertTrue(intention.isAvailable(project, myFixture.editor, myFixture.file))
        WriteCommandAction.runWriteCommandAction(project) {
            val document = myFixture.editor.document
            document.insertString(document.textLength, "\n// changed after proof")
        }
        PsiDocumentManager.getInstance(project).commitDocument(myFixture.editor.document)
        val changed = myFixture.editor.document.text
        WriteCommandAction.runWriteCommandAction(project) {
            intention.invoke(project, myFixture.editor, myFixture.file)
        }
        assertEquals(changed, myFixture.editor.document.text)
    }

    @Test
    fun `prepared proof refuses a changed caret`() {
        myFixture.configureByText(
            "Caret.res",
            "module A = { let value = 1 }\nopen A\nlet result = <caret>A.value + A.value",
        )
        val intention = RescriptRemoveQualifierIntention()
        assertTrue(intention.isAvailable(project, myFixture.editor, myFixture.file))
        val before = myFixture.editor.document.text
        myFixture.editor.caretModel.moveToOffset(before.lastIndexOf("A.value"))
        WriteCommandAction.runWriteCommandAction(project) {
            intention.invoke(project, myFixture.editor, myFixture.file)
        }
        assertEquals(before, myFixture.editor.document.text)
    }
}
