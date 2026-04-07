package com.rescript.plugin.paste

import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import java.awt.datatransfer.DataFlavor

/**
 * Post-processes paste operations to convert HTML markup to ReScript JSX syntax.
 *
 * Automatically transforms common HTML patterns when pasting into ReScript files:
 * - Attribute names: `class` -> `className`, `for` -> `htmlFor`, `onclick` -> `onClick`, etc.
 * - Void elements: `<br>` -> `<br />`, `<img ...>` -> `<img ... />`, etc.
 * - Style attributes: `style="color: red"` -> `style={ReactDOM.Style.make(~color="red", ())}`
 *
 * Only activates when the pasted content appears to contain HTML tags (not React JSX).
 * React JSX content (with `className=`, camelCase handlers, or expression braces)
 * is excluded and handled by [RescriptPasteAsRescriptProcessor] instead.
 *
 * Extends [RescriptBasePasteProcessor] which provides the common clipboard extraction,
 * RescriptFile guard, and WriteCommandAction replacement workflow.
 */
class RescriptPasteAsJsxProcessor : RescriptBasePasteProcessor<HtmlTransferData>() {
    override fun detectContent(text: String): Boolean = looksLikeHtml(text)

    override fun convertContent(text: String): String = convertHtmlToJsx(text)

    override fun createTransferableData(text: String): HtmlTransferData = HtmlTransferData(text)

    override fun getOriginalText(data: HtmlTransferData): String = data.originalHtml

    override val transferableDataClass: Class<HtmlTransferData> = HtmlTransferData::class.java

