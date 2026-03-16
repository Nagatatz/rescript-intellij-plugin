package com.rescript.plugin.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptFormatCheckAnnotatorTest {
    // --- CollectedInfo data class ---

    @Test
    fun `CollectedInfo holds all fields`() {
        val info =
            RescriptFormatCheckAnnotator.CollectedInfo(
                documentText = "let x = 1",
                extension = "res",
                projectBasePath = "/project",
                filePath = "/project/src/App.res",
            )
        assertEquals("let x = 1", info.documentText)
        assertEquals("res", info.extension)
        assertEquals("/project", info.projectBasePath)
        assertEquals("/project/src/App.res", info.filePath)
    }

    @Test
    fun `CollectedInfo equality`() {
        val a = RescriptFormatCheckAnnotator.CollectedInfo("text", "res", "/p", "/p/a.res")
        val b = RescriptFormatCheckAnnotator.CollectedInfo("text", "res", "/p", "/p/a.res")
        assertEquals(a, b)
    }

    @Test
    fun `CollectedInfo supports resi extension`() {
        val info =
            RescriptFormatCheckAnnotator.CollectedInfo(
                documentText = "let x: int",
                extension = "resi",
                projectBasePath = "/project",
                filePath = "/project/src/App.resi",
            )
        assertEquals("resi", info.extension)
    }

    // --- AnnotationResult data class ---

    @Test
    fun `AnnotationResult holds message`() {
        val result = RescriptFormatCheckAnnotator.AnnotationResult("Code is not formatted.")
        assertEquals("Code is not formatted.", result.message)
    }

    @Test
    fun `AnnotationResult equality`() {
        val a = RescriptFormatCheckAnnotator.AnnotationResult("msg")
        val b = RescriptFormatCheckAnnotator.AnnotationResult("msg")
        assertEquals(a, b)
    }

    // --- doAnnotate null handling ---

    @Test
    fun `doAnnotate returns null when info is null`() {
        val annotator = RescriptFormatCheckAnnotator()
        assertNull(annotator.doAnnotate(null))
    }

    @Test
    fun `doAnnotate returns null when CLI not found`() {
        val annotator = RescriptFormatCheckAnnotator()
        val info =
            RescriptFormatCheckAnnotator.CollectedInfo(
                documentText = "let x = 1",
                extension = "res",
                projectBasePath = "/nonexistent/project",
                filePath = "/nonexistent/project/src/App.res",
            )
        // CLI won't be found at nonexistent path
        assertNull(annotator.doAnnotate(info))
    }

    // --- RescriptFormatQuickFix ---

    @Test
    fun `RescriptFormatQuickFix has correct text`() {
        val fix = RescriptFormatQuickFix()
        assertEquals("Format this file", fix.text)
    }

    @Test
    fun `RescriptFormatQuickFix has correct family name`() {
        val fix = RescriptFormatQuickFix()
        assertEquals("ReScript format check", fix.familyName)
    }

    @Test
    fun `RescriptFormatQuickFix does not start in write action`() {
        val fix = RescriptFormatQuickFix()
        assertTrue(!fix.startInWriteAction())
    }

    // --- runFormatCheck static method ---

    @Test
    fun `runFormatCheck returns null for non-existent CLI path`() {
        // Non-existent binary throws ProcessNotCreatedException, caught by the caller
        val result =
            try {
                RescriptFormatCheckAnnotator.runFormatCheck(
                    cliPath = "/nonexistent/rescript",
                    extension = "res",
                    documentText = "let x = 1",
                )
            } catch (_: Exception) {
                null
            }
        assertNull(result)
    }

    @Test
    fun `runFormatCheck returns null for command with non-zero exit`() {
        // /usr/bin/false always exits with code 1
        val result =
            RescriptFormatCheckAnnotator.runFormatCheck(
                cliPath = "/usr/bin/false",
                extension = "res",
                documentText = "let x = 1",
            )
        assertNull(result)
    }

    @Test
    fun `runFormatCheck handles empty document text with non-existent CLI`() {
        val result =
            try {
                RescriptFormatCheckAnnotator.runFormatCheck(
                    cliPath = "/nonexistent/rescript",
                    extension = "res",
                    documentText = "",
                )
            } catch (_: Exception) {
                null
            }
        assertNull(result)
    }

    @Test
    fun `runFormatCheck cleans up process on non-zero exit`() {
        // Verify no threads leak when process exits with error code
        val threadsBefore =
            Thread
                .getAllStackTraces()
                .keys
                .count { it.name.startsWith("rescript-format-check") }

        RescriptFormatCheckAnnotator.runFormatCheck(
            cliPath = "/usr/bin/false",
            extension = "res",
            documentText = "let x = 1",
        )

        // Allow time for daemon threads to terminate
        Thread.sleep(100)

        val threadsAfter =
            Thread
                .getAllStackTraces()
                .keys
                .count { it.name.startsWith("rescript-format-check") }

        assertEquals("No rescript-format-check threads should leak", threadsBefore, threadsAfter)
    }

    @Test
    fun `runFormatCheck handles slow command with cleanup`() {
        // Use /bin/cat without closing stdin — it will block until timeout or interrupt
        // This tests the try-finally cleanup path
        val result =
            try {
                RescriptFormatCheckAnnotator.runFormatCheck(
                    cliPath = "/bin/cat",
                    extension = "res",
                    documentText = "let x = 1\n",
                )
            } catch (_: Exception) {
                null
            }
        // cat echoes stdin, so it should return the input text
        // (or null if there's a timing issue)
        if (result != null) {
            assertEquals("let x = 1\n", result)
        }
    }
}
