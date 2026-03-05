package com.rescript.plugin.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class RescriptTestLocatorTest {
    @Test
    fun `INSTANCE is a singleton`() {
        val a = RescriptTestLocator.INSTANCE
        val b = RescriptTestLocator.INSTANCE
        assertNotNull(a)
        assertSame(a, b)
    }

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

    @Test
    fun `compiledJsToResPath returns null for lib prefix with non-js suffix`() {
        // lib/js/ prefix present but file doesn't end in a known JS suffix
        assertNull(RescriptTestLocator.compiledJsToResPath("lib/js/src/MyTest.ts"))
    }

    @Test
    fun `compiledJsToResPath returns null for lib prefix with res suffix`() {
        assertNull(RescriptTestLocator.compiledJsToResPath("lib/js/src/MyTest.res"))
    }

    @Test
    fun `compiledJsToResPath handles lib es6 with mjs`() {
        assertEquals(
            "src/MyTest.res",
            RescriptTestLocator.compiledJsToResPath("lib/es6/src/MyTest.mjs"),
        )
    }

    @Test
    fun `compiledJsToResPath handles lib bs with plain js`() {
        assertEquals(
            "src/MyTest.res",
            RescriptTestLocator.compiledJsToResPath("lib/bs/src/MyTest.js"),
        )
    }
}
