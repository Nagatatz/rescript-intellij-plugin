package com.rescript.plugin.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import java.awt.CardLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Tool window panel scaffold with a Visual / Source card toggle, used by
 * the diagram-style panels (Variant Flow, Module Dependency Diagram).
 *
 * Extends [RescriptToolWindowPanelBase] with the [CardLayout] switcher
 * and the mutually exclusive Visual / Source [ToggleAction] pair both
 * panels used to duplicate. Subclasses wrap their two components via
 * [buildDualView], pass the result to `installUi`, and add the actions
 * from [createVisualModeAction] / [createSourceModeAction] to their
 * toolbar group. The visual card is selected initially.
 */
abstract class DualViewToolWindowPanel(
    project: Project,
    toolbarPlace: String,
    debounceMs: Int = 0,
) : RescriptToolWindowPanelBase(project, toolbarPlace, debounceMs) {
    private val viewCards: CardLayout = CardLayout()

    private val viewSwitcher: JPanel = JPanel(viewCards)

    @Volatile
    private var visualMode: Boolean = true

    /**
     * Registers [visual] and [source] (each wrapped in a [JBScrollPane])
     * as the two cards and returns the switcher component to be passed
     * to `installUi`. The visual card starts selected.
     *
     * @param visual Java2D graph component shown in Visual mode
     * @param source read-only source text component shown in Source mode
     */
    protected fun buildDualView(
        visual: JComponent,
        source: JComponent,
    ): JComponent {
        viewSwitcher.add(JBScrollPane(visual), CARD_VISUAL)
        viewSwitcher.add(JBScrollPane(source), CARD_SOURCE)
        viewCards.show(viewSwitcher, CARD_VISUAL)
        return viewSwitcher
    }

    /** Flips the centre card; `true` selects the visual graph, `false` the source text. */
    protected fun switchView(toVisual: Boolean) {
        visualMode = toVisual
        viewCards.show(viewSwitcher, if (toVisual) CARD_VISUAL else CARD_SOURCE)
    }

    /**
     * Builds the toolbar toggle that selects the visual (Java2D) card.
     * Mutually exclusive with [createSourceModeAction]'s toggle —
     * exactly one reports selected.
     *
     * @param description action tooltip describing the visual rendering
     */
    protected fun createVisualModeAction(description: String): ToggleAction =
        object : ToggleAction("Visual", description, AllIcons.Toolwindows.ToolWindowHierarchy) {
            override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

            override fun isSelected(e: AnActionEvent): Boolean = visualMode

            override fun setSelected(
                e: AnActionEvent,
                state: Boolean,
            ) {
                if (state) switchView(true)
            }
        }

    /**
     * Builds the toolbar toggle that selects the source text card.
     *
     * @param description action tooltip describing the source view
     */
    protected fun createSourceModeAction(description: String): ToggleAction =
        object : ToggleAction("Source", description, AllIcons.FileTypes.Text) {
            override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

            override fun isSelected(e: AnActionEvent): Boolean = !visualMode

            override fun setSelected(
                e: AnActionEvent,
                state: Boolean,
            ) {
                if (state) switchView(false)
            }
        }

    private companion object {
        const val CARD_VISUAL = "visual"
        const val CARD_SOURCE = "source"
    }
}
