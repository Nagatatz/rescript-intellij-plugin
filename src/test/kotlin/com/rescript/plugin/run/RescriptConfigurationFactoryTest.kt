package com.rescript.plugin.run

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptConfigurationFactoryTest {
    private val type = RescriptRunConfigurationType()
    private val factory = RescriptConfigurationFactory(type)

    @Test
    fun `getId returns RescriptRunConfiguration`() {
        assertEquals("RescriptRunConfiguration", factory.id)
    }

    @Test
    fun `getOptionsClass returns RescriptRunConfigurationOptions`() {
        assertEquals(RescriptRunConfigurationOptions::class.java, factory.optionsClass)
    }
}
