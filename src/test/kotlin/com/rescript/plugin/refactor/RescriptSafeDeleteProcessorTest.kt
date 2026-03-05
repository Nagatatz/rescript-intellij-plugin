package com.rescript.plugin.refactor

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptSafeDeleteProcessorTest {
    @Test
    fun `processor can be instantiated`() {
        val processor = RescriptSafeDeleteProcessor()
        assertNotNull(processor)
    }
}
