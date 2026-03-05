package com.rescript.plugin.imports

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptUnusedOpenDetectorTest {
    // ── isUnusedOpenWarning() tests ─────────────────────────────────────

    @Test
    fun testMatchesStandardUnusedOpenMessage() {
        val info = buildWarningHighlightInfo("unused open Belt")
        assertTrue(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testMatchesUnusedOpenWithDottedPath() {
        val info = buildWarningHighlightInfo("unused open Belt.Array")
        assertTrue(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testMatchesUnusedOpenWithDeepPath() {
        val info = buildWarningHighlightInfo("unused open Js.Promise2.Result")
        assertTrue(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testMatchesCaseInsensitive() {
        val info = buildWarningHighlightInfo("Unused Open Belt")
        assertTrue(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testMatchesUpperCaseMessage() {
        val info = buildWarningHighlightInfo("UNUSED OPEN Belt")
        assertTrue(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testRejectsNonWarningErrorSeverity() {
        val info = buildHighlightInfo(HighlightInfoType.ERROR, "unused open Belt")
        assertFalse(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testRejectsNonWarningInfoSeverity() {
        val info = buildHighlightInfo(HighlightInfoType.INFORMATION, "unused open Belt")
        assertFalse(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testRejectsUnrelatedWarningMessage() {
        val info = buildWarningHighlightInfo("unused variable x")
        assertFalse(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testRejectsPartialMatchOpen() {
        val info = buildWarningHighlightInfo("unused opening bracket")
        assertFalse(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testRejectsNullDescription() {
        // HighlightInfo with no description set
        val info =
            HighlightInfo
                .newHighlightInfo(HighlightInfoType.WARNING)
                .range(0, 10)
                .createUnconditionally()
        assertFalse(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testRejectsEmptyDescription() {
        val info = buildWarningHighlightInfo("")
        assertFalse(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    @Test
    fun testMatchesMessageWithLeadingContext() {
        // Some LSP implementations may prefix the message
        val info = buildWarningHighlightInfo("Warning: unused open Belt.Array")
        assertTrue(RescriptUnusedOpenDetector.isUnusedOpenWarning(info))
    }

    // ── Helper methods ──────────────────────────────────────────────────

    private fun buildWarningHighlightInfo(description: String): HighlightInfo =
        buildHighlightInfo(HighlightInfoType.WARNING, description)

    private fun buildHighlightInfo(
        type: HighlightInfoType,
        description: String,
    ): HighlightInfo =
        HighlightInfo
            .newHighlightInfo(type)
            .range(0, 10)
            .description(description)
            .createUnconditionally()
}
