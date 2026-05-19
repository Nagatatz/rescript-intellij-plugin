package com.rescript.plugin.flow

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.rescript.plugin.highlight.RescriptSyntaxHighlighter
import com.rescript.plugin.util.RescriptColorUtils
import com.rescript.plugin.util.RescriptSecurityUtils

/**
 * Renders a Mermaid `flowchart TD` source string as HTML with each
 * Mermaid token wrapped in a coloured `<span>` so that
 * [com.intellij.openapi.fileEditor.FileEditorManager]-rendered source
 * mode of the Variant Flow and Module Dependency tool windows looks
 * close to a syntax-highlighted editor instead of monochrome plain
 * text.
 *
 * The colour for each token category is sourced from the active
 * editor scheme through [RescriptSyntaxHighlighter] keys (keywords,
 * operators, strings, comments). Tokens that have no scheme attribute
 * fall back to a neutral grey defined by [DEFAULT_HEX].
 *
 * The Mermaid syntax surface covered by v1 is limited to
 * `flowchart TD`-style files written by the plugin's own exporters
 * (keywords `flowchart` / `graph` / `TD` / `LR` / `subgraph` / `end`,
 * arrows `-->` / `---` / `-.->` / `==>`, quoted node labels, `%%`
 * comments). Anything outside this surface renders as plain text.
 */
internal object MermaidSourceColorizer {
    /** CSS colour used when the editor scheme has no attribute for a token category. */
    internal const val DEFAULT_HEX = "#888888"

    private val TOKEN_REGEX =
        Regex(
            """("[^"]*"|-\.->|-->|---|==>|\bflowchart\b|\bgraph\b|\bTD\b|\bLR\b|\bsubgraph\b|\bend\b)""",
        )

    /**
     * Builds the HTML document body for [source].
     *
     * Resolves the four token colours through the current
     * [EditorColorsManager] scheme so the rendered text matches the
     * editor's own palette.
     *
     * @param source raw Mermaid `flowchart TD` text
     * @return HTML wrapped in `<html><body>` tags, ready to drop into a
     *   `JEditorPane` with `contentType = "text/html"`
     */
    fun render(source: String): String {
        val keywordHex = hexFor(RescriptSyntaxHighlighter.KEYWORD)
        val operatorHex = hexFor(RescriptSyntaxHighlighter.OPERATOR)
        val stringHex = hexFor(RescriptSyntaxHighlighter.STRING)
        val commentHex = hexFor(RescriptSyntaxHighlighter.LINE_COMMENT)
        return renderWithColors(source, keywordHex, operatorHex, stringHex, commentHex)
    }

    /**
     * Pure variant of [render] that takes the four colour hex codes as
     * arguments so unit tests can drive the renderer without spinning
     * up an [EditorColorsManager]. Kept `internal` for tests.
     *
     * @param source raw Mermaid text
     * @param keywordHex CSS hex for keyword tokens
     * @param operatorHex CSS hex for arrow / operator tokens
     * @param stringHex CSS hex for quoted node labels
     * @param commentHex CSS hex for `%%` comment lines
     * @return HTML document body
     */
    internal fun renderWithColors(
        source: String,
        keywordHex: String,
        operatorHex: String,
        stringHex: String,
        commentHex: String,
    ): String =
        buildString {
            append("<html><body style='font-family:monospace;white-space:pre'>")
            for (line in source.lineSequence()) {
                append(renderLine(line, keywordHex, operatorHex, stringHex, commentHex))
                append("<br>")
            }
            append("</body></html>")
        }

    /**
     * Renders a single Mermaid source line as HTML. Comment lines
     * (starting with `%%` after trimming leading whitespace) get the
     * comment colour applied to the whole line; otherwise each
     * recognised token is wrapped in its category colour, and the
     * surrounding text is HTML-escaped without colouring.
     *
     * @param line one Mermaid source line, no trailing newline
     * @param keywordHex CSS hex for keyword tokens
     * @param operatorHex CSS hex for arrow tokens
     * @param stringHex CSS hex for quoted strings
     * @param commentHex CSS hex for comment lines
     * @return HTML fragment for the line (no `<br>` appended)
     */
    internal fun renderLine(
        line: String,
        keywordHex: String,
        operatorHex: String,
        stringHex: String,
        commentHex: String,
    ): String {
        if (line.trimStart().startsWith("%%")) {
            return span(RescriptSecurityUtils.escapeHtml(line), commentHex)
        }
        val sb = StringBuilder()
        var last = 0
        for (m in TOKEN_REGEX.findAll(line)) {
            sb.append(RescriptSecurityUtils.escapeHtml(line.substring(last, m.range.first)))
            val tok = m.value
            val hex =
                when {
                    tok.startsWith("\"") -> stringHex
                    tok.startsWith("-") || tok.startsWith("=") -> operatorHex
                    else -> keywordHex
                }
            sb.append(span(RescriptSecurityUtils.escapeHtml(tok), hex))
            last = m.range.last + 1
        }
        sb.append(RescriptSecurityUtils.escapeHtml(line.substring(last)))
        return sb.toString()
    }

    private fun span(
        text: String,
        hex: String,
    ): String = "<span style='color:$hex'>$text</span>"

    private fun hexFor(key: TextAttributesKey): String {
        val attributes =
            EditorColorsManager
                .getInstance()
                .globalScheme
                .getAttributes(key)
        val color = attributes?.foregroundColor ?: return DEFAULT_HEX
        return RescriptColorUtils.colorToHexString(color)
    }
}
