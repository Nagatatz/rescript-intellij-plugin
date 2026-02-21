package com.rescript.plugin.paste

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

/**
 * Post-processes paste operations to convert HTML markup to ReScript JSX syntax.
 *
 * Automatically transforms common HTML patterns when pasting into ReScript files:
 * - Attribute names: `class` -> `className`, `for` -> `htmlFor`, `onclick` -> `onClick`, etc.
 * - Void elements: `<br>` -> `<br />`, `<img ...>` -> `<img ... />`, etc.
 * - Style attributes: `style="color: red"` -> `style={ReactDOM.Style.make(~color="red", ())}`
 *
 * Only activates when the pasted content appears to contain HTML tags.
 */
class RescriptPasteAsJsxProcessor : CopyPastePostProcessor<TextBlockTransferableData>() {
    override fun collectTransferableData(
        file: PsiFile,
        editor: Editor,
        startOffsets: IntArray,
        endOffsets: IntArray,
    ): List<TextBlockTransferableData> = emptyList()

    override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> {
        if (!content.isDataFlavorSupported(DataFlavor.stringFlavor)) return emptyList()
        val text = content.getTransferData(DataFlavor.stringFlavor) as? String ?: return emptyList()
        if (!looksLikeHtml(text)) return emptyList()
        return listOf(HtmlTransferData(text))
    }

    override fun processTransferableData(
        project: Project,
        editor: Editor,
        bounds: RangeMarker,
        caretOffset: Int,
        indented: com.intellij.openapi.util.Ref<in Boolean>,
        values: MutableList<out TextBlockTransferableData>,
    ) {
        val file =
            com.intellij.psi.PsiDocumentManager
                .getInstance(project)
                .getPsiFile(editor.document)
        if (file !is RescriptFile) return

        val htmlData = values.filterIsInstance<HtmlTransferData>().firstOrNull() ?: return
        val converted = convertHtmlToJsx(htmlData.originalHtml)
        if (converted == htmlData.originalHtml) return

        // Replace the pasted range with converted JSX
        val document = editor.document
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(bounds.startOffset, bounds.endOffset, converted)
        }
    }

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

        /**
         * Checks whether the given text appears to contain HTML markup.
         *
         * @param text the clipboard text to check
         * @return true if the text likely contains HTML tags
         */
        internal fun looksLikeHtml(text: String): Boolean {
            val trimmed = text.trim()
            return trimmed.contains("<") &&
                trimmed.contains(">") &&
                Regex("<[a-zA-Z][a-zA-Z0-9]*[\\s>/]").containsMatchIn(trimmed)
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
            val stylePattern = Regex("""style="([^"]*)"""")
            return stylePattern.replace(html) { match ->
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
private class HtmlTransferData(
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
