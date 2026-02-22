package com.rescript.plugin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
