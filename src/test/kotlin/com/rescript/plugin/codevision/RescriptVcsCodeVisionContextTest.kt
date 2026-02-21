package com.rescript.plugin.codevision

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptVcsCodeVisionContextTest {
    @Test
    fun `context can be instantiated`() {
        val context = RescriptVcsCodeVisionContext()
        assertNotNull(context)
    }
}
