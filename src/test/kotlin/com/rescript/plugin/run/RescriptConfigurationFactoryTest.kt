package com.rescript.plugin.run

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RescriptConfigurationFactoryTest {
    private val type = RescriptRunConfigurationType()
    private val factory = RescriptConfigurationFactory(type)

    @Test
    fun `getId returns RescriptRunConfiguration ID`() {
        assertEquals(RescriptRunConfigurationType.ID, factory.id)
    }

    @Test
    fun `getOptionsClass returns RescriptRunConfigurationOptions`() {
        assertEquals(RescriptRunConfigurationOptions::class.java, factory.optionsClass)
    }
}
