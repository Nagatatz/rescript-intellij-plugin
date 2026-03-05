package com.rescript.plugin.completion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptDecoratorCompletionContributorTest {
    @Test
    fun testInstanceCanBeCreated() {
        val contributor = RescriptDecoratorCompletionContributor()
        assertNotNull(contributor)
    }

    @Test
    fun testDecoratorsListIsNotEmpty() {
        assertTrue(RescriptDecoratorCompletionContributor.DECORATORS.isNotEmpty())
    }

    @Test
    fun testDecoratorsContainsGenType() {
        val genType = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "genType" }
        assertNotNull(genType, "genType should be in decorators list")
        assertEquals("Generate TypeScript types", genType!!.second)
    }

    @Test
    fun testDecoratorsContainsModule() {
        val module = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "module" }
        assertNotNull(module, "module should be in decorators list")
        assertEquals("Bind to a JS module", module!!.second)
    }

    @Test
    fun testDecoratorsContainsVal() {
        val valDecorator = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "val" }
        assertNotNull(valDecorator, "val should be in decorators list")
        assertEquals("Bind to a JS value", valDecorator!!.second)
    }

    @Test
    fun testDecoratorsContainsScope() {
        val scope = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "scope" }
        assertNotNull(scope, "scope should be in decorators list")
    }

    @Test
    fun testDecoratorsContainsSend() {
        val send = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "send" }
        assertNotNull(send, "send should be in decorators list")
    }

    @Test
    fun testDecoratorsContainsReactComponent() {
        val reactComponent = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "react.component" }
        assertNotNull(reactComponent, "react.component should be in decorators list")
        assertEquals("React component annotation", reactComponent!!.second)
    }

    @Test
    fun testDecoratorsContainsJsxComponent() {
        val jsxComponent = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "jsx.component" }
        assertNotNull(jsxComponent, "jsx.component should be in decorators list")
    }

    @Test
    fun testDecoratorsContainsDeprecated() {
        val deprecated = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "deprecated" }
        assertNotNull(deprecated, "deprecated should be in decorators list")
    }

    @Test
    fun testDecoratorsContainsLive() {
        val live = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "live" }
        assertNotNull(live, "live should be in decorators list")
        assertEquals("Mark as used (suppress unused warning)", live!!.second)
    }

    @Test
    fun testDecoratorsContainsUnboxed() {
        val unboxed = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "unboxed" }
        assertNotNull(unboxed, "unboxed should be in decorators list")
    }

    @Test
    fun testDecoratorsHaveNonEmptyDescriptions() {
        for ((name, description) in RescriptDecoratorCompletionContributor.DECORATORS) {
            assertTrue(description.isNotEmpty(), "Decorator '$name' should have non-empty description")
        }
    }

    @Test
    fun testDecoratorsHaveUniqueNames() {
        val names = RescriptDecoratorCompletionContributor.DECORATORS.map { it.first }
        assertEquals(names.size, names.toSet().size, "All decorator names should be unique")
    }

    @Test
    fun testDecoratorsCount() {
        assertEquals(24, RescriptDecoratorCompletionContributor.DECORATORS.size)
    }
}
