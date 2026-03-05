package com.rescript.plugin.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RescriptTestRunConfigurationTypeTest {
    @Test
    fun `ID is RescriptTestRunConfiguration`() {
        assertEquals("RescriptTestRunConfiguration", RescriptTestRunConfigurationType.ID)
    }

    @Test
    fun `instance has correct display name`() {
        val type = RescriptTestRunConfigurationType()
        assertEquals("ReScript Test", type.displayName)
    }

    @Test
    fun `instance has one configuration factory`() {
        val type = RescriptTestRunConfigurationType()
        assertEquals(1, type.configurationFactories.size)
    }
}
