package com.rescript.plugin.completion

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptLookupCharFilterTest {
    @Test
    fun `filter can be instantiated`() {
        val filter = RescriptLookupCharFilter()
        assertNotNull(filter)
    }
}
