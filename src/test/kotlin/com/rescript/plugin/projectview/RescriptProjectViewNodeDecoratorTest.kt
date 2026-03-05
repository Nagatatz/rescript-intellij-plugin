package com.rescript.plugin.projectview

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptProjectViewNodeDecoratorTest {
    @Test
    fun `decorator can be instantiated`() {
        val decorator = RescriptProjectViewNodeDecorator()
        assertNotNull(decorator)
    }
}
