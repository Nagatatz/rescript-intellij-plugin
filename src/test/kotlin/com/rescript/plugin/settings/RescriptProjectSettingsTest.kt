package com.rescript.plugin.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptProjectSettingsTest {
    @Test
    fun `default state has incremental typechecking enabled`() {
        val state = RescriptProjectSettings.State()
        assertTrue(state.incrementalTypecheckingEnabled)
    }

    @Test
    fun `incremental typechecking can be toggled`() {
        val state = RescriptProjectSettings.State()
        state.incrementalTypecheckingEnabled = false
        assertFalse(state.incrementalTypecheckingEnabled)
        state.incrementalTypecheckingEnabled = true
        assertTrue(state.incrementalTypecheckingEnabled)
    }

    @Test
    fun `default state has empty lsp server path`() {
        val state = RescriptProjectSettings.State()
        assertTrue(state.lspServerPath.isEmpty())
    }

    @Test
    fun `default state has empty node path`() {
        val state = RescriptProjectSettings.State()
        assertTrue(state.nodePath.isEmpty())
    }

    // --- RescriptProjectSettings instance tests ---

    @Test
    fun `settings can be instantiated`() {
        val settings = RescriptProjectSettings()
        assertNotNull(settings)
    }

    @Test
    fun `getState returns default state`() {
        val settings = RescriptProjectSettings()
        val state = settings.state
        assertNotNull(state)
        assertEquals("", state.lspServerPath)
        assertEquals("", state.nodePath)
        assertTrue(state.incrementalTypecheckingEnabled)
    }

    @Test
    fun `loadState replaces current state`() {
        val settings = RescriptProjectSettings()
        val newState = RescriptProjectSettings.State()
        newState.lspServerPath = "/custom/path"
        newState.nodePath = "/usr/bin/node"
        newState.incrementalTypecheckingEnabled = false

        settings.loadState(newState)

        assertEquals("/custom/path", settings.state.lspServerPath)
        assertEquals("/usr/bin/node", settings.state.nodePath)
        assertFalse(settings.state.incrementalTypecheckingEnabled)
    }

    @Test
    fun `lspServerPath property delegates to state`() {
        val settings = RescriptProjectSettings()
        settings.lspServerPath = "/my/server"
        assertEquals("/my/server", settings.lspServerPath)
        assertEquals("/my/server", settings.state.lspServerPath)
    }

    @Test
    fun `nodePath property delegates to state`() {
        val settings = RescriptProjectSettings()
        settings.nodePath = "/usr/local/bin/node"
        assertEquals("/usr/local/bin/node", settings.nodePath)
        assertEquals("/usr/local/bin/node", settings.state.nodePath)
    }

    @Test
    fun `incrementalTypecheckingEnabled property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertTrue(settings.incrementalTypecheckingEnabled)
        settings.incrementalTypecheckingEnabled = false
        assertFalse(settings.incrementalTypecheckingEnabled)
        assertFalse(settings.state.incrementalTypecheckingEnabled)
    }

    @Test
    fun `state lspServerPath can be set`() {
        val state = RescriptProjectSettings.State()
        state.lspServerPath = "/path/to/lsp"
        assertEquals("/path/to/lsp", state.lspServerPath)
    }

    @Test
    fun `state nodePath can be set`() {
        val state = RescriptProjectSettings.State()
        state.nodePath = "/path/to/node"
        assertEquals("/path/to/node", state.nodePath)
    }
}
