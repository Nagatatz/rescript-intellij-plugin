package com.rescript.plugin.typeinfo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptTypeInfoPanel]'s pure message-selection logic.
 *
 * The Swing panel itself is IntelliJ-fixture-dependent and exempt from unit
 * testing; only the [RescriptTypeInfoPanel.selectMessage] helper — which picks
 * the status text — is exercised here.
 */
class RescriptTypeInfoPanelTest {
    @Test
    fun `selectMessage reports LSP not connected when the server is absent`() {
        assertEquals(
            "ReScript Language Server not connected",
            RescriptTypeInfoPanel.selectMessage(serverConnected = false, typeText = null),
        )
    }

    @Test
    fun `selectMessage shows the type when connected and resolved`() {
        assertEquals(
            "int",
            RescriptTypeInfoPanel.selectMessage(serverConnected = true, typeText = "int"),
        )
    }

    @Test
    fun `selectMessage shows no type when connected but unresolved`() {
        assertEquals(
            "No type information",
            RescriptTypeInfoPanel.selectMessage(serverConnected = true, typeText = null),
        )
    }

    @Test
    fun `selectMessage prefers not-connected over any stale type text`() {
        assertEquals(
            "ReScript Language Server not connected",
            RescriptTypeInfoPanel.selectMessage(serverConnected = false, typeText = "int"),
        )
    }
}
