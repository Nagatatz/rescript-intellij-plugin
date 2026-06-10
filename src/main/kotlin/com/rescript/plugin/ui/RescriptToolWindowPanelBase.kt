package com.rescript.plugin.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBLabel
import com.intellij.util.Alarm
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
 * standard Refresh action, and an optional [Alarm]-based debounce in
 * front of [doRefresh]. Subclasses build their action group (keeping
 * control over separators and extra actions) and call [installUi] from
 * their `init`.
 *
 * The debounce deliberately stays on [Alarm] for now so behaviour is
 * unchanged; migrating it to coroutines is roadmap #128 and will touch
 * only this class.
 */
abstract class RescriptToolWindowPanelBase(
    private val toolbarPlace: String,
    private val debounceMs: Int = 0,
) : SimpleToolWindowPanel(true, true),
    Disposable {
    /** Status line shown beneath the centre component. */
    protected val statusLabel: JBLabel = JBLabel(" ")

    // Registered as a child disposable of the panel, so it tears down
    // automatically when the tool window content is disposed.
    private val refreshAlarm: Alarm? =
        if (debounceMs > 0) Alarm(Alarm.ThreadToUse.SWING_THREAD, this) else null

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
     * is cancelled and re-armed ([Alarm] cancel-and-restart); with no
     * debounce [doRefresh] runs immediately on the calling thread,
     * matching the panels that refreshed directly before this base
     * class existed.
     */
    protected fun scheduleRefresh() {
        val alarm = refreshAlarm
        if (alarm == null) {
            doRefresh()
        } else {
            alarm.cancelAllRequests()
            alarm.addRequest({ doRefresh() }, debounceMs)
        }
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
        // The Alarm (when present) is a child disposable of this panel;
        // subclasses with extra resources override and call super.
    }
}
