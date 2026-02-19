package com.rescript.plugin.test

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptTestConfigurationFactoryTest {
    private val type = RescriptTestRunConfigurationType()
    private val factory = RescriptTestConfigurationFactory(type)

    @Test
    fun `getId returns RescriptTestRunConfiguration`() {
        assertEquals("RescriptTestRunConfiguration", factory.id)
    }

    @Test
    fun `getOptionsClass returns RescriptTestRunConfigurationOptions`() {
        assertEquals(RescriptTestRunConfigurationOptions::class.java, factory.optionsClass)
    }
}
