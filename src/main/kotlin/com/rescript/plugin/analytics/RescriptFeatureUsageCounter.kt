package com.rescript.plugin.analytics

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.rescript.plugin.wizard.ProjectTemplate

/**
 * Feature Usage Statistics counter for the ReScript plugin.
 *
 * Records anonymous, opt-in usage signals for a small set of features so
 * the maintainer can identify which surfaces are actually used and which
 * are candidates for deprecation. All values are validated against
 * closed enums or platform-supplied class-name validators — no free-form
 * strings, file paths, or source content are recorded.
 *
 * Implements the IntelliJ Platform `CounterUsagesCollector` extension point
 * registered via `<statistics.counterUsagesCollector>` in plugin.xml.
 *
 * @see TOOLWINDOW_IDS for the closed enum of recorded tool window IDs
 * @see ProjectTemplate for the closed enum of recorded wizard templates
 */
class RescriptFeatureUsageCounter : CounterUsagesCollector() {
    override fun getGroup(): EventLogGroup = GROUP

    companion object {
        /**
         * Plugin-scoped event group. Bumping the version invalidates downstream
         * dashboards — only do it when event shapes change incompatibly.
         */
        private val GROUP =
            EventLogGroup(
                id = "rescript.features",
                version = 1,
                recorder = "FUS",
                description =
                    "ReScript IntelliJ plugin feature usage signals " +
                        "(opt-in via IDE-level statistics consent).",
            )

        /**
         * Closed set of tool window IDs this collector observes. Adding a new
         * tool window requires extending this list and bumping the GROUP version.
         */
        val TOOLWINDOW_IDS: List<String> =
            listOf(
                "ReScript Type",
                "ReScript PPX",
                "ReScript REPL",
                "ReScript Compiled JS",
                "ReScript Dependencies",
                "ReScript Dependency Diagram",
            )

        private val TEMPLATE_FIELD = EventFields.Enum<ProjectTemplate>("template")

        private val TOOLWINDOW_FIELD =
            EventFields.String("toolwindow_id", TOOLWINDOW_IDS)

        private val INTENTION_FIELD = EventFields.Class("intention_class")

        /**
         * Fired when the user picks a template in the New Project wizard and
         * confirms the dialog. Captures the [ProjectTemplate] enum only — no
         * project name, paths, or user-entered values.
         */
        val WIZARD_TEMPLATE_SELECTED =
            GROUP.registerEvent(
                "wizard.template.selected",
                TEMPLATE_FIELD,
                "Project template chosen by the user when scaffolding a new ReScript project.",
            )

        /**
         * Fired when one of the plugin's tool windows is opened. Validates the
         * ID against [TOOLWINDOW_IDS] so unknown IDs are recorded as `validation.invalid`.
         */
        val TOOLWINDOW_OPENED =
            GROUP.registerEvent(
                "toolwindow.opened",
                TOOLWINDOW_FIELD,
                "Tool window opened by the user; values restricted to the closed list of plugin-owned tool windows.",
            )

        /**
         * Fired when an intention action contributed by this plugin is invoked.
         * Records the action class only; the platform validates that the class
         * belongs to a registered plugin.
         */
        val INTENTION_INVOKED =
            GROUP.registerEvent(
                "intention.invoked",
                INTENTION_FIELD,
                "Intention action invoked from the plugin; only the action class is recorded.",
            )
    }
}
