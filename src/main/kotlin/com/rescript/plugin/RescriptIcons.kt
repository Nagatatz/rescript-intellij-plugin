package com.rescript.plugin

import com.intellij.openapi.util.IconLoader

/**
 * Central registry of all icons used by the ReScript plugin.
 *
 * Icons are loaded from `/icons/` resources and used across file types,
 * tool windows, gutter markers, and structure views.
 */
object RescriptIcons {
    @JvmField val FILE = IconLoader.getIcon("/icons/rescript-file.svg", RescriptIcons::class.java)

    @JvmField val INTERFACE_FILE = IconLoader.getIcon("/icons/rescript-interface.svg", RescriptIcons::class.java)

    @JvmField val CONFIG_FILE = IconLoader.getIcon("/icons/rescript-config.svg", RescriptIcons::class.java)
}
