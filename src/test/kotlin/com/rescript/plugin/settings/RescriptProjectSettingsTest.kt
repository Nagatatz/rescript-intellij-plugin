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

    // --- Cross-file incremental type checking ---

    @Test
    fun `default state has incrementalTypecheckingAcrossFiles disabled`() {
        val state = RescriptProjectSettings.State()
        assertFalse(state.incrementalTypecheckingAcrossFiles)
    }

    @Test
    fun `incrementalTypecheckingAcrossFiles property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertFalse(settings.incrementalTypecheckingAcrossFiles)
        settings.incrementalTypecheckingAcrossFiles = true
        assertTrue(settings.incrementalTypecheckingAcrossFiles)
        assertTrue(settings.state.incrementalTypecheckingAcrossFiles)
    }

    // --- LSP additional settings ---

    @Test
    fun `default state has empty rescriptBinaryPath`() {
        val state = RescriptProjectSettings.State()
        assertEquals("", state.rescriptBinaryPath)
    }

    @Test
    fun `default state has empty platformPath`() {
        val state = RescriptProjectSettings.State()
        assertEquals("", state.platformPath)
    }

    @Test
    fun `default state has empty runtimePath`() {
        val state = RescriptProjectSettings.State()
        assertEquals("", state.runtimePath)
    }

    @Test
    fun `default state has info logLevel`() {
        val state = RescriptProjectSettings.State()
        assertEquals("info", state.logLevel)
    }

    @Test
    fun `rescriptBinaryPath property delegates to state`() {
        val settings = RescriptProjectSettings()
        settings.rescriptBinaryPath = "/path/to/rescript"
        assertEquals("/path/to/rescript", settings.rescriptBinaryPath)
        assertEquals("/path/to/rescript", settings.state.rescriptBinaryPath)
    }

    @Test
    fun `platformPath property delegates to state`() {
        val settings = RescriptProjectSettings()
        settings.platformPath = "/path/to/platform"
        assertEquals("/path/to/platform", settings.platformPath)
        assertEquals("/path/to/platform", settings.state.platformPath)
    }

    @Test
    fun `runtimePath property delegates to state`() {
        val settings = RescriptProjectSettings()
        settings.runtimePath = "/path/to/runtime"
        assertEquals("/path/to/runtime", settings.runtimePath)
        assertEquals("/path/to/runtime", settings.state.runtimePath)
    }

    @Test
    fun `logLevel property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertEquals("info", settings.logLevel)
        settings.logLevel = "error"
        assertEquals("error", settings.logLevel)
        assertEquals("error", settings.state.logLevel)
    }

    // --- Reanalyze server mode ---

    @Test
    fun `default state has reanalyzeServerEnabled enabled`() {
        val state = RescriptProjectSettings.State()
        assertTrue(state.reanalyzeServerEnabled)
    }

    @Test
    fun `reanalyzeServerEnabled property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertTrue(settings.reanalyzeServerEnabled)
        settings.reanalyzeServerEnabled = false
        assertFalse(settings.reanalyzeServerEnabled)
        assertFalse(settings.state.reanalyzeServerEnabled)
    }

    @Test
    fun `loadState restores all new fields`() {
        val settings = RescriptProjectSettings()
        val newState = RescriptProjectSettings.State()
        newState.incrementalTypecheckingAcrossFiles = true
        newState.rescriptBinaryPath = "/bin/rescript"
        newState.platformPath = "/platform"
        newState.runtimePath = "/runtime"
        newState.logLevel = "log"

        settings.loadState(newState)

        assertTrue(settings.incrementalTypecheckingAcrossFiles)
        assertEquals("/bin/rescript", settings.rescriptBinaryPath)
        assertEquals("/platform", settings.platformPath)
        assertEquals("/runtime", settings.runtimePath)
        assertEquals("log", settings.logLevel)
    }
}
