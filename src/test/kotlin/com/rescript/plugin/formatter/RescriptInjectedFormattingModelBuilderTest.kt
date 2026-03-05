package com.rescript.plugin.formatter

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptInjectedFormattingModelBuilderTest {
    @Test
    fun testBuilderCanBeInstantiated() {
        val builder = RescriptInjectedFormattingModelBuilder()
        assertNotNull(builder)
    }
}
