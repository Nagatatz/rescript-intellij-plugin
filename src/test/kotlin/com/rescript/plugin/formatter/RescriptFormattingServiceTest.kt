package com.rescript.plugin.formatter

import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for [RescriptFormattingService].
 *
 * Covers the public predicates, metadata, cancellation before launch and failed output delivery.
 * A mocked AsyncFormattingRequest and a temporary CLI script exercise the formatting task
 * with an initialized IntelliJ fixture; portable process lifecycle tests live in the runner suite.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptFormattingServiceTest {
    private lateinit var myFixture: CodeInsightTestFixture

    private lateinit var service: RescriptFormattingService

    @BeforeEach
    fun setUpService() {
        service = RescriptFormattingService()
    }

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

    @Test
    @DisabledOnOs(OS.WINDOWS)
    fun `cancelled formatting task does not start CLI or deliver text`(
        @TempDir dir: Path,
    ) {
        val request = formattingRequest(dir, "#!/bin/sh\nexit 99\n")
        val task = formattingTask(request)
        task.javaClass
            .getDeclaredMethod("cancel")
            .apply { isAccessible = true }
            .invoke(task)
        task.javaClass
            .getDeclaredMethod("run")
            .apply { isAccessible = true }
            .invoke(task)
        Mockito.verify(request, Mockito.never()).onTextReady(Mockito.anyString())
        Mockito.verify(request, Mockito.never()).onError(Mockito.anyString(), Mockito.anyString())
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    fun `failed formatting task reports stderr without delivering partial output`(
        @TempDir dir: Path,
    ) {
        val request =
            formattingRequest(dir, "#!/bin/sh\ncat >/dev/null\nprintf partial\nprintf 'syntax error' >&2\nexit 42\n")
        val task = formattingTask(request)
        task.javaClass
            .getDeclaredMethod("run")
            .apply { isAccessible = true }
            .invoke(task)
        Mockito.verify(request, Mockito.never()).onTextReady(Mockito.anyString())
        Mockito.verify(request).onError("ReScript", "syntax error")
    }

    private fun formattingRequest(
        dir: Path,
        script: String,
    ): AsyncFormattingRequest {
        val cli = Files.createDirectories(dir.resolve("node_modules/.bin")).resolve("rescript")
        Files.writeString(cli, script)
        assertTrue(cli.toFile().setExecutable(true))
        val request = Mockito.mock(AsyncFormattingRequest::class.java, Mockito.RETURNS_DEEP_STUBS)
        Mockito.`when`(request.context.project).thenReturn(myFixture.project)
        Mockito.`when`(request.ioFile).thenReturn(dir.resolve("App.res").toFile())
        Mockito.`when`(request.documentText).thenReturn("let x = 1")
        return request
    }

    private fun formattingTask(request: AsyncFormattingRequest): Any =
        service.javaClass
            .getDeclaredMethod("createFormattingTask", AsyncFormattingRequest::class.java)
            .apply { isAccessible = true }
            .invoke(service, request)!!
}
