package com.rescript.plugin.notebook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Round-trips representative notebook documents through
 * [RescriptNotebookSerializer] to guarantee that on-disk JSON and
 * the in-memory model stay in sync.
 */
class RescriptNotebookSerializerTest {
    @Test
    fun `empty document round-trips`() {
        val doc = NotebookDocument.empty()
        val json = RescriptNotebookSerializer.toJson(doc)
        assertEquals(doc, RescriptNotebookSerializer.fromJson(json))
    }

    @Test
    fun `single cell round-trips`() {
        val doc =
            NotebookDocument(
                cells = listOf(NotebookCell(code = "Js.log(\"hi\")", lastOutput = "hi")),
            )
        assertEquals(doc, RescriptNotebookSerializer.fromJson(RescriptNotebookSerializer.toJson(doc)))
    }

    @Test
    fun `multi-cell round-trips and preserves order`() {
        val doc =
            NotebookDocument(
                cells =
                    listOf(
                        NotebookCell("let x = 1", "1"),
                        NotebookCell("let y = x + 2", ""),
                        NotebookCell("Js.log(y)", "3"),
                    ),
            )
        val parsed = RescriptNotebookSerializer.fromJson(RescriptNotebookSerializer.toJson(doc))
        assertEquals(doc.cells, parsed.cells)
    }

    @Test
    fun `blank input becomes empty document`() {
        assertEquals(NotebookDocument.empty(), RescriptNotebookSerializer.fromJson("   "))
    }

    @Test
    fun `invalid JSON throws IllegalStateException`() {
        assertThrows(IllegalStateException::class.java) {
            RescriptNotebookSerializer.fromJson("not json")
        }
    }

    @Test
    fun `top-level non-object JSON throws IllegalStateException`() {
        assertThrows(IllegalStateException::class.java) {
            RescriptNotebookSerializer.fromJson("[1, 2, 3]")
        }
    }

    @Test
    fun `unknown fields are ignored on read`() {
        val text =
            """
            {
              "version": 1,
              "cells": [{"code": "x", "lastOutput": "", "extraField": "ignored"}],
              "topLevelExtra": true
            }
            """.trimIndent()
        val doc = RescriptNotebookSerializer.fromJson(text)
        assertEquals(1, doc.cells.size)
        assertEquals("x", doc.cells.first().code)
    }

    @Test
    fun `output ends with a newline`() {
        val text = RescriptNotebookSerializer.toJson(NotebookDocument.empty())
        assertTrue(text.endsWith("\n"))
    }
}
