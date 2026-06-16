package com.rescript.plugin.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptTestConfigurationProducer]'s companion utility functions.
 *
 * Tests the test file name pattern matching logic that determines whether a file
 * should be treated as a test file for automatic run configuration creation.
 * The IDE-dependent configuration setup (context extraction, framework detection)
 * is not tested here as it requires the IntelliJ run configuration infrastructure.
 *
 * @see RescriptTestConfigurationProducer
 */
class RescriptTestConfigurationProducerTest {
    // ── isTestFileName ───────────────────────────────────────────────────

    @Test
    fun testIsTestFileNameWithUnderscoreTestSuffix() {
        assertTrue(RescriptTestConfigurationProducer.isTestFileName("my_module_test"))
    }

    @Test
    fun testIsTestFileNameWithTestSuffix() {
        assertTrue(RescriptTestConfigurationProducer.isTestFileName("MyModuleTest"))
    }

    @Test
    fun testIsTestFileNameWithUnderscoreSpecSuffix() {
        assertTrue(RescriptTestConfigurationProducer.isTestFileName("my_module_spec"))
    }

    @Test
    fun testIsTestFileNameWithSpecSuffix() {
        assertTrue(RescriptTestConfigurationProducer.isTestFileName("MyModuleSpec"))
    }

    @Test
    fun testIsTestFileNameWithRegularFile() {
        assertFalse(RescriptTestConfigurationProducer.isTestFileName("MyModule"))
    }

    @Test
    fun testIsTestFileNameWithTestInMiddle() {
        // "test" in the middle of the name should not match
        assertFalse(RescriptTestConfigurationProducer.isTestFileName("TestHelper"))
    }

    @Test
    fun testIsTestFileNameWithLowercaseTest() {
        // "test" suffix (lowercase 't') should not match
        assertFalse(RescriptTestConfigurationProducer.isTestFileName("mytest"))
    }

    @Test
    fun testIsTestFileNameEmpty() {
        assertFalse(RescriptTestConfigurationProducer.isTestFileName(""))
    }

    @Test
    fun testIsTestFileNameExactlyTest() {
        assertTrue(RescriptTestConfigurationProducer.isTestFileName("Test"))
    }

    @Test
    fun testIsTestFileNameExactlySpec() {
        assertTrue(RescriptTestConfigurationProducer.isTestFileName("Spec"))
    }

    @Test
    fun testIsTestFileNameExactlyUnderscoreTest() {
        assertTrue(RescriptTestConfigurationProducer.isTestFileName("_test"))
    }

    @Test
    fun testIsTestFileNameExactlyUnderscoreSpec() {
        assertTrue(RescriptTestConfigurationProducer.isTestFileName("_spec"))
    }

    @Test
    fun testIsTestFileNameDotTestSuffix() {
        // "Foo.test" — the dot-separated convention should match via _test suffix
        assertFalse(RescriptTestConfigurationProducer.isTestFileName("Foo.test"))
    }

    @Test
    fun testIsTestFileNameUtilsFile() {
        assertFalse(RescriptTestConfigurationProducer.isTestFileName("TestUtils"))
    }

    // ── configurationName ────────────────────────────────────────────────

    @Test
    fun testConfigurationNameWithNullTestNameIsFileScoped() {
        assertEquals(
            "Test: MyModule_test",
            RescriptTestConfigurationProducer.configurationName("MyModule_test", null),
        )
    }

    @Test
    fun testConfigurationNameWithBlankTestNameIsFileScoped() {
        assertEquals(
            "Test: MyModule_test",
            RescriptTestConfigurationProducer.configurationName("MyModule_test", "   "),
        )
    }

    @Test
    fun testConfigurationNameWithTestNameIsTestScoped() {
        assertEquals(
            "Test: MyModule_test > renders correctly",
            RescriptTestConfigurationProducer.configurationName("MyModule_test", "renders correctly"),
        )
    }

    @Test
    fun testConfigurationNameWithEmptyTestNameIsFileScoped() {
        assertEquals(
            "Test: MyModule_test",
            RescriptTestConfigurationProducer.configurationName("MyModule_test", ""),
        )
    }
}
