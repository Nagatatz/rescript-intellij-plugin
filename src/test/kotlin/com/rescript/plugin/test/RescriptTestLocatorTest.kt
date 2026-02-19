package com.rescript.plugin.test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptTestLocatorTest {
    @Test
    fun `compiledJsToResPath converts bs js path`() {
        assertEquals(
            "src/MyTest.res",
            RescriptTestLocator.compiledJsToResPath("lib/js/src/MyTest.bs.js"),
        )
    }

    @Test
    fun `compiledJsToResPath converts mjs path`() {
        assertEquals(
            "src/MyTest.res",
            RescriptTestLocator.compiledJsToResPath("lib/js/src/MyTest.mjs"),
        )
    }

    @Test
    fun `compiledJsToResPath converts plain js path`() {
        assertEquals(
            "src/MyTest.res",
            RescriptTestLocator.compiledJsToResPath("lib/js/src/MyTest.js"),
        )
    }

    @Test
    fun `compiledJsToResPath handles nested paths`() {
        assertEquals(
            "src/components/deep/MyComponent.res",
            RescriptTestLocator.compiledJsToResPath("lib/js/src/components/deep/MyComponent.bs.js"),
        )
    }

    @Test
    fun `compiledJsToResPath handles es6 output`() {
        assertEquals(
            "src/MyTest.res",
            RescriptTestLocator.compiledJsToResPath("lib/es6/src/MyTest.bs.js"),
        )
    }

    @Test
    fun `compiledJsToResPath handles bs output`() {
        assertEquals(
            "src/MyTest.res",
            RescriptTestLocator.compiledJsToResPath("lib/bs/src/MyTest.bs.js"),
        )
    }

    @Test
    fun `compiledJsToResPath returns null for non-js path`() {
        assertNull(RescriptTestLocator.compiledJsToResPath("src/MyTest.res"))
    }

    @Test
    fun `compiledJsToResPath returns null for path without lib prefix`() {
        assertNull(RescriptTestLocator.compiledJsToResPath("src/MyTest.bs.js"))
    }

    @Test
    fun `compiledJsToResPath handles root-level files`() {
        assertEquals(
            "MyTest.res",
            RescriptTestLocator.compiledJsToResPath("lib/js/MyTest.bs.js"),
        )
    }
}
