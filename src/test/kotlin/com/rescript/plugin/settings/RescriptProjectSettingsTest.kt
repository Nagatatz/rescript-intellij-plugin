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

    // --- LSP initialization options settings ---

    @Test
    fun `default state has signatureHelpEnabled true`() {
        val state = RescriptProjectSettings.State()
        assertTrue(state.signatureHelpEnabled)
    }

    @Test
    fun `default state has signatureHelpForConstructorPayloads true`() {
        val state = RescriptProjectSettings.State()
        assertTrue(state.signatureHelpForConstructorPayloads)
    }

    @Test
    fun `default state has cacheProjectConfigEnabled true`() {
        val state = RescriptProjectSettings.State()
        assertTrue(state.cacheProjectConfigEnabled)
    }

    @Test
    fun `default state has inlayHintsEnabled false`() {
        val state = RescriptProjectSettings.State()
        assertFalse(state.inlayHintsEnabled)
    }

    @Test
    fun `default state has inlayHintsMaxLength 25`() {
        val state = RescriptProjectSettings.State()
        assertEquals(25, state.inlayHintsMaxLength)
    }

    @Test
    fun `default state has compileStatusEnabled true`() {
        val state = RescriptProjectSettings.State()
        assertTrue(state.compileStatusEnabled)
    }

    @Test
    fun `signatureHelpEnabled property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertTrue(settings.signatureHelpEnabled)
        settings.signatureHelpEnabled = false
        assertFalse(settings.signatureHelpEnabled)
        assertFalse(settings.state.signatureHelpEnabled)
    }

    @Test
    fun `signatureHelpForConstructorPayloads property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertTrue(settings.signatureHelpForConstructorPayloads)
        settings.signatureHelpForConstructorPayloads = false
        assertFalse(settings.signatureHelpForConstructorPayloads)
        assertFalse(settings.state.signatureHelpForConstructorPayloads)
    }

    @Test
    fun `cacheProjectConfigEnabled property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertTrue(settings.cacheProjectConfigEnabled)
        settings.cacheProjectConfigEnabled = false
        assertFalse(settings.cacheProjectConfigEnabled)
        assertFalse(settings.state.cacheProjectConfigEnabled)
    }

    @Test
    fun `inlayHintsEnabled property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertFalse(settings.inlayHintsEnabled)
        settings.inlayHintsEnabled = true
        assertTrue(settings.inlayHintsEnabled)
        assertTrue(settings.state.inlayHintsEnabled)
    }

    @Test
    fun `inlayHintsMaxLength property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertEquals(25, settings.inlayHintsMaxLength)
        settings.inlayHintsMaxLength = 50
        assertEquals(50, settings.inlayHintsMaxLength)
        assertEquals(50, settings.state.inlayHintsMaxLength)
    }

    @Test
    fun `compileStatusEnabled property delegates to state`() {
        val settings = RescriptProjectSettings()
        assertTrue(settings.compileStatusEnabled)
        settings.compileStatusEnabled = false
        assertFalse(settings.compileStatusEnabled)
        assertFalse(settings.state.compileStatusEnabled)
    }

    @Test
    fun `loadState restores LSP init option fields`() {
        val settings = RescriptProjectSettings()
        val newState = RescriptProjectSettings.State()
        newState.signatureHelpEnabled = false
        newState.signatureHelpForConstructorPayloads = false
        newState.cacheProjectConfigEnabled = false
        newState.inlayHintsEnabled = true
        newState.inlayHintsMaxLength = 100
        newState.compileStatusEnabled = false

        settings.loadState(newState)

        assertFalse(settings.signatureHelpEnabled)
        assertFalse(settings.signatureHelpForConstructorPayloads)
        assertFalse(settings.cacheProjectConfigEnabled)
        assertTrue(settings.inlayHintsEnabled)
        assertEquals(100, settings.inlayHintsMaxLength)
        assertFalse(settings.compileStatusEnabled)
    }
}
