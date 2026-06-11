package com.rescript.plugin.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import com.rescript.plugin.util.RescriptCoroutineDebouncer
import com.rescript.plugin.util.RescriptCoroutineScopeService
import kotlinx.coroutines.Dispatchers
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Shared scaffold for the ReScript tool window panels (Variant Flow,
 * Module Dependency, Type Coverage, Type Impact, Interop Risk).
 *
 * Extends [SimpleToolWindowPanel] with the layout and refresh plumbing
 * every panel used to repeat: a status label docked under the centre
 * component, an [ActionManager]-built toolbar wired to `this`, a
 * standard Refresh action, and an optional coroutine debounce in front
 * of [doRefresh] (dispatched on [Dispatchers.EDT], matching the old
 * `Alarm(SWING_THREAD)` behaviour). Subclasses build their action group
 * (keeping control over separators and extra actions) and call
 * [installUi] from their `init`.
 */
abstract class RescriptToolWindowPanelBase(
    project: Project,
    private val toolbarPlace: String,
    debounceMs: Int = 0,
) : SimpleToolWindowPanel(true, true),
    Disposable {
    /** Status line shown beneath the centre component. */
    protected val statusLabel: JBLabel = JBLabel(" ")

    // Pending work dies with the project scope; dispose() additionally
    // drops it when the tool window content goes away first.
    private val debouncer: RescriptCoroutineDebouncer? =
        if (debounceMs > 0) {
            RescriptCoroutineDebouncer(
                project.service<RescriptCoroutineScopeService>().scope,
                debounceMs.toLong(),
                Dispatchers.EDT,
            )
        } else {
            null
        }

    /**
     * Installs the shared panel chrome: [center] above [statusLabel]
     * as the content, and a toolbar built from [actions] targeting this
     * panel.
     *
     * @param center the panel's main component (typically a scroll pane)
     * @param actions toolbar group assembled by the subclass
     */
    protected fun installUi(
        center: JComponent,
        actions: DefaultActionGroup,
    ) {
        val centerPanel =
            JPanel(BorderLayout()).apply {
                add(center, BorderLayout.CENTER)
                add(statusLabel, BorderLayout.SOUTH)
            }
        setContent(centerPanel)
        val toolbar = ActionManager.getInstance().createActionToolbar(toolbarPlace, actions, true)
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    /**
     * Requests a refresh. With a positive debounce the pending request
     * is cancelled and re-armed; with no debounce [doRefresh] runs
     * immediately on the calling thread, matching the panels that
     * refreshed directly before this base class existed.
     */
    protected fun scheduleRefresh() {
        debouncer?.schedule { doRefresh() } ?: doRefresh()
    }

    /** Rebuilds the panel's content from the current project state. */
    protected abstract fun doRefresh()

    /**
     * Builds the standard toolbar Refresh action (BGT update thread,
     * platform refresh icon) that funnels into [scheduleRefresh].
     *
     * @param description action tooltip describing what gets rebuilt
     */
    protected fun createRefreshAction(description: String): AnAction =
        object : AnAction("Refresh", description, AllIcons.Actions.Refresh) {
            override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

            override fun actionPerformed(e: AnActionEvent) {
                scheduleRefresh()
            }
        }

    override fun dispose() {
        // Drop any pending refresh; subclasses with extra resources
        // override and call super.
        debouncer?.cancel()
    }
}
