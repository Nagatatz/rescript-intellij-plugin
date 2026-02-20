package com.rescript.plugin.projectview

import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptProjectViewNodeDecoratorTest {
    @Test
    fun `decorator can be instantiated`() {
        val decorator = RescriptProjectViewNodeDecorator()
        assertNotNull(decorator)
    }
}
