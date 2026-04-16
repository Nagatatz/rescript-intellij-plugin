package com.rescript.plugin.formatter

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Unit tests for [RescriptFormattingService].
 *
 * Covers the public predicates (getFeatures, canFormat) and accesses the
 * protected metadata accessors via reflection. The protected
 * createFormattingTask method requires a fully-populated
 * AsyncFormattingRequest and an actual ReScript CLI binary, so its
 * happy-path is not exercised here; the canFormat predicate is the main
 * gate users encounter, and the analyzer logic on top of CLI output is
 * straightforward stream handling.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptFormattingServiceTest {
    private lateinit var myFixture: CodeInsightTestFixture

    private val service = RescriptFormattingService()

    // ── public methods ────────────────────────────────────────────────

    @Test
    fun testFeaturesAreEmpty() {
        assertTrue(service.features.isEmpty())
    }

    @Test
    fun testCanFormatResFile() {
        val file = myFixture.configureByText("Foo.res", "let x = 1")
        assertTrue(service.canFormat(file))
    }

    @Test
    fun testCanFormatResiFile() {
        val file = myFixture.configureByText("Foo.resi", "let x: int")
        assertTrue(service.canFormat(file))
    }

    @Test
    fun testCannotFormatNonRescriptFile() {
        val file = myFixture.configureByText("notes.txt", "hello")
        assertFalse(service.canFormat(file))
    }

    // ── protected metadata accessors (via reflection) ─────────────────

    @Test
    fun testNameIsRescriptFormat() {
        val method = service.javaClass.getDeclaredMethod("getName")
        method.isAccessible = true
        assertEquals("rescript format", method.invoke(service))
    }

    @Test
    fun testNotificationGroupId() {
        val method = service.javaClass.getDeclaredMethod("getNotificationGroupId")
        method.isAccessible = true
        assertEquals("ReScript", method.invoke(service))
    }
}
