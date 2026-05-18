package com.rescript.plugin.ppx

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptPpxViewPanelTest {
    @Test
    fun `findPpxAnnotations finds react component`() {
        val source =
            """
            @react.component
            let make = () => <div />
            """.trimIndent()
        val annotations = RescriptPpxViewPanel.findPpxAnnotations(source)
        assertEquals(1, annotations.size)
        assertEquals("@react.component", annotations[0].first)
        assertEquals(1, annotations[0].second)
    }

    @Test
    fun `findPpxAnnotations finds multiple annotations`() {
        val source =
            """
            @genType
            @react.component
            let make = (~name: string) => <div />
            """.trimIndent()
        val annotations = RescriptPpxViewPanel.findPpxAnnotations(source)
        assertEquals(2, annotations.size)
    }

    @Test
    fun `findPpxAnnotations finds annotation with arguments`() {
        val source =
            """
            @module("react-dom")
            external render: unit => unit = "render"
            """.trimIndent()
        val annotations = RescriptPpxViewPanel.findPpxAnnotations(source)
        assertEquals(1, annotations.size)
        assertTrue(annotations[0].first.contains("@module"))
    }

    @Test
    fun `findPpxAnnotations returns empty for no annotations`() {
        val source = "let x = 42"
        val annotations = RescriptPpxViewPanel.findPpxAnnotations(source)
        assertTrue(annotations.isEmpty())
    }

    @Test
    fun `getPpxExpansionInfo returns description for react component`() {
        val info = RescriptPpxViewPanel.getPpxExpansionInfo("@react.component")
        assertTrue(info.contains("React"))
    }

    @Test
    fun `getPpxExpansionInfo returns description for genType`() {
        val info = RescriptPpxViewPanel.getPpxExpansionInfo("@genType")
        assertTrue(info.contains("TypeScript") || info.contains(".gen.tsx"))
    }

    @Test
    fun `getPpxExpansionInfo returns description for module`() {
        val info = RescriptPpxViewPanel.getPpxExpansionInfo("@module")
        assertTrue(info.contains("JavaScript") || info.contains("module"))
    }

    @Test
    fun `getPpxExpansionInfo returns generic for unknown`() {
        val info = RescriptPpxViewPanel.getPpxExpansionInfo("@customPpx")
        assertEquals("Custom PPX annotation", info)
    }

    @Test
    fun `PPX_DESCRIPTIONS contains common annotations`() {
        val descriptions = RescriptPpxViewPanel.PPX_DESCRIPTIONS
        assertNotNull(descriptions["react.component"])
        assertNotNull(descriptions["genType"])
        assertNotNull(descriptions["module"])
        assertNotNull(descriptions["val"])
        assertNotNull(descriptions["send"])
        assertNotNull(descriptions["deriving"])
    }

    @Test
    fun `findPpxAnnotations correct line numbers`() {
        val source =
            """
            let x = 42

            @react.component
            let make = () => <div />
            """.trimIndent()
        val annotations = RescriptPpxViewPanel.findPpxAnnotations(source)
        assertEquals(1, annotations.size)
        assertEquals(3, annotations[0].second)
    }

    @Test
    fun `renderHtml returns empty-state message when no annotations`() {
        val html = RescriptPpxViewPanel.renderHtml(emptyList(), "#888888")
        assertTrue(html.startsWith("<html>"), "expected HTML wrapper")
        assertTrue(html.contains("No PPX annotations found"), "expected empty-state copy in $html")
    }

    @Test
    fun `renderHtml wraps each annotation token in a coloured span`() {
        val html =
            RescriptPpxViewPanel.renderHtml(
                listOf("@react.component" to 5),
                "#3E72C2",
            )
        assertTrue(html.contains("color:#3E72C2"), "expected colour attribute in $html")
        assertTrue(html.contains("font-weight:bold"), "expected bold annotation in $html")
        assertTrue(html.contains("@react.component"), "annotation text must appear")
        assertTrue(html.contains("Line 5"), "line number must appear")
    }

    @Test
    fun `renderHtml emits one entry per annotation in source order`() {
        val html =
            RescriptPpxViewPanel.renderHtml(
                listOf(
                    "@genType" to 1,
                    "@react.component" to 2,
                ),
                "#3E72C2",
            )
        val genTypePos = html.indexOf("@genType")
        val componentPos = html.indexOf("@react.component")
        assertTrue(genTypePos in 0 until componentPos, "annotations must appear in source order")
    }

    @Test
    fun `renderHtml escapes HTML metacharacters in expansion text`() {
        // The "@string" expansion text contains the literal "<" inside
        // "string enum values for polymorphic variants" — no escaping
        // needed there, but we cover the escape path by checking the
        // annotation token does not break the HTML structure.
        val html =
            RescriptPpxViewPanel.renderHtml(
                listOf("@module(\"react-dom\")" to 1),
                "#888888",
            )
        // " characters inside the annotation text must be escaped to &quot;
        assertTrue(html.contains("&quot;react-dom&quot;"), "quotes must be escaped in $html")
    }
}
