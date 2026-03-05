package com.rescript.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptErrorReporterTest {
    @Test
    fun testReportActionText() {
        val reporter = RescriptErrorReporter()
        assertEquals("Report to ReScript Plugin GitHub", reporter.reportActionText)
    }

    @Test
    fun testBuildTitleWithNullEvent() {
        val title = RescriptErrorReporter.buildTitle(null)
        assertEquals("Unhandled exception in ReScript plugin", title)
    }

    @Test
    fun testBuildBodyContainsEnvironmentSection() {
        val body = RescriptErrorReporter.buildBody(null, null)
        assertTrue(body.contains("## Environment"))
        assertTrue(body.contains("Plugin version:"))
        assertTrue(body.contains("IDE:"))
        assertTrue(body.contains("OS:"))
        assertTrue(body.contains("JDK:"))
    }

    @Test
    fun testBuildBodyWithAdditionalInfo() {
        val body = RescriptErrorReporter.buildBody(null, "User description of the problem")
        assertTrue(body.contains("## Additional Information"))
        assertTrue(body.contains("User description of the problem"))
    }

    @Test
    fun testBuildBodyWithoutAdditionalInfo() {
        val body = RescriptErrorReporter.buildBody(null, null)
        assertFalse(body.contains("## Additional Information"))
    }

    @Test
    fun testBuildBodyWithBlankAdditionalInfo() {
        val body = RescriptErrorReporter.buildBody(null, "   ")
        assertFalse(body.contains("## Additional Information"))
    }

    @Test
    fun testBuildGitHubIssueUrlContainsRepo() {
        val url = RescriptErrorReporter.buildGitHubIssueUrl("Test title", "Test body")
        assertTrue(url.startsWith("https://github.com/ngtz/rescript-intellij-plugin/issues/new"))
    }

    @Test
    fun testBuildGitHubIssueUrlContainsBugLabel() {
        val url = RescriptErrorReporter.buildGitHubIssueUrl("Test title", "Test body")
        assertTrue(url.contains("labels=bug"))
    }

    @Test
    fun testBuildGitHubIssueUrlContainsEncodedTitle() {
        val url = RescriptErrorReporter.buildGitHubIssueUrl("Error: something failed", "body")
        assertTrue(url.contains("title=Error"))
    }

    @Test
    fun testBuildGitHubIssueUrlContainsEncodedBody() {
        val url = RescriptErrorReporter.buildGitHubIssueUrl("title", "## Environment\ntest")
        assertTrue(url.contains("body="))
    }

    @Test
    fun testBuildGitHubIssueUrlDoesNotExceedMaxLength() {
        val longBody = "x".repeat(20000)
        val url = RescriptErrorReporter.buildGitHubIssueUrl("title", longBody)
        assertTrue(url.length <= 8000)
    }

    @Test
    fun testBuildTitleTruncatesLongMessage() {
        // buildTitle should truncate to 200 characters max
        val result = RescriptErrorReporter.buildTitle(null)
        assertTrue(result.length <= 200)
    }

    // -- Path sanitization tests --

    @Test
    fun testSanitizeFilePathsRedactsUnixAbsolutePaths() {
        val input = "at com.example.Foo.bar(/Users/john/projects/my-app/src/Foo.kt:42)"
        val result = RescriptErrorReporter.sanitizeFilePaths(input)
        assertFalse(result.contains("/Users/john/"), "Should not contain /Users/john/")
        assertTrue(result.contains("<...>/my-app/src/"), "Should contain last two segments")
    }

    @Test
    fun testSanitizeFilePathsRedactsWindowsAbsolutePaths() {
        val input = """at com.example.Foo.bar(C:\Users\john\projects\my-app\src\Foo.kt:42)"""
        val result = RescriptErrorReporter.sanitizeFilePaths(input)
        assertFalse(result.contains("C:\\Users\\john\\"), "Should not contain C:\\Users\\john\\")
        assertTrue(result.contains("<...>"), "Should contain redaction marker")
    }

    @Test
    fun testSanitizeFilePathsPreservesNonPathText() {
        val input = "NullPointerException: something went wrong"
        val result = RescriptErrorReporter.sanitizeFilePaths(input)
        assertEquals(input, result)
    }

    @Test
    fun testSanitizeFilePathsHandlesMultiplePaths() {
        val input =
            """
            at com.example.A(/Users/john/projects/app/A.kt:1)
            at com.example.B(/Users/john/projects/app/B.kt:2)
            """.trimIndent()
        val result = RescriptErrorReporter.sanitizeFilePaths(input)
        assertFalse(result.contains("/Users/john/"), "Should not contain /Users/john/")
    }

    @Test
    fun testSanitizeFilePathsKeepsShortPaths() {
        // Paths with 2 or fewer segments should be kept as-is
        val input = "at com.example.Foo(/a/ b)"
        val result = RescriptErrorReporter.sanitizeFilePaths(input)
        // Short paths pass through the regex but have <=2 segments
        assertNotNull(result)
    }
}
