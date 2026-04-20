package com.rescript.plugin.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptSettingDescriptorTest {
    @Test
    fun `BoolDescriptor round-trips through RescriptProjectSettings`() {
        val settings = RescriptProjectSettings()
        val descriptor =
            BoolDescriptor(
                id = "errorLensEnabled",
                title = "Error Lens",
                default = true,
                getter = { it.errorLensEnabled },
                setter = { s, v -> s.errorLensEnabled = v },
            )

        assertTrue(descriptor.currentValue(settings))
        descriptor.applyValue(settings, false)
        assertFalse(descriptor.currentValue(settings))
        assertFalse(settings.errorLensEnabled)
    }

    @Test
    fun `PathDescriptor round-trips persisted string`() {
        val settings = RescriptProjectSettings()
        val descriptor =
            PathDescriptor(
                id = "lspServerPath",
                kind = PathKind.File,
                title = "Language Server Path",
                description = "Select the rescript-language-server executable or cli.js",
                getter = { it.lspServerPath },
                setter = { s, v -> s.lspServerPath = v },
            )

        assertEquals("", descriptor.currentValue(settings))
        descriptor.applyValue(settings, "/opt/lsp/server.js")
        assertEquals("/opt/lsp/server.js", descriptor.currentValue(settings))
        assertEquals("/opt/lsp/server.js", settings.lspServerPath)
    }

    @Test
    fun `ComboDescriptor round-trips selection`() {
        val settings = RescriptProjectSettings()
        val descriptor =
            ComboDescriptor(
                id = "logLevel",
                options = arrayOf("error", "warn", "info", "log"),
                default = "info",
                getter = { it.logLevel },
                setter = { s, v -> s.logLevel = v },
            )

        assertEquals("info", descriptor.currentValue(settings))
        descriptor.applyValue(settings, "warn")
        assertEquals("warn", descriptor.currentValue(settings))
        assertEquals("warn", settings.logLevel)
    }

    @Test
    fun `IntSpinnerDescriptor round-trips integer value`() {
        val settings = RescriptProjectSettings()
        val descriptor =
            IntSpinnerDescriptor(
                id = "inlayHintsMaxLength",
                default = 25,
                min = 0,
                max = 200,
                step = 1,
                getter = { it.inlayHintsMaxLength },
                setter = { s, v -> s.inlayHintsMaxLength = v },
            )

        assertEquals(25, descriptor.currentValue(settings))
        descriptor.applyValue(settings, 80)
        assertEquals(80, descriptor.currentValue(settings))
        assertEquals(80, settings.inlayHintsMaxLength)
    }

    @Test
    fun `BoolDescriptor id is exposed for schema lookup`() {
        val descriptor =
            BoolDescriptor(
                id = "compileStatusEnabled",
                title = "Status",
                default = true,
                getter = { it.compileStatusEnabled },
                setter = { s, v -> s.compileStatusEnabled = v },
            )

        assertEquals("compileStatusEnabled", descriptor.id)
    }
}
