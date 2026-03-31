package com.rescript.plugin.util

import com.intellij.openapi.editor.Document
import org.eclipse.lsp4j.Position
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RescriptOffsetUtilsTest {
    // ── offsetToPosition ─────────────────────────────────────────

    @Test
    fun `offsetToPosition returns line 0 character 0 for offset 0`() {
        val doc = stubDocument("hello\nworld")

        val pos = RescriptOffsetUtils.offsetToPosition(doc, 0)
        assertEquals(0, pos.line)
        assertEquals(0, pos.character)
    }

    @Test
    fun `offsetToPosition returns correct position on second line`() {
        val doc = stubDocument("hello\nworld")

        // offset 8 = "wo" on second line → line 1, character 2
        val pos = RescriptOffsetUtils.offsetToPosition(doc, 8)
        assertEquals(1, pos.line)
        assertEquals(2, pos.character)
    }

    @Test
    fun `offsetToPosition returns character at line start`() {
        val doc = stubDocument("abc\ndef")

        val pos = RescriptOffsetUtils.offsetToPosition(doc, 4)
        assertEquals(1, pos.line)
        assertEquals(0, pos.character)
    }

    // ── positionToOffset ─────────────────────────────────────────

    @Test
    fun `positionToOffset returns 0 for line 0 character 0`() {
        val doc = stubDocument("hello\nworld")

        val offset = RescriptOffsetUtils.positionToOffset(doc, Position(0, 0))
        assertEquals(0, offset)
    }

    @Test
    fun `positionToOffset returns correct offset on second line`() {
        val doc = stubDocument("hello\nworld")

        // line 1, character 2 → offset 8
        val offset = RescriptOffsetUtils.positionToOffset(doc, Position(1, 2))
        assertEquals(8, offset)
    }

    @Test
    fun `positionToOffset returns -1 for negative line`() {
        val doc = stubDocument("hello")

        val offset = RescriptOffsetUtils.positionToOffset(doc, Position(-1, 0))
        assertEquals(-1, offset)
    }

    @Test
    fun `positionToOffset returns -1 for line beyond document`() {
        val doc = stubDocument("hello")

        val offset = RescriptOffsetUtils.positionToOffset(doc, Position(5, 0))
        assertEquals(-1, offset)
    }

    // ── Round-trip ───────────────────────────────────────────────

    @Test
    fun `round-trip offset to position and back`() {
        val doc = stubDocument("let x = 1\nlet y = 2\nlet z = 3")
        val targetOffset = 15

        val pos = RescriptOffsetUtils.offsetToPosition(doc, targetOffset)
        val result = RescriptOffsetUtils.positionToOffset(doc, pos)
        assertEquals(targetOffset, result)
    }

    /**
     * Creates a Document proxy stub that computes line/offset information
     * from the given text content, without requiring Mockito.
     */
    private fun stubDocument(text: String): Document {
        // Pre-compute line start offsets from text content
        val lineStarts = mutableListOf(0)
        for (i in text.indices) {
            if (text[i] == '\n') {
                lineStarts.add(i + 1)
            }
        }
        val lineCount = lineStarts.size

        return java.lang.reflect.Proxy.newProxyInstance(
            Document::class.java.classLoader,
            arrayOf(Document::class.java),
        ) { _, method, args ->
            when (method.name) {
                "getText" -> {
                    text
                }

                "getTextLength" -> {
                    text.length
                }

                "getLineCount" -> {
                    lineCount
                }

                "getLineNumber" -> {
                    val offset = args[0] as Int
                    // Find the line containing this offset
                    var line = 0
                    for (i in lineStarts.indices) {
                        if (i + 1 < lineStarts.size && lineStarts[i + 1] <= offset) {
                            line = i + 1
                        } else {
                            break
                        }
                    }
                    line
                }

                "getLineStartOffset" -> {
                    val line = args[0] as Int
                    lineStarts[line]
                }

                "toString" -> {
                    "StubDocument(${text.take(20)}...)"
                }

                "hashCode" -> {
                    text.hashCode()
                }

                "equals" -> {
                    false
                }

                else -> {
                    null
                }
            }
        } as Document
    }
}
