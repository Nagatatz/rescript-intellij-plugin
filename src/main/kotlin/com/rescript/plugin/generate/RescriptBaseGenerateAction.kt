package com.rescript.plugin.generate

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction

/**
 * Abstract base class for ReScript code generation actions (Cmd+N / Alt+Insert).
 *
 * Provides the common [ActionUpdateThread.BGT] thread policy so subclasses
 * do not need to override [getActionUpdateThread] individually.
 *
 * @see RescriptGenerateActionUtil for shared editor context helpers
 * @see RescriptGenerateGroup for the Generate menu integration
 */
abstract class RescriptBaseGenerateAction(
    title: String,
    description: String,
) : AnAction(title, description, null) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