    companion object {
        // HTML attributes that need renaming for JSX
        private val ATTRIBUTE_MAP =
            mapOf(
                "class" to "className",
                "for" to "htmlFor",
                "onclick" to "onClick",
                "onchange" to "onChange",
                "onsubmit" to "onSubmit",
                "onfocus" to "onFocus",
                "onblur" to "onBlur",
                "onkeydown" to "onKeyDown",
                "onkeyup" to "onKeyUp",
                "onkeypress" to "onKeyPress",
                "onmousedown" to "onMouseDown",
                "onmouseup" to "onMouseUp",
                "onmouseover" to "onMouseOver",
                "onmouseout" to "onMouseOut",
                "ondblclick" to "onDoubleClick",
                "tabindex" to "tabIndex",
                "readonly" to "readOnly",
                "maxlength" to "maxLength",
                "cellpadding" to "cellPadding",
                "cellspacing" to "cellSpacing",
                "colspan" to "colSpan",
                "rowspan" to "rowSpan",
                "enctype" to "encType",
                "crossorigin" to "crossOrigin",
                "autocomplete" to "autoComplete",
                "autofocus" to "autoFocus",
                "autoplay" to "autoPlay",
            )

        // HTML void elements that should be self-closing
        private val VOID_ELEMENTS =
            setOf(
                "area",
                "base",
                "br",
                "col",
                "embed",
                "hr",
                "img",
                "input",
                "link",
                "meta",
                "param",
                "source",
                "track",
                "wbr",
            )

        // Pattern to detect HTML attribute assignments
        private val ATTR_PATTERN = Regex("""(?<=\s)([a-z]+)(=)""")

        // Pattern to detect void elements that aren't self-closing
        private val VOID_TAG_PATTERN = Regex("""<(${VOID_ELEMENTS.joinToString("|")})(\s[^>]*)?>""")

        // Pattern matching an HTML-like opening tag
        private val HTML_TAG_PATTERN = Regex("<[a-zA-Z][a-zA-Z0-9]*[\\s>/]")

        // Pattern matching inline style attribute: style="..."
        private val STYLE_ATTR_PATTERN = Regex("""style="([^"]*)"""")

        // Pattern detecting camelCase event handlers (e.g., `onClick=`, `onChange=`)
        private val CAMEL_CASE_HANDLER_PATTERN = Regex("""on[A-Z]\w+=""")

        // Pattern detecting JSX expression braces (e.g., `{variable}`, `{fn()}`)
        private val JSX_EXPRESSION_PATTERN = Regex("""\{[^}]+}""")

        /**
         * Checks whether the given text appears to contain HTML markup.
         *
         * Returns false for React JSX content (which already uses `className=`,
         * camelCase event handlers, or JSX expression braces), so that JSX/TSX
         * is handled by [RescriptPasteAsRescriptProcessor] instead.
         *
         * @param text the clipboard text to check
         * @return true if the text likely contains HTML tags (not JSX)
         */
        internal fun looksLikeHtml(text: String): Boolean {
            val trimmed = text.trim()
            if (!trimmed.contains("<") || !trimmed.contains(">")) return false
            if (!HTML_TAG_PATTERN.containsMatchIn(trimmed)) return false

            // Exclude React JSX: if text already uses JSX conventions, it's not HTML
            if (looksLikeJsx(trimmed)) return false

            return true
        }

        /**
         * Checks whether the text appears to be React JSX rather than plain HTML.
         *
         * Detects JSX-specific patterns: `className=` attribute, camelCase event
         * handlers (`onClick=`, etc.), and expression braces (`{expr}`).
         *
         * @param text the text to check
         * @return true if the text looks like React JSX
         */
        internal fun looksLikeJsx(text: String): Boolean {
            // className= is already JSX (HTML uses class=)
            if (text.contains("className=")) return true
            // Expression braces like {variable} or {fn()} indicate JSX
            if (JSX_EXPRESSION_PATTERN.containsMatchIn(text)) return true
            // camelCase event handlers like onClick= indicate JSX
            if (CAMEL_CASE_HANDLER_PATTERN.containsMatchIn(text)) return true
            return false
        }

        /**
         * Converts HTML markup to ReScript JSX syntax.
         *
         * Performs attribute renaming, void element self-closing, and
         * style attribute conversion.
         *
         * @param html the HTML string to convert
         * @return the converted ReScript JSX string
         */
        internal fun convertHtmlToJsx(html: String): String {
            var result = html

            // Rename HTML attributes to JSX equivalents
            result =
                ATTR_PATTERN.replace(result) { match ->
                    val attrName = match.groupValues[1]
                    val jsxName = ATTRIBUTE_MAP[attrName] ?: attrName
                    "$jsxName="
                }

            // Self-close void elements: <br> -> <br />, <img src="..."> -> <img src="..." />
            result =
                VOID_TAG_PATTERN.replace(result) { match ->
                    val fullMatch = match.value
                    if (fullMatch.endsWith("/>")) {
                        fullMatch
                    } else {
                        fullMatch.dropLast(1) + " />"
                    }
                }

            // Convert style="..." to style={ReactDOM.Style.make(..., ())}
            result = convertStyleAttributes(result)

            return result
        }

        /**
         * Converts inline `style="..."` attributes to ReScript `ReactDOM.Style.make()` calls.
         *
         * @param html the HTML/JSX string
         * @return the string with style attributes converted
         */
        private fun convertStyleAttributes(html: String): String {
            return STYLE_ATTR_PATTERN.replace(html) { match ->
                val cssText = match.groupValues[1]
                val props =
                    cssText
                        .split(";")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString(", ") { prop ->
                            val parts = prop.split(":", limit = 2)
                            if (parts.size == 2) {
                                val name = cssPropertyToCamelCase(parts[0].trim())
                                val value = parts[1].trim()
                                "~$name=\"$value\""
                            } else {
                                ""
                            }
                        }.ifEmpty { return@replace match.value }
                "style={ReactDOM.Style.make($props, ())}"
            }
        }

        /**
         * Converts a CSS property name to camelCase for ReScript/React style objects.
         *
         * @param cssProp the CSS property (e.g., "background-color")
         * @return the camelCase version (e.g., "backgroundColor")
         */
        internal fun cssPropertyToCamelCase(cssProp: String): String {
            val parts = cssProp.split("-")
            return parts.first() +
                parts.drop(1).joinToString("") { part ->
                    part.replaceFirstChar { it.uppercaseChar() }
                }
        }
    }
}

/**
 * Transferable data wrapper for HTML content detected during paste.
 *
 * @property originalHtml the original HTML text from the clipboard
 */
class HtmlTransferData(
    val originalHtml: String,
) : TextBlockTransferableData {
    override fun getFlavor(): DataFlavor = DATA_FLAVOR

    override fun getOffsetCount(): Int = 0

    override fun getOffsets(
        offsets: IntArray,
        index: Int,
    ): Int = index

    override fun setOffsets(
        offsets: IntArray,
        index: Int,
    ): Int = index

    companion object {
        private val DATA_FLAVOR =
            DataFlavor(
                HtmlTransferData::class.java,
                "ReScript HTML to JSX",
            )
    }
}
