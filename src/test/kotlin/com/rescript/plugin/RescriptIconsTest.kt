package com.rescript.plugin

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Smoke tests for [RescriptIcons].
 *
 * Verifies that every icon field resolves to a non-null Icon and that the
 * underlying SVG resource exists on the classpath. IconLoader silently returns
 * a placeholder if the resource is missing, so the resource existence check
 * guards against typos in the path constant.
 */
class RescriptIconsTest {
    @Test
    fun `FILE icon is non-null and resource exists`() {
        assertNotNull(RescriptIcons.FILE)
        assertNotNull(RescriptIcons::class.java.getResource("/icons/rescript-file.svg"))
    }

    @Test
    fun `INTERFACE_FILE icon is non-null and resource exists`() {
        assertNotNull(RescriptIcons.INTERFACE_FILE)
        assertNotNull(RescriptIcons::class.java.getResource("/icons/rescript-interface.svg"))
    }

    @Test
    fun `CONFIG_FILE icon is non-null and resource exists`() {
        assertNotNull(RescriptIcons.CONFIG_FILE)
        assertNotNull(RescriptIcons::class.java.getResource("/icons/rescript-config.svg"))
    }

    @Test
    fun `TOOL_WINDOW icon is non-null and both theme variants exist`() {
        assertNotNull(RescriptIcons.TOOL_WINDOW)
        assertNotNull(RescriptIcons::class.java.getResource("/icons/rescript-toolwindow.svg"))
        assertNotNull(RescriptIcons::class.java.getResource("/icons/rescript-toolwindow_dark.svg"))
    }
}
