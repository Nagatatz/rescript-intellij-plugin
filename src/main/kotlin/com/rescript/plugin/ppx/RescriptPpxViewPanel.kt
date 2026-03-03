package com.rescript.plugin.ppx

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

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
        JTextArea().apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, 13)
            lineWrap = true
            wrapStyleWord = true
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
                    if (!file.name.endsWith(".res") && !file.name.endsWith(".resi")) return

                    updatePpxInfo(document.text)
                }
            },
            parentDisposable,
        )
    }

    private fun updatePpxInfo(sourceText: String) {
        val annotations = findPpxAnnotations(sourceText)
        if (annotations.isEmpty()) {
            infoArea.text = "No PPX annotations found in this file."
            return
        }

        val info =
            buildString {
                for ((annotation, line) in annotations) {
                    val expansion = getPpxExpansionInfo(annotation)
                    appendLine("Line $line: $annotation")
                    appendLine("  → $expansion")
                    appendLine()
                }
            }
        infoArea.text = info
    }

    companion object {
        // Pattern matching @annotation or @annotation(...)
        private val ANNOTATION_PATTERN = Regex("""@(\w+(?:\.\w+)*)(?:\([^)]*\))?""")

        /** Pattern extracting the PPX name from an annotation string (e.g., `@react.component`). */
        private val PPX_NAME_PATTERN = Regex("""@(\w+(?:\.\w+)*)""")

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
