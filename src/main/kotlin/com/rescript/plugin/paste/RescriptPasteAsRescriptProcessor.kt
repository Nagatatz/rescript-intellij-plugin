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
 * Post-processes paste operations to convert JavaScript/TypeScript code to ReScript syntax.
 *
 * Detects JavaScript and TypeScript patterns in pasted text (const/let/var declarations,
 * function definitions, strict equality, type annotations, etc.) and applies heuristic
 * transformations to produce valid ReScript code. Also handles JSX/TSX patterns such as
 * conditional rendering and array method calls.
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

        // TypeScript keyword patterns at line start
        private val TS_KEYWORDS =
            listOf("interface ", "enum ")

        // TypeScript type annotation patterns detected within lines
        private val TS_LINE_PATTERNS =
            listOf(
                // Basic type annotations: `: string`, `: number`, etc.
                Regex(""":\s*(string|number|boolean|any|void|never|unknown)\b"""),
                // React type patterns: `React.FC<`, `React.useState<`, etc.
                Regex("""React\.(FC|Component|useState|useEffect|useRef|useMemo|useCallback)\b"""),
                // Type assertion: `as string`, `as number`, etc.
                Regex("""\bas\s+(string|number|boolean|any)\b"""),
            )

        /**
         * Checks whether the pasted text appears to be JavaScript or TypeScript code.
         *
         * Detects JS patterns (const, function, ===, etc.) and TS patterns
         * (type annotations, interface, enum, React types).
         *
         * @param text the clipboard text
         * @return true if the text contains JavaScript or TypeScript patterns
         */
        internal fun looksLikeJavaScript(text: String): Boolean {
            val trimmed = text.trim()
            val lines = trimmed.lines()
            val jsLineCount =
                lines.count { line ->
                    val t = line.trimStart()
                    JS_KEYWORDS.any { kw -> t.startsWith(kw) } ||
                        TS_KEYWORDS.any { kw -> t.startsWith(kw) } ||
                        t.contains("===") ||
                        t.contains("!==") ||
                        t.contains("console.log(") ||
                        TS_LINE_PATTERNS.any { pat -> pat.containsMatchIn(t) }
                }
            // Need at least 2 JS-like lines, or 1 if short
            return jsLineCount >= 2 || (jsLineCount >= 1 && lines.size <= 3)
        }

        /**
         * Converts JavaScript/TypeScript code to ReScript syntax using heuristic transformations.
         *
         * Applies transformations in order: TypeScript type stripping first, then JS-to-ReScript
         * conversion, then JSX pattern conversion.
         *
         * @param js the JavaScript/TypeScript source text
         * @return the converted ReScript text
         */
        internal fun convertJsToRescript(js: String): String {
            val lines = js.lines()
            return lines.joinToString("\n") { line -> convertLine(line) }
        }

        /**
         * Converts a single line of JavaScript/TypeScript to ReScript.
         *
         * Applies transformations in three phases:
         * 1. TypeScript type annotation and declaration handling
         * 2. JavaScript-to-ReScript syntax conversion
         * 3. JSX/TSX pattern conversion
         *
         * @param line the JavaScript/TypeScript line
         * @return the converted line
         */
        internal fun convertLine(line: String): String {
            var result = line

            // --- Phase 0: Comment out / handle TS-specific and untranslatable declarations ---
            val trimmed = result.trimStart()

            // Comment out interface/enum declarations (not translatable to ReScript)
            if (trimmed.startsWith("interface ") ||
                trimmed.startsWith("enum ") ||
                trimmed.startsWith("export interface ") ||
                trimmed.startsWith("export enum ") ||
                trimmed.startsWith("export type ")
            ) {
                return "// $result"
            }

            // Comment out import/export (not directly translatable)
            if (trimmed.startsWith("import ") ||
                trimmed.startsWith("export default ") ||
                trimmed.startsWith("export {")
            ) {
                return "// $result"
            }

            // export const/let/function → just remove export prefix
            if (trimmed.startsWith("export const ") ||
                trimmed.startsWith("export let ") ||
                trimmed.startsWith("export function ") ||
                trimmed.startsWith("export async ")
            ) {
                result = result.replaceFirst("export ", "")
            }

            // --- Phase 1: Strip TypeScript type annotations ---
            // Remove type assertions: `value as Type` → `value`
            result = AS_ASSERTION_PATTERN.replace(result, "")

            // Remove variable type annotations: `const x: Type =` → `const x =`
            result =
                VAR_TYPE_ANNOTATION.replace(result) { match ->
                    "${match.groupValues[1]}${match.groupValues[2]}"
                }

            // Remove function return type annotations: `): Type {` → `) {`
            result =
                RETURN_TYPE_ANNOTATION.replace(result) { match ->
                    ")${match.groupValues[1]}"
                }

            // Remove parameter type annotations: `(a: Type, b: Type)` → `(a, b)`
            result = stripParamTypeAnnotations(result)

            // --- Phase 2: JavaScript-to-ReScript syntax conversion ---
            // const/var/let → let (ReScript uses let for all bindings)
            result = result.replace(CONST_PATTERN, "let ")
            result = result.replace(VAR_PATTERN, "let ")

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
            result = result.replace(NULL_PATTERN, "None")
            result = result.replace(UNDEFINED_PATTERN, "None")

            // --- Phase 3: JSX/TSX pattern conversion ---
            // `{condition && <expr>}` → `{condition ? <expr> : React.null}`
            result =
                JSX_AND_PATTERN.replace(result) { match ->
                    val condition = match.groupValues[1].trim()
                    val expr = match.groupValues[2].trim()
                    "{$condition ? $expr : React.null}"
                }

            // `.map(` → `->Array.map(`
            result = result.replace(DOT_MAP_PATTERN, "->Array.map(")
            // `.filter(` → `->Array.filter(`
            result = result.replace(DOT_FILTER_PATTERN, "->Array.filter(")
            // `.forEach(` → `->Array.forEach(`
            result = result.replace(DOT_FOREACH_PATTERN, "->Array.forEach(")

            // `{...props}` → `{...props} /* spread is not supported in ReScript JSX */`
            result =
                JSX_SPREAD_PATTERN.replace(result) { match ->
                    "${match.value} /* spread is not supported in ReScript JSX */"
                }

            // Remove semicolons at end of line
            if (result.trimEnd().endsWith(";")) {
                result = result.trimEnd().dropLast(1)
            }

            return result
        }

        /**
         * Strips type annotations from function parameters within parentheses.
         *
         * Handles patterns like `(a: number, b: string)` → `(a, b)` and
         * `(a: number, b?: string)` → `(a, b)`.
         *
         * @param line the source line
         * @return the line with parameter type annotations removed
         */
        internal fun stripParamTypeAnnotations(line: String): String =
            PARAM_TYPE_ANNOTATION.replace(line) { match ->
                match.groupValues[1]
            }

        // Pattern for `const` keyword at line start or after whitespace
        private val CONST_PATTERN = Regex("""(?<=^|\s)const\s+""")

        // Pattern for `var` keyword at line start or after whitespace
        private val VAR_PATTERN = Regex("""(?<=^|\s)var\s+""")

        // Pattern for standalone `null` token
        private val NULL_PATTERN = Regex("""\bnull\b""")

        // Pattern for standalone `undefined` token
        private val UNDEFINED_PATTERN = Regex("""\bundefined\b""")

        // Pattern for `function name(args) {`
        private val FUNCTION_PATTERN =
            Regex("""^(\s*)function\s+(\w+)\s*\(([^)]*)\)\s*\{""")

        // Pattern for `async function name(args) {`
        private val ASYNC_FUNCTION_PATTERN =
            Regex("""^(\s*)async\s+function\s+(\w+)\s*\(([^)]*)\)\s*\{""")

        // Variable type annotation: `const x: Type =` → captures name and `=`
        // Matches `: SomeType<Generic>` between identifier and `=`
        private val VAR_TYPE_ANNOTATION =
            Regex("""((?:const|let|var)\s+\w+)\s*:\s*[^=]+?(\s*=)""")

        // Function return type annotation: `): ReturnType {` or `): ReturnType =>`
        private val RETURN_TYPE_ANNOTATION =
            Regex("""\)\s*:\s*[\w<>\[\]|&.?,\s]+(\s*(?:\{|=>))""")

        // Parameter type annotation: `name: Type` or `name?: Type` inside parameter lists
        // Captures the parameter name in group 1
        private val PARAM_TYPE_ANNOTATION =
            Regex("""(\w+)\??\s*:\s*[\w<>\[\]|&.?]+""")

        // Type assertion: ` as Type` at word boundary
        private val AS_ASSERTION_PATTERN =
            Regex("""\s+as\s+[\w<>\[\]|&.?]+""")

        // JSX conditional rendering: `{condition && <expr>}`
        private val JSX_AND_PATTERN =
            Regex("""\{([^}]+?)\s*&&\s*([^}]+)\}""")

        // `.map(` array method call
        private val DOT_MAP_PATTERN = Regex("""\.map\(""")

        // `.filter(` array method call
        private val DOT_FILTER_PATTERN = Regex("""\.filter\(""")

        // `.forEach(` array method call
        private val DOT_FOREACH_PATTERN = Regex("""\.forEach\(""")

        // JSX spread: `{...identifier}`
        private val JSX_SPREAD_PATTERN = Regex("""\{\.\.\.\w+\}""")
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
