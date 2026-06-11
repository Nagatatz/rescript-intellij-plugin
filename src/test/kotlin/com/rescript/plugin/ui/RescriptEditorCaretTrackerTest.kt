package com.rescript.plugin.ui

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for the shared caret tracking helper.
 *
 * Editors are created through [EditorFactory] against the light
 * fixture's project, so the install path (existing editors, future
 * editors via the factory listener, and the project filter) is
 * exercised end-to-end without a tool window.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptEditorCaretTrackerTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    @Test
    fun `caret moves in an existing project editor fire the callback`() {
        val factory = EditorFactory.getInstance()
        val editor = factory.createEditor(factory.createDocument("let x = 1"), project)
        val disposable = Disposer.newDisposable()
        try {
            var calls = 0
            var movedEditor: com.intellij.openapi.editor.Editor? = null
            RescriptEditorCaretTracker.install(project, disposable) { e ->
                calls++
                movedEditor = e
            }
            editor.caretModel.moveToOffset(3)
            assertTrue(calls >= 1, "expected at least one caret callback, got $calls")
            assertSame(editor, movedEditor)
        } finally {
            Disposer.dispose(disposable)
            factory.releaseEditor(editor)
        }
    }

    @Test
    fun `editors created after install are tracked via the factory listener`() {
        val factory = EditorFactory.getInstance()
        val disposable = Disposer.newDisposable()
        var editor: com.intellij.openapi.editor.Editor? = null
        try {
            var calls = 0
            RescriptEditorCaretTracker.install(project, disposable) { _ -> calls++ }
            editor = factory.createEditor(factory.createDocument("let y = 2"), project)
            val before = calls
            editor.caretModel.moveToOffset(4)
            assertTrue(calls > before, "expected the late-created editor to be tracked")
        } finally {
            Disposer.dispose(disposable)
            editor?.let { factory.releaseEditor(it) }
        }
    }

    @Test
    fun `editors without a project are filtered out`() {
        val factory = EditorFactory.getInstance()
        val editor = factory.createEditor(factory.createDocument("let z = 3"))
        val disposable = Disposer.newDisposable()
        try {
            assertFalse(RescriptEditorCaretTracker.shouldTrack(editor, project))
            var calls = 0
            RescriptEditorCaretTracker.install(project, disposable) { _ -> calls++ }
            editor.caretModel.moveToOffset(2)
            assertEquals(0, calls, "project-less editors must not fire the callback")
        } finally {
            Disposer.dispose(disposable)
            factory.releaseEditor(editor)
        }
    }

    @Test
    fun `shouldTrack accepts editors of the same project`() {
        val factory = EditorFactory.getInstance()
        val editor = factory.createEditor(factory.createDocument("let a = 4"), project)
        try {
            assertTrue(RescriptEditorCaretTracker.shouldTrack(editor, project))
        } finally {
            factory.releaseEditor(editor)
        }
    }
}
