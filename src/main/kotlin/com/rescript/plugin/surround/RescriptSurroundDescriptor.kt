package com.rescript.plugin.surround

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Provides "Surround With" templates for ReScript code (Ctrl+Alt+T).
 *
 * Available surrounders: `if (...)`, `switch ...`, `try ... catch`, and `{ }`.
 * Determines the elements to surround by finding the common PSI parent
 * of the selection range.
 */
class RescriptSurroundDescriptor : SurroundDescriptor {
    private val surrounders: Array<Surrounder> =
        arrayOf(
            RescriptIfSurrounder(),
            RescriptSwitchSurrounder(),
            RescriptTrySurrounder(),
            RescriptBlockSurrounder(),
        )

    override fun getElementsToSurround(
        file: PsiFile,
        startOffset: Int,
        endOffset: Int,
    ): Array<PsiElement> {
        if (file !is RescriptFile) return PsiElement.EMPTY_ARRAY
        if (startOffset >= endOffset) return PsiElement.EMPTY_ARRAY

        val startElement = file.findElementAt(startOffset) ?: return PsiElement.EMPTY_ARRAY
        val endElement = file.findElementAt(endOffset - 1) ?: return PsiElement.EMPTY_ARRAY
        val commonParent =
            PsiTreeUtil.findCommonParent(startElement, endElement) ?: return PsiElement.EMPTY_ARRAY

        return arrayOf(commonParent)
    }

    override fun getSurrounders(): Array<Surrounder> = surrounders

    override fun isExclusive(): Boolean = false
}

/**
 * Base class for ReScript surrounders that wraps selected text in a template
 * and positions the caret at a placeholder location within the generated code.
 */
abstract class RescriptBaseSurrounder(
    private val description: String,
) : Surrounder {
    override fun getTemplateDescription(): String = description

    override fun isApplicable(elements: Array<out PsiElement>): Boolean = elements.isNotEmpty()

    protected fun buildSurroundedText(selectedText: String): Pair<String, TextRange> {
        val text = generateTemplate(selectedText)
        val cursorRange = getCursorRange(text)
        return Pair(text, cursorRange)
    }

    abstract fun generateTemplate(selectedText: String): String

    abstract fun getCursorRange(generatedText: String): TextRange

    override fun surroundElements(
        project: Project,
        editor: Editor,
        elements: Array<out PsiElement>,
    ): TextRange? {
        if (elements.isEmpty()) return null

        val first = elements.first()
        val last = elements.last()
        val startOffset = first.textRange.startOffset
        val endOffset = last.textRange.endOffset
        val selectedText = editor.document.getText(TextRange(startOffset, endOffset))

        val (template, cursorRange) = buildSurroundedText(selectedText)

        editor.document.replaceString(startOffset, endOffset, template)

        return TextRange(startOffset + cursorRange.startOffset, startOffset + cursorRange.endOffset)
    }
}

/** Surrounds selected code with `if (condition) { ... }`. */
class RescriptIfSurrounder : RescriptBaseSurrounder("if (...)") {
    override fun generateTemplate(selectedText: String): String = "if (condition) {\n  $selectedText\n}"

    override fun getCursorRange(generatedText: String): TextRange {
        val start = generatedText.indexOf("condition")
        return TextRange(start, start + "condition".length)
    }
}

/** Surrounds selected code with `switch expr { | _ => ... }`. */
class RescriptSwitchSurrounder : RescriptBaseSurrounder("switch ...") {
    override fun generateTemplate(selectedText: String): String = "switch expr {\n| _ => $selectedText\n}"

    override fun getCursorRange(generatedText: String): TextRange {
        val start = generatedText.indexOf("expr")
        return TextRange(start, start + "expr".length)
    }
}

/** Surrounds selected code with `try { ... } catch { | exn => () }`. */
class RescriptTrySurrounder : RescriptBaseSurrounder("try ... catch") {
    override fun generateTemplate(selectedText: String): String = "try {\n  $selectedText\n} catch {\n| exn => ()\n}"

    override fun getCursorRange(generatedText: String): TextRange {
        val start = generatedText.lastIndexOf("()")
        return TextRange(start, start + "()".length)
    }
}

/** Surrounds selected code with `{ ... }`. */
class RescriptBlockSurrounder : RescriptBaseSurrounder("{ }") {
    override fun generateTemplate(selectedText: String): String = "{\n  $selectedText\n}"

    override fun getCursorRange(generatedText: String): TextRange {
        val pos = generatedText.lastIndexOf("}")
        return TextRange(pos, pos)
    }
}
