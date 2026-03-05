package com.rescript.plugin.lsp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptPpxVisualizationProviderTest {
    @Test
    fun `ppxHintFor returns hint for react_component`() {
        assertEquals(
            "generates React.createElement",
            RescriptPpxVisualizationProvider.ppxHintFor("react.component"),
        )
    }

    @Test
    fun `ppxHintFor returns hint for genType`() {
        assertEquals(
            "generates .gen.tsx",
            RescriptPpxVisualizationProvider.ppxHintFor("genType"),
        )
    }

    @Test
    fun `ppxHintFor returns hint for module`() {
        assertEquals(
            "binds to JS module",
            RescriptPpxVisualizationProvider.ppxHintFor("module"),
        )
    }

    @Test
    fun `ppxHintFor returns hint for val`() {
        assertEquals(
            "binds to JS value",
            RescriptPpxVisualizationProvider.ppxHintFor("val"),
        )
    }

    @Test
    fun `ppxHintFor returns hint for send`() {
        assertEquals(
            "binds to JS method call",
            RescriptPpxVisualizationProvider.ppxHintFor("send"),
        )
    }

    @Test
    fun `ppxHintFor returns hint for deriving json`() {
        assertEquals(
            "generates toJson + fromJson",
            RescriptPpxVisualizationProvider.ppxHintFor("deriving(json)"),
        )
    }

    @Test
    fun `ppxHintFor returns hint for deriving accessors`() {
        assertEquals(
            "generates field accessors",
            RescriptPpxVisualizationProvider.ppxHintFor("deriving(accessors)"),
        )
    }

    @Test
    fun `ppxHintFor returns hint for unboxed`() {
        assertEquals(
            "unboxed representation",
            RescriptPpxVisualizationProvider.ppxHintFor("unboxed"),
        )
    }

    @Test
    fun `ppxHintFor returns null for unknown annotation`() {
        assertNull(RescriptPpxVisualizationProvider.ppxHintFor("unknown"))
    }

    @Test
    fun `PPX_HINTS map has expected size`() {
        assertTrue(RescriptPpxVisualizationProvider.PPX_HINTS.size >= 11)
    }

    @Test
    fun `ppxHintFor returns hint for get`() {
        assertEquals("binds to JS property getter", RescriptPpxVisualizationProvider.ppxHintFor("get"))
    }

    @Test
    fun `ppxHintFor returns hint for set`() {
        assertEquals("binds to JS property setter", RescriptPpxVisualizationProvider.ppxHintFor("set"))
    }

    @Test
    fun `ppxHintFor returns hint for new`() {
        assertEquals("binds to JS constructor", RescriptPpxVisualizationProvider.ppxHintFor("new"))
    }

    @Test
    fun `provider can be instantiated`() {
        assertNotNull(RescriptPpxVisualizationProvider())
    }

    @Test
    fun `provider name is correct`() {
        assertEquals("PPX annotation descriptions", RescriptPpxVisualizationProvider().name)
    }
}
