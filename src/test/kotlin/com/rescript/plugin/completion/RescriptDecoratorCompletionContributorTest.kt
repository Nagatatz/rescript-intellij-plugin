package com.rescript.plugin.completion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertNotNull("genType should be in decorators list", genType)
        assertEquals("Generate TypeScript types", genType!!.second)
    }

    @Test
    fun testDecoratorsContainsModule() {
        val module = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "module" }
        assertNotNull("module should be in decorators list", module)
        assertEquals("Bind to a JS module", module!!.second)
    }

    @Test
    fun testDecoratorsContainsVal() {
        val valDecorator = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "val" }
        assertNotNull("val should be in decorators list", valDecorator)
        assertEquals("Bind to a JS value", valDecorator!!.second)
    }

    @Test
    fun testDecoratorsContainsScope() {
        val scope = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "scope" }
        assertNotNull("scope should be in decorators list", scope)
    }

    @Test
    fun testDecoratorsContainsSend() {
        val send = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "send" }
        assertNotNull("send should be in decorators list", send)
    }

    @Test
    fun testDecoratorsContainsReactComponent() {
        val reactComponent = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "react.component" }
        assertNotNull("react.component should be in decorators list", reactComponent)
        assertEquals("React component annotation", reactComponent!!.second)
    }

    @Test
    fun testDecoratorsContainsJsxComponent() {
        val jsxComponent = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "jsx.component" }
        assertNotNull("jsx.component should be in decorators list", jsxComponent)
    }

    @Test
    fun testDecoratorsContainsDeprecated() {
        val deprecated = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "deprecated" }
        assertNotNull("deprecated should be in decorators list", deprecated)
    }

    @Test
    fun testDecoratorsContainsLive() {
        val live = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "live" }
        assertNotNull("live should be in decorators list", live)
        assertEquals("Mark as used (suppress unused warning)", live!!.second)
    }

    @Test
    fun testDecoratorsContainsUnboxed() {
        val unboxed = RescriptDecoratorCompletionContributor.DECORATORS.find { it.first == "unboxed" }
        assertNotNull("unboxed should be in decorators list", unboxed)
    }

    @Test
    fun testDecoratorsHaveNonEmptyDescriptions() {
        for ((name, description) in RescriptDecoratorCompletionContributor.DECORATORS) {
            assertTrue("Decorator '$name' should have non-empty description", description.isNotEmpty())
        }
    }

    @Test
    fun testDecoratorsHaveUniqueNames() {
        val names = RescriptDecoratorCompletionContributor.DECORATORS.map { it.first }
        assertEquals("All decorator names should be unique", names.size, names.toSet().size)
    }

    @Test
    fun testDecoratorsCount() {
        assertEquals(24, RescriptDecoratorCompletionContributor.DECORATORS.size)
    }
}
