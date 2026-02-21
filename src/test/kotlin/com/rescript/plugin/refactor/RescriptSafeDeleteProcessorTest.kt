package com.rescript.plugin.refactor

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptSafeDeleteProcessorTest {
    @Test
    fun `processor can be instantiated`() {
        val processor = RescriptSafeDeleteProcessor()
        assertNotNull(processor)
    }
}
