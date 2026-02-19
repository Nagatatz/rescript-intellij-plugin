package com.rescript.plugin.run

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptRunConfigurationTypeTest {
    @Test
    fun `ID is RescriptRunConfiguration`() {
        assertEquals("RescriptRunConfiguration", RescriptRunConfigurationType.ID)
    }

    @Test
    fun `instance has correct display name`() {
        val type = RescriptRunConfigurationType()
        assertEquals("ReScript", type.displayName)
    }

    @Test
    fun `instance has one configuration factory`() {
        val type = RescriptRunConfigurationType()
        assertEquals(1, type.configurationFactories.size)
    }
}
