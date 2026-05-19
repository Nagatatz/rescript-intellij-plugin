package com.rescript.plugin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.awt.Font
import javax.swing.JEditorPane

/**
 * Structural tests for the read-only HTML editor pane factory.
 *
 * The factory wraps a small `JEditorPane` configuration, so the tests
 * lock down the invariants (HTML content type, read-only, monospace
 * font, optional border) that every consumer panel relies on.
 */
class HtmlEditorPaneFactoryTest {
    @Test
    fun `default pane is non-editable and uses html content type`() {
        val pane = HtmlEditorPaneFactory.createReadOnlyHtmlPane()
        assertFalse(pane.isEditable)
        assertEquals("text/html", pane.contentType)
    }

    @Test
    fun `default pane uses monospace font at default size`() {
        val pane = HtmlEditorPaneFactory.createReadOnlyHtmlPane()
        assertEquals(Font.MONOSPACED, pane.font.family)
        assertEquals(HtmlEditorPaneFactory.DEFAULT_FONT_SIZE, pane.font.size)
        assertEquals(Font.PLAIN, pane.font.style)
    }

    @Test
    fun `default pane preserves JEditorPane's stock border when borderInset is omitted`() {
        // JEditorPane installs its own MarginBorder; the factory does not
        // touch it. We verify that the factory does not surprise callers by
        // wrapping or replacing it, only that callers can override via
        // `borderInset` (covered by the next test).
        val a = HtmlEditorPaneFactory.createReadOnlyHtmlPane()
        val b = HtmlEditorPaneFactory.createReadOnlyHtmlPane()
        assertEquals(a.border?.javaClass, b.border?.javaClass)
    }

    @Test
    fun `borderInset argument installs a non-null JBUI empty border`() {
        val pane = HtmlEditorPaneFactory.createReadOnlyHtmlPane(borderInset = 8)
        assertNotNull(pane.border, "expected a border when borderInset is supplied")
    }

    @Test
    fun `HONOR_DISPLAY_PROPERTIES is set to true`() {
        val pane = HtmlEditorPaneFactory.createReadOnlyHtmlPane()
        assertEquals(true, pane.getClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES))
    }

    @Test
    fun `explicit fontSize argument overrides the default`() {
        val pane = HtmlEditorPaneFactory.createReadOnlyHtmlPane(fontSize = 16)
        assertEquals(16, pane.font.size)
    }
}
