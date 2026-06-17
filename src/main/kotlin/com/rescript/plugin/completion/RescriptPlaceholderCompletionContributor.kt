package com.rescript.plugin.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.TypeShape

/**
 * Offers record and variant literal placeholders at type-annotated value
 * positions such as `let x: person = ` or `let r: result<int> = `.
 *
 * The flow is purely syntactic and LSP-independent:
 * 1. [RescriptTypeAnnotationContext] detects the expected head type name
 *    from the text before the caret.
 * 2. [RescriptPlaceholderTypeResolver] resolves that name to a [TypeShape]
 *    via built-ins or the stub index.
 * 3. [RescriptPlaceholderBuilder] renders the shape into insertable text,
 *    which is added as a lookup element.
 *
 * Implements the IntelliJ Platform [CompletionContributor] extension point.
 *
 * @see RescriptDecoratorCompletionContributor for a sibling contributor
 */
class RescriptPlaceholderCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(
        parameters: CompletionParameters,
        result: CompletionResultSet,
    ) {
        if (parameters.completionType != CompletionType.BASIC) return

        val position = parameters.position
        if (position.language != RescriptLanguage) return

        val caretOffset = parameters.offset
        val document = parameters.editor.document
        if (caretOffset > document.textLength) return
        val textBeforeCaret = document.getText(TextRange(0, caretOffset))

        val detection = RescriptTypeAnnotationContext.detectExpectedType(textBeforeCaret) ?: return
        val shape = RescriptPlaceholderTypeResolver.resolve(position.project, detection.typeName) ?: return

        when (shape) {
            is TypeShape.Record -> addRecordPlaceholder(result, shape, detection.afterOpenBrace)
            is TypeShape.Variant -> addVariantPlaceholders(result, shape, detection.afterOpenBrace)
            TypeShape.Unknown -> Unit
        }
    }

    /**
     * Adds a single record-literal placeholder. When the user has already
     * typed an opening brace, the wrapping braces are stripped so the
     * inserted text only fills in the fields.
     */
    private fun addRecordPlaceholder(
        result: CompletionResultSet,
        shape: TypeShape.Record,
        afterOpenBrace: Boolean,
    ) {
        val full = RescriptPlaceholderBuilder.buildRecordPlaceholder(shape.fields) ?: return
        val insertText = if (afterOpenBrace) full.removePrefix("{ ").removeSuffix(" }") else full
        result.addElement(placeholderElement(insertText, "record"))
    }

    /**
     * Adds one lookup element per variant constructor. Constructors are not
     * offered after an opening brace, since `{` introduces a record literal.
     */
    private fun addVariantPlaceholders(
        result: CompletionResultSet,
        shape: TypeShape.Variant,
        afterOpenBrace: Boolean,
    ) {
        if (afterOpenBrace) return
        for (placeholder in RescriptPlaceholderBuilder.buildVariantPlaceholders(shape.constructors)) {
            result.addElement(placeholderElement(placeholder.insertText, "variant", placeholder.lookupName))
        }
    }

    /**
     * Builds a lookup element whose insertion replaces the prefix with
     * [insertText] and parks the caret on the first `_` placeholder hole.
     *
     * @param insertText the literal text to insert
     * @param typeText short kind label shown on the right of the popup
     * @param lookupString the string used for prefix matching (defaults to
     *   [insertText]); variants match on the bare constructor name
     */
    private fun placeholderElement(
        insertText: String,
        typeText: String,
        lookupString: String = insertText,
    ): LookupElementBuilder =
        LookupElementBuilder
            .create(lookupString)
            .withPresentableText(insertText)
            .withTypeText(typeText)
            .withInsertHandler { context, _ -> applyInsertion(context, insertText) }

    /** Replaces the completed prefix with [insertText] and positions the caret. */
    private fun applyInsertion(
        context: InsertionContext,
        insertText: String,
    ) {
        val document = context.document
        document.replaceString(context.startOffset, context.tailOffset, insertText)
        // Park the caret on the first `_` hole so the user can fill it in.
        val holeIndex = insertText.indexOf('_')
        val caret = if (holeIndex >= 0) context.startOffset + holeIndex else context.startOffset + insertText.length
        context.editor.caretModel.moveToOffset(caret)
    }
}
