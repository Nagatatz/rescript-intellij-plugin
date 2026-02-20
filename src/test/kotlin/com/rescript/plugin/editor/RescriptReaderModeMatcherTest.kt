package com.rescript.plugin.editor

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptReaderModeMatcherTest {
    @Test
    fun `matcher can be instantiated`() {
        val matcher = RescriptReaderModeMatcher()
        assertNotNull(matcher)
    }
}
