package com.rescript.plugin.ppx

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.rescript.plugin.highlight.RescriptSyntaxHighlighter
import com.rescript.plugin.util.RescriptColorUtils
import com.rescript.plugin.util.RescriptFileUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JPanel

/**
 * Swing panel for the PPX expansion view tool window.
 *
 * Displays information about PPX annotations found in the current ReScript file,
 * including their expansion effects and generated code descriptions.
 *
 * @param project the current IntelliJ project
 * @param parentDisposable lifecycle owner for resource cleanup
 *
 * @see RescriptPpxViewToolWindowFactory which creates this panel
 */
class RescriptPpxViewPanel(
    private val project: Project,
    parentDisposable: Disposable,
) {
    private val infoArea =
        JEditorPane().apply {
            contentType = "text/html"
            isEditable = false
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
            font = Font(Font.MONOSPACED, Font.PLAIN, 13)
            border = JBUI.Borders.empty(8)
        }

    @Suppress("DialogTitleCapitalization") // "PPX" is an acronym
    private val headerLabel =
        JBLabel("PPX Annotations").apply {
            border = JBUI.Borders.empty(4, 8)
            font = font.deriveFont(Font.BOLD)
        }

    private val mainPanel: JPanel =
        JPanel(BorderLayout()).apply {
            add(headerLabel, BorderLayout.NORTH)
            add(JBScrollPane(infoArea), BorderLayout.CENTER)
        }

    val component: JComponent
        get() = mainPanel

    init {
        EditorFactory.getInstance().eventMulticaster.addCaretListener(
            object : CaretListener {
                override fun caretPositionChanged(event: CaretEvent) {
                    val editor = event.editor
                    val document = editor.document
                    val file = FileDocumentManager.getInstance().getFile(document) ?: return
                    if (!RescriptFileUtil.isRescriptFileName(file.name)) return

                    updatePpxInfo(document.text)
                }
            },
            parentDisposable,
        )
    }

    private fun updatePpxInfo(sourceText: String) {
        val annotations = findPpxAnnotations(sourceText)
        infoArea.text = renderHtml(annotations, annotationColorHex())
    }

    /**
     * Reads the foreground colour the editor scheme assigns to
     * [RescriptSyntaxHighlighter.ANNOTATION] and returns it as a CSS
     * `#RRGGBB` literal. Falls back to a neutral grey when the scheme
     * has no foreground configured (e.g. inside test fixtures).
     */
    private fun annotationColorHex(): String {
        val attributes =
            EditorColorsManager
                .getInstance()
                .globalScheme
                .getAttributes(RescriptSyntaxHighlighter.ANNOTATION)
        val color: Color = attributes?.foregroundColor ?: return DEFAULT_ANNOTATION_HEX
        return RescriptColorUtils.colorToHexString(color)
    }

    companion object {
        // Pattern matching @annotation or @annotation(...)
        private val ANNOTATION_PATTERN = Regex("""@(\w+(?:\.\w+)*)(?:\([^)]*\))?""")

        /** Pattern extracting the PPX name from an annotation string (e.g., `@react.component`). */
        private val PPX_NAME_PATTERN = Regex("""@(\w+(?:\.\w+)*)""")

        /** CSS hex used when the editor scheme has no annotation foreground configured. */
        internal const val DEFAULT_ANNOTATION_HEX = "#888888"

        /**
         * Builds the HTML payload rendered by the panel's `JEditorPane`.
         * Pure function over the annotation list and the chosen
         * annotation colour so the rendering can be unit-tested without
         * a live Swing fixture.
         *
         * @param annotations annotation matches in source order
         * @param colorHex CSS `#RRGGBB` literal applied to every
         *   `@annotation` token
         * @return HTML document body wrapped in `<html><body>` tags
         */
        internal fun renderHtml(
            annotations: List<Pair<String, Int>>,
            colorHex: String,
        ): String {
            if (annotations.isEmpty()) {
                return "<html><body style='font-family:monospace'>" +
                    "No PPX annotations found in this file." +
                    "</body></html>"
            }
            return buildString {
                append("<html><body style='font-family:monospace;white-space:pre'>")
                for ((annotation, line) in annotations) {
                    val expansion = getPpxExpansionInfo(annotation)
                    val coloredAnnotation =
                        "<span style='color:$colorHex;font-weight:bold'>" +
                            escapeHtml(annotation) +
                            "</span>"
                    append("Line ").append(line).append(": ")
                    append(coloredAnnotation).append("<br>")
                    append("&nbsp;&nbsp;&rarr; ").append(escapeHtml(expansion)).append("<br><br>")
                }
                append("</body></html>")
            }
        }

        /** Minimal HTML escape for the literal text we drop into the rendered body. */
        private fun escapeHtml(s: String): String =
            s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")

        /**
         * Finds all PPX annotations in the source text.
         *
         * @param sourceText the ReScript source code
         * @return list of (annotation text, line number) pairs
         */
        internal fun findPpxAnnotations(sourceText: String): List<Pair<String, Int>> {
            val results = mutableListOf<Pair<String, Int>>()
            for ((lineNum, line) in sourceText.lines().withIndex()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("@")) {
                    val match = ANNOTATION_PATTERN.find(trimmed)
                    if (match != null) {
                        results.add(Pair(match.value, lineNum + 1))
                    }
                }
            }
            return results
        }

        /**
         * Returns a human-readable description of what a PPX annotation generates.
         *
         * @param annotation the PPX annotation text (e.g., "@react.component")
         * @return description of the expansion effect
         */
        internal fun getPpxExpansionInfo(annotation: String): String {
            // Extract the base name (without arguments)
            val nameMatch = PPX_NAME_PATTERN.find(annotation)
            val name = nameMatch?.groupValues?.get(1) ?: return "Unknown annotation"

            return PPX_DESCRIPTIONS[name] ?: "Custom PPX annotation"
        }

        /** Static mapping of known PPX annotations to their expansion descriptions. */
        internal val PPX_DESCRIPTIONS =
            mapOf(
                "react.component" to
                    "Generates React.createElement wrapper with props record → individual labeled params",
                "genType" to "Generates .gen.tsx TypeScript binding file for cross-language interop",
                "module" to "Binds to an external JavaScript module import",
                "val" to "Binds to a JavaScript global value",
                "send" to "Binds to a JavaScript method call (obj.method())",
                "get" to "Binds to a JavaScript property getter (obj.prop)",
                "set" to "Binds to a JavaScript property setter (obj.prop = value)",
                "new" to "Binds to a JavaScript constructor (new Class())",
                "scope" to "Scopes bindings under a JavaScript namespace",
                "deriving" to "Generates serialization/accessor functions based on type definition",
                "unboxed" to "Uses unboxed representation (removes runtime wrapper)",
                "as" to "Renames the binding in generated JavaScript output",
                "string" to "Represents string enum values for polymorphic variants",
                "int" to "Represents integer enum values for polymorphic variants",
                "unwrap" to "Unwraps argument types for external function bindings",
                "return" to "Wraps return value (e.g., nullable → option) in external bindings",
                "obj" to "Creates a JavaScript object from a record",
                "variadic" to "Binds to a JavaScript variadic function",
                "inline" to "Inlines the binding value at compile time",
                "live" to "Marks binding as used (suppresses dead-code warnings)",
                "dead" to "Marks binding as intentionally dead code",
            )
    }
}
