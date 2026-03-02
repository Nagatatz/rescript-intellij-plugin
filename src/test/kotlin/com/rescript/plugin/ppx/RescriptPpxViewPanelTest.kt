package com.rescript.plugin.ppx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
