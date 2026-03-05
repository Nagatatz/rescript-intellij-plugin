package com.rescript.plugin.completion

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptLookupCharFilterTest {
    @Test
    fun `filter can be instantiated`() {
        val filter = RescriptLookupCharFilter()
        assertNotNull(filter)
    }
}
