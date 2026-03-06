package com.rescript.plugin.completion

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Provides postfix completion templates for ReScript expressions.
 *
 * Supports the following templates triggered by typing `.` after an expression:
 * - `.switch` -> `switch expr { | _ => }`
 * - `.pipe` -> `expr->`
 * - `.log` -> `Console.log(expr)`
 * - `.some` -> `Some(expr)`
 * - `.ok` -> `Ok(expr)`
 * - `.error` -> `Error(expr)`
 * - `.ignore` -> `expr->ignore`
 * - `.promise` -> `expr->Promise.then(result => { ... })`
 * - `.await` -> `await expr`
 */
class RescriptPostfixTemplateProvider : PostfixTemplateProvider {
    private val templates: Set<PostfixTemplate> =
        TEMPLATE_DEFINITIONS
            .map { def ->
                RescriptSimplePostfixTemplate(def.name, def.example, def.expand, this)
            }.toSet()

    override fun getTemplates(): Set<PostfixTemplate> = templates

    override fun isTerminalSymbol(currentChar: Char): Boolean = currentChar == '.'

    override fun preExpand(
        file: PsiFile,
        editor: Editor,
    ) {}

    override fun afterExpand(
        file: PsiFile,
        editor: Editor,
    ) {}

    override fun preCheck(
        copyFile: PsiFile,
        realEditor: Editor,
        currentOffset: Int,
    ): PsiFile = copyFile

    companion object {
        private val NON_APPLICABLE_TOKENS =
            setOf(
                RescriptTokenTypes.SINGLE_COMMENT,
                RescriptTokenTypes.MULTI_COMMENT,
                RescriptTokenTypes.STRING_VALUE,
                RescriptTokenTypes.JS_STRING_OPEN,
                RescriptTokenTypes.JS_STRING_CLOSE,
            )

        fun isRescriptApplicable(context: PsiElement): Boolean {
            if (context.containingFile?.language != RescriptLanguage) return false
            val tokenType = context.node?.elementType ?: return false
            return tokenType !in NON_APPLICABLE_TOKENS
        }
    }
}

/**
 * Result of expanding a postfix template expression.
 *
 * @param text the replacement text
 * @param caretOffset the caret position relative to the replacement start
 */
data class ExpandResult(
    val text: String,
    val caretOffset: Int = text.length,
)

/**
 * Definition of a postfix template expansion.
 *
 * @param name the template trigger name (e.g., "switch")
 * @param example the example shown in the completion popup
 * @param expand function that transforms the original expression text into an [ExpandResult]
 */
data class TemplateDefinition(
    val name: String,
    val example: String,
    val expand: (expr: String) -> ExpandResult,
)

/** All postfix template definitions in a data-driven list. */
private val TEMPLATE_DEFINITIONS =
    listOf(
        TemplateDefinition("switch", "switch expr { | _ => }") { expr ->
            val text = "switch $expr { | _ => }"
            ExpandResult(text, text.length - 1)
        },
        TemplateDefinition("pipe", "expr->") { expr ->
            ExpandResult("$expr->")
        },
        TemplateDefinition("log", "Console.log(expr)") { expr ->
            ExpandResult("Console.log($expr)")
        },
        TemplateDefinition("some", "Some(expr)") { expr ->
            ExpandResult("Some($expr)")
        },
        TemplateDefinition("ok", "Ok(expr)") { expr ->
            ExpandResult("Ok($expr)")
        },
        TemplateDefinition("error", "Error(expr)") { expr ->
            ExpandResult("Error($expr)")
        },
        TemplateDefinition("ignore", "expr->ignore") { expr ->
            ExpandResult("$expr->ignore")
        },
        TemplateDefinition("promise", "expr->Promise.then(result => { ... })") { expr ->
            val text = "$expr->Promise.then(result => {\n  \n})"
            ExpandResult(text, expr.length + "->Promise.then(result => {\n  ".length)
        },
        TemplateDefinition("await", "await expr") { expr ->
            ExpandResult("await $expr")
        },
    )

/**
 * Data-driven postfix template that delegates expansion to a [TemplateDefinition].
 *
 * Replaces the expression before the dot with the result of [expand] and positions the caret.
 */
private class RescriptSimplePostfixTemplate(
    name: String,
    example: String,
    private val expand: (expr: String) -> ExpandResult,
    provider: PostfixTemplateProvider,
) : PostfixTemplate(null, name, ".$name", example, provider) {
    override fun isApplicable(
        context: PsiElement,
        copyDocument: Document,
        newOffset: Int,
    ): Boolean = RescriptPostfixTemplateProvider.isRescriptApplicable(context)

    override fun expand(
        context: PsiElement,
        editor: Editor,
    ) {
        val document = editor.document
        val endOffset = context.textRange.endOffset
        val dotOffset = findDotBefore(document, endOffset) ?: return
        val exprText = document.getText(TextRange(context.textRange.startOffset, dotOffset))
        val result = expand(exprText)
        document.replaceString(context.textRange.startOffset, endOffset, result.text)
        editor.caretModel.moveToOffset(context.textRange.startOffset + result.caretOffset)
    }
}

private fun findDotBefore(
    document: Document,
    offset: Int,
): Int? {
    val text = document.charsSequence
    var i = offset - 1
    // Skip past the template key characters to find the dot
    while (i >= 0 && text[i] != '.') {
        i--
    }
    return if (i >= 0) i else null
}
