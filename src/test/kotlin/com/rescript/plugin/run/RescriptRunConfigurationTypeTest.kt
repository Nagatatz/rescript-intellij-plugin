package com.rescript.plugin.run

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptRunConfigurationTypeTest {
    @Test
    fun `ID constant has expected value`() {
        assertEquals("RescriptRunConfiguration", RescriptRunConfigurationType.ID)
    }

    @Test
    fun `id property matches ID constant`() {
        val type = RescriptRunConfigurationType()
        assertEquals(RescriptRunConfigurationType.ID, type.id)
    }

    @Test
    fun `displayName is ReScript`() {
        val type = RescriptRunConfigurationType()
        assertEquals("ReScript", type.displayName)
    }

    @Test
    fun `description is set`() {
        val type = RescriptRunConfigurationType()
        assertEquals("Run ReScript build commands", type.configurationTypeDescription)
    }

    @Test
    fun `has one configuration factory`() {
        val type = RescriptRunConfigurationType()
        assertEquals(1, type.configurationFactories.size)
    }
}
