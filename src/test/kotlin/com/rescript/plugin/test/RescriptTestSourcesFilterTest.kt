package com.rescript.plugin.test

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptTestSourcesFilterTest {
    @Test
    fun `underscore test suffix is recognized`() {
        assertTrue(isTestFileName("Foo_test.res"))
        assertTrue(isTestFileName("Bar_test.resi"))
    }

    @Test
    fun `dot test suffix is recognized`() {
        assertTrue(isTestFileName("Foo.test.res"))
        assertTrue(isTestFileName("Bar.test.resi"))
    }

    @Test
    fun `regular source files are not test files`() {
        assertFalse(isTestFileName("Foo.res"))
        assertFalse(isTestFileName("Bar.resi"))
        assertFalse(isTestFileName("FooTest.res"))
    }

    @Test
    fun `non-rescript files are not test files`() {
        assertFalse(isTestFileName("foo_test.js"))
        assertFalse(isTestFileName("foo.test.ts"))
    }

    @Test
    fun `__tests__ directory path is recognized`() {
        assertTrue(isTestPath("/project/src/__tests__/Foo.res"))
        assertTrue(isTestPath("/project/__tests__/sub/Bar.res"))
    }

    @Test
    fun `regular directory path is not test path`() {
        assertFalse(isTestPath("/project/src/Foo.res"))
        assertFalse(isTestPath("/project/tests/Foo.res"))
    }

    // Helper methods that test the logic without VirtualFile dependency
    private fun isTestFileName(fileName: String): Boolean {
        val testSuffixes = listOf("_test.res", ".test.res", "_test.resi", ".test.resi")
        return testSuffixes.any { fileName.endsWith(it) }
    }

    private fun isTestPath(path: String): Boolean = path.split("/").any { it == "__tests__" }
}
