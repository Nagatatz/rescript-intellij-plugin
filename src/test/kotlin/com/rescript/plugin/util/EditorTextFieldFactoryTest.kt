package com.rescript.plugin.util

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.RescriptFileType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for the shared panel editor-settings helper.
 *
 * The factory registers an `EditorSettingsProvider`, which only runs when
 * the field materialises its editor; the tests force that through a probe
 * subclass exposing the protected `createEditor()`.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class EditorTextFieldFactoryTest {
    private lateinit var project: Project

    /** Probe that exposes the protected editor creation for assertions. */
    private class ProbeField(
        project: Project,
    ) : EditorTextField("let x = 1", project, RescriptFileType) {
        fun probeEditor(): EditorEx = createEditor()
    }

    @Test
    fun `panel defaults hide line numbers folding outline and right margin`() {
        val field = ProbeField(project)
        EditorTextFieldFactory.applyPanelDefaults(field)
        val editor = field.probeEditor()
        try {
            assertFalse(editor.settings.isLineNumbersShown)
            assertFalse(editor.settings.isFoldingOutlineShown)
            assertFalse(editor.settings.isRightMarginShown)
        } finally {
            EditorFactory.getInstance().releaseEditor(editor)
        }
    }

    @Test
    fun `customizer receives the created editor after shared defaults`() {
        val field = ProbeField(project)
        var captured: EditorEx? = null
        EditorTextFieldFactory.applyPanelDefaults(field) { editor ->
            captured = editor
            editor.settings.isCaretRowShown = false
        }
        val editor = field.probeEditor()
        try {
            assertSame(editor, captured)
            assertFalse(editor.settings.isCaretRowShown)
            // Shared defaults still applied even with a customizer present.
            assertFalse(editor.settings.isLineNumbersShown)
        } finally {
            EditorFactory.getInstance().releaseEditor(editor)
        }
    }
}
