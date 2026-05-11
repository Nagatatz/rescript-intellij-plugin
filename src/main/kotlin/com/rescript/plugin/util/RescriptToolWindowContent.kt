package com.rescript.plugin.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import javax.swing.JComponent

/**
 * Installs a single-content panel into a ReScript tool window.
 *
 * Centralises the boilerplate previously duplicated across the six
 * Pattern A `*ToolWindowFactory` classes — those whose panel is itself a
 * [Disposable] (e.g. a `SimpleToolWindowPanel` subclass) and is mounted
 * directly as the tool window's only content. Pattern B factories
 * (e.g. `ppx`, `repl`, `dependencies`, `preview`, `typeinfo`) pass the
 * tool window's own disposable into the panel constructor and mount
 * `panel.component`; they deliberately stay out of this helper.
 */
object RescriptToolWindowContent {
    /**
     * Creates a single content for [toolWindow] from [component], registers
     * [disposable] as a child of that content (so it tears down with the
     * tool window), and adds the content to the tool window's content
     * manager.
     *
     * @param toolWindow the target tool window
     * @param component the Swing component shown as the tool window's
     *   content
     * @param disposable the lifecycle owner registered as a child of the
     *   created content; for Pattern A factories this is the panel itself
     */
    fun install(
        toolWindow: ToolWindow,
        component: JComponent,
        disposable: Disposable,
    ) {
        val content = ContentFactory.getInstance().createContent(component, "", false)
        Disposer.register(content, disposable)
        toolWindow.contentManager.addContent(content)
    }
}
