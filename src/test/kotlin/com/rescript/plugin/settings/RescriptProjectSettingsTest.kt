package com.rescript.plugin.settings

import org.junit.Assert.assertFalse
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
}
