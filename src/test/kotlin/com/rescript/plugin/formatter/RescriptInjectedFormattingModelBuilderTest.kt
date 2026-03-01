package com.rescript.plugin.formatter

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptInjectedFormattingModelBuilderTest {
    @Test
    fun testBuilderCanBeInstantiated() {
        val builder = RescriptInjectedFormattingModelBuilder()
        assertNotNull(builder)
    }
}
