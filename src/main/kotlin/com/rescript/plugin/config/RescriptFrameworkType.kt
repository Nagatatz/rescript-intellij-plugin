package com.rescript.plugin.config

import com.intellij.framework.FrameworkType
import com.rescript.plugin.RescriptIcons
import javax.swing.Icon

/**
 * Framework type definition for ReScript projects.
 *
 * Provides the presentation name and icon for the ReScript framework
 * in the IDE's framework detection UI.
 *
 * @see RescriptFrameworkDetector
 */
class RescriptFrameworkType : FrameworkType("rescript") {
    override fun getPresentableName(): String = "ReScript"

    override fun getIcon(): Icon = RescriptIcons.FILE

    companion object {
        /** Singleton instance used by the framework detector. */
        val INSTANCE = RescriptFrameworkType()
    }
}
