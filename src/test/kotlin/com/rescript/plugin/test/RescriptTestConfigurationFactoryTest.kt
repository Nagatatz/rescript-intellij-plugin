package com.rescript.plugin.test

import org.junit.Assert.*
import org.junit.Test

class RescriptTestConfigurationFactoryTest {
    private val type = RescriptTestRunConfigurationType()
    private val factory = RescriptTestConfigurationFactory(type)

    @Test
    fun `getId returns RescriptTestRunConfiguration ID`() {
        assertEquals(RescriptTestRunConfigurationType.ID, factory.id)
    }

    @Test
    fun `getOptionsClass returns RescriptTestRunConfigurationOptions`() {
        assertEquals(RescriptTestRunConfigurationOptions::class.java, factory.optionsClass)
    }
}
