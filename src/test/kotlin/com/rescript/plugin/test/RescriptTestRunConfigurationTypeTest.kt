package com.rescript.plugin.test

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptTestRunConfigurationTypeTest {
    @Test
    fun `ID constant has expected value`() {
        assertEquals("RescriptTestRunConfiguration", RescriptTestRunConfigurationType.ID)
    }

    @Test
    fun `id property matches ID constant`() {
        val type = RescriptTestRunConfigurationType()
        assertEquals(RescriptTestRunConfigurationType.ID, type.id)
    }

    @Test
    fun `displayName is ReScript Test`() {
        val type = RescriptTestRunConfigurationType()
        assertEquals("ReScript Test", type.displayName)
    }

    @Test
    fun `description is set`() {
        val type = RescriptTestRunConfigurationType()
        assertEquals("Run ReScript tests with jest or vitest", type.configurationTypeDescription)
    }

    @Test
    fun `has one configuration factory`() {
        val type = RescriptTestRunConfigurationType()
        assertEquals(1, type.configurationFactories.size)
    }
}
