package com.rescript.plugin.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project

/**
 * Attaches a caret listener to every existing and future editor of a
 * project, so caret-driven tool window panels (Variant Flow, Type
 * Impact) can react to caret motion without holding references to
 * specific files.
 *
 * Both panels used to repeat the same `attachEditorListeners` /
 * `attachCaretListener` pair: walk [EditorFactory.getAllEditors], then
 * register an [EditorFactoryListener] for editors created later. All
 * listeners are bound to the caller's parent disposable, matching the
 * previous per-panel lifecycle.
 */
internal object RescriptEditorCaretTracker {
    /**
     * Installs caret tracking for [project]: [onCaretMoved] fires on
     * every caret position change in any of the project's editors,
     * current or future. Listener lifetimes are bound to
     * [parentDisposable].
     *
     * @param project only editors belonging to this project are tracked
     * @param parentDisposable owner for the caret and factory listeners
     * @param onCaretMoved callback invoked on the event thread per caret move
     */
    fun install(
        project: Project,
        parentDisposable: Disposable,
        onCaretMoved: () -> Unit,
    ) {
        val editorFactory = EditorFactory.getInstance()
        for (editor in editorFactory.allEditors) {
            attachCaretListener(editor, project, parentDisposable, onCaretMoved)
        }
        editorFactory.addEditorFactoryListener(
            object : EditorFactoryListener {
                override fun editorCreated(event: EditorFactoryEvent) {
                    attachCaretListener(event.editor, project, parentDisposable, onCaretMoved)
                }
            },
            parentDisposable,
        )
    }

    /**
     * Returns whether [editor] belongs to [project] and should receive
     * a caret listener. Exposed `internal` so the predicate is unit
     * testable apart from the IDE-coupled listener wiring.
     */
    internal fun shouldTrack(
        editor: Editor,
        project: Project,
    ): Boolean = editor.project == project

    private fun attachCaretListener(
        editor: Editor,
        project: Project,
        parentDisposable: Disposable,
        onCaretMoved: () -> Unit,
    ) {
        if (!shouldTrack(editor, project)) return
        editor.caretModel.addCaretListener(
            object : CaretListener {
                override fun caretPositionChanged(event: CaretEvent) {
                    onCaretMoved()
                }
            },
            parentDisposable,
        )
    }
}
