package com.rescript.plugin.completion

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes

class RescriptPostfixTemplateProvider : PostfixTemplateProvider {
    private val templates: Set<PostfixTemplate> =
        setOf(
            SwitchPostfixTemplate(this),
            PipePostfixTemplate(this),
            LogPostfixTemplate(this),
            SomePostfixTemplate(this),
            OkPostfixTemplate(this),
            ErrorPostfixTemplate(this),
            IgnorePostfixTemplate(this),
        )

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

private abstract class RescriptPostfixTemplateBase(
    name: String,
    example: String,
    provider: PostfixTemplateProvider,
) : PostfixTemplate(null, name, ".$name", example, provider) {
    override fun isApplicable(
        context: PsiElement,
        copyDocument: Document,
        newOffset: Int,
    ): Boolean = RescriptPostfixTemplateProvider.isRescriptApplicable(context)
}

private class SwitchPostfixTemplate(
    provider: PostfixTemplateProvider,
) : RescriptPostfixTemplateBase("switch", "switch expr { | _ => }", provider) {
    override fun expand(
        context: PsiElement,
        editor: Editor,
    ) {
        val document = editor.document
        val endOffset = context.textRange.endOffset
        val dotOffset = findDotBefore(document, endOffset) ?: return
        val exprText =
            document.getText(
                com.intellij.openapi.util
                    .TextRange(context.textRange.startOffset, dotOffset),
            )
        val replacement = "switch $exprText { | _ => }"
        document.replaceString(context.textRange.startOffset, endOffset, replacement)
        editor.caretModel.moveToOffset(context.textRange.startOffset + replacement.length - 1)
    }
}

private class PipePostfixTemplate(
    provider: PostfixTemplateProvider,
) : RescriptPostfixTemplateBase("pipe", "expr->", provider) {
    override fun expand(
        context: PsiElement,
        editor: Editor,
    ) {
        val document = editor.document
        val endOffset = context.textRange.endOffset
        val dotOffset = findDotBefore(document, endOffset) ?: return
        val exprText =
            document.getText(
                com.intellij.openapi.util
                    .TextRange(context.textRange.startOffset, dotOffset),
            )
        val replacement = "$exprText->"
        document.replaceString(context.textRange.startOffset, endOffset, replacement)
        editor.caretModel.moveToOffset(context.textRange.startOffset + replacement.length)
    }
}

private class LogPostfixTemplate(
    provider: PostfixTemplateProvider,
) : RescriptPostfixTemplateBase("log", "Console.log(expr)", provider) {
    override fun expand(
        context: PsiElement,
        editor: Editor,
    ) {
        val document = editor.document
        val endOffset = context.textRange.endOffset
        val dotOffset = findDotBefore(document, endOffset) ?: return
        val exprText =
            document.getText(
                com.intellij.openapi.util
                    .TextRange(context.textRange.startOffset, dotOffset),
            )
        val replacement = "Console.log($exprText)"
        document.replaceString(context.textRange.startOffset, endOffset, replacement)
        editor.caretModel.moveToOffset(context.textRange.startOffset + replacement.length)
    }
}

private class SomePostfixTemplate(
    provider: PostfixTemplateProvider,
) : RescriptPostfixTemplateBase("some", "Some(expr)", provider) {
    override fun expand(
        context: PsiElement,
        editor: Editor,
    ) {
        wrapExpression(context, editor, "Some")
    }
}

private class OkPostfixTemplate(
    provider: PostfixTemplateProvider,
) : RescriptPostfixTemplateBase("ok", "Ok(expr)", provider) {
    override fun expand(
        context: PsiElement,
        editor: Editor,
    ) {
        wrapExpression(context, editor, "Ok")
    }
}

private class ErrorPostfixTemplate(
    provider: PostfixTemplateProvider,
) : RescriptPostfixTemplateBase("error", "Error(expr)", provider) {
    override fun expand(
        context: PsiElement,
        editor: Editor,
    ) {
        wrapExpression(context, editor, "Error")
    }
}

private class IgnorePostfixTemplate(
    provider: PostfixTemplateProvider,
) : RescriptPostfixTemplateBase("ignore", "expr->ignore", provider) {
    override fun expand(
        context: PsiElement,
        editor: Editor,
    ) {
        val document = editor.document
        val endOffset = context.textRange.endOffset
        val dotOffset = findDotBefore(document, endOffset) ?: return
        val exprText =
            document.getText(
                com.intellij.openapi.util
                    .TextRange(context.textRange.startOffset, dotOffset),
            )
        val replacement = "$exprText->ignore"
        document.replaceString(context.textRange.startOffset, endOffset, replacement)
        editor.caretModel.moveToOffset(context.textRange.startOffset + replacement.length)
    }
}

private fun wrapExpression(
    context: PsiElement,
    editor: Editor,
    wrapper: String,
) {
    val document = editor.document
    val endOffset = context.textRange.endOffset
    val dotOffset = findDotBefore(document, endOffset) ?: return
    val exprText =
        document.getText(
            com.intellij.openapi.util
                .TextRange(context.textRange.startOffset, dotOffset),
        )
    val replacement = "$wrapper($exprText)"
    document.replaceString(context.textRange.startOffset, endOffset, replacement)
    editor.caretModel.moveToOffset(context.textRange.startOffset + replacement.length)
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
