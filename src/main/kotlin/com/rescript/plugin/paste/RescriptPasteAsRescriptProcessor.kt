package com.rescript.plugin.paste

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.rescript.plugin.lang.psi.RescriptFile
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

/**
 * Post-processes paste operations to convert JavaScript code to ReScript syntax.
 *
 * Detects JavaScript patterns in pasted text (const/let/var declarations,
 * function definitions, strict equality, etc.) and applies heuristic
 * transformations to produce valid ReScript code.
 *
 * Implements [CopyPastePostProcessor] for automatic paste-time conversion.
 *
 * @see RescriptPasteAsJsxProcessor for the HTML-to-JSX paste processor
 */
class RescriptPasteAsRescriptProcessor : CopyPastePostProcessor<TextBlockTransferableData>() {
    override fun collectTransferableData(
        file: PsiFile,
        editor: Editor,
        startOffsets: IntArray,
        endOffsets: IntArray,
    ): List<TextBlockTransferableData> = emptyList()

    override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> {
        if (!content.isDataFlavorSupported(DataFlavor.stringFlavor)) return emptyList()
        val text = content.getTransferData(DataFlavor.stringFlavor) as? String ?: return emptyList()
        if (!looksLikeJavaScript(text)) return emptyList()
        return listOf(JsTransferData(text))
    }

    override fun processTransferableData(
        project: Project,
        editor: Editor,
        bounds: RangeMarker,
        caretOffset: Int,
        indented: Ref<in Boolean>,
        values: MutableList<out TextBlockTransferableData>,
    ) {
        val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
        if (file !is RescriptFile) return

        val jsData = values.filterIsInstance<JsTransferData>().firstOrNull() ?: return
        val converted = convertJsToRescript(jsData.originalJs)
        if (converted == jsData.originalJs) return

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.replaceString(bounds.startOffset, bounds.endOffset, converted)
        }
    }

    companion object {
        // JavaScript keyword patterns at line start
        private val JS_KEYWORDS =
            listOf("const ", "var ", "function ", "import ", "export ", "class ", "async ", "await ")

        /**
         * Checks whether the pasted text appears to be JavaScript code.
         *
         * @param text the clipboard text
         * @return true if the text contains JavaScript-specific patterns
         */
        internal fun looksLikeJavaScript(text: String): Boolean {
            val trimmed = text.trim()
            val lines = trimmed.lines()
            val jsLineCount =
                lines.count { line ->
                    val t = line.trimStart()
                    JS_KEYWORDS.any { kw -> t.startsWith(kw) } ||
                        t.contains("===") ||
                        t.contains("!==") ||
                        t.contains("console.log(")
                }
            // Need at least 2 JS-like lines, or 1 if short
            return jsLineCount >= 2 || (jsLineCount >= 1 && lines.size <= 3)
        }

        /**
         * Converts JavaScript code to ReScript syntax using heuristic transformations.
         *
         * Handles the following transformations:
         * - `const x =` / `var x =` → `let x =`
         * - `function name(args) {` → `let name = (args) => {`
         * - `===` → `==`, `!==` → `!=`
         * - `null` / `undefined` → `None`
         * - `console.log(` → `Js.log(`
         * - `import`/`export` lines are commented out
         *
         * @param js the JavaScript source text
         * @return the converted ReScript text
         */
        internal fun convertJsToRescript(js: String): String {
            val lines = js.lines()
            return lines.joinToString("\n") { line -> convertLine(line) }
        }

        /**
         * Converts a single line of JavaScript to ReScript.
         *
         * @param line the JavaScript line
         * @return the converted line
         */
        internal fun convertLine(line: String): String {
            var result = line

            // Comment out import/export (not directly translatable)
            val trimmed = result.trimStart()
            if (trimmed.startsWith("import ") ||
                trimmed.startsWith("export default ") ||
                trimmed.startsWith("export {")
            ) {
                return "// $result"
            }
            // export const/let/function → just remove export prefix
            if (trimmed.startsWith("export const ") ||
                trimmed.startsWith("export let ") ||
                trimmed.startsWith("export function ")
            ) {
                result = result.replaceFirst("export ", "")
            }

            // const/var/let → let (ReScript uses let for all bindings)
            result = result.replace(Regex("""(?<=^|\s)const\s+"""), "let ")
            result = result.replace(Regex("""(?<=^|\s)var\s+"""), "let ")

            // function name(args) { → let name = (args) => {
            result =
                FUNCTION_PATTERN.replace(result) { match ->
                    val indent = match.groupValues[1]
                    val name = match.groupValues[2]
                    val args = match.groupValues[3]
                    "${indent}let $name = ($args) => {"
                }

            // async function → also convert
            result =
                ASYNC_FUNCTION_PATTERN.replace(result) { match ->
                    val indent = match.groupValues[1]
                    val name = match.groupValues[2]
                    val args = match.groupValues[3]
                    "${indent}let $name = async ($args) => {"
                }

            // Strict equality
            result = result.replace("===", "==")
            result = result.replace("!==", "!=")

            // console.log → Js.log
            result = result.replace("console.log(", "Js.log(")

            // null/undefined → None (only standalone tokens)
            result = result.replace(Regex("""\bnull\b"""), "None")
            result = result.replace(Regex("""\bundefined\b"""), "None")

            // Remove semicolons at end of line
            if (result.trimEnd().endsWith(";")) {
                result = result.trimEnd().dropLast(1)
            }

            return result
        }

        // Pattern for `function name(args) {`
        private val FUNCTION_PATTERN =
            Regex("""^(\s*)function\s+(\w+)\s*\(([^)]*)\)\s*\{""")

        // Pattern for `async function name(args) {`
        private val ASYNC_FUNCTION_PATTERN =
            Regex("""^(\s*)async\s+function\s+(\w+)\s*\(([^)]*)\)\s*\{""")
    }
}

/**
 * Transferable data wrapper for JavaScript content detected during paste.
 *
 * @property originalJs the original JavaScript text from the clipboard
 */
private class JsTransferData(
    val originalJs: String,
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
                JsTransferData::class.java,
                "ReScript JS to ReScript",
            )
    }
}
