package com.rescript.plugin.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Provides completion candidates for ReScript decorators (attributes).
 *
 * When the user types `@` in a ReScript file, this contributor offers standard
 * decorator names such as `@genType`, `@module`, `@val`, etc. Each candidate
 * includes a short description of the decorator's purpose.
 *
 * Decorators are only offered when the caret is positioned immediately after
 * an `@` token (ARROBASE) or inside an annotation name (ANNOTATION_NAME).
 *
 * @see RescriptCompletionConfidence for popup suppression logic
 */
class RescriptDecoratorCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(
        parameters: CompletionParameters,
        result: CompletionResultSet,
    ) {
        if (parameters.completionType != CompletionType.BASIC) return

        val position = parameters.position
        if (position.language != RescriptLanguage) return

        val tokenType = position.node?.elementType ?: return

        // Offer decorators after @ or while typing an annotation name
        if (tokenType != RescriptTokenTypes.ARROBASE && tokenType != RescriptTokenTypes.ANNOTATION_NAME) return

        // Compute prefix: for ANNOTATION_NAME, strip the leading '@'
        val prefix =
            if (tokenType == RescriptTokenTypes.ANNOTATION_NAME) {
                val text = position.text.removeSuffix("IntellijIdeaRulezzz")
                text.removePrefix("@")
            } else {
                ""
            }

        val filteredResult = result.withPrefixMatcher(prefix)

        for ((name, description) in DECORATORS) {
            val element =
                LookupElementBuilder
                    .create(name)
                    .withTypeText(description)
                    .withPresentableText("@$name")
                    .withInsertHandler { context, _ ->
                        // If triggered from ARROBASE, the '@' is already in the document
                        // Just insert the decorator name
                        val editor = context.editor
                        val doc = editor.document
                        val tailOffset = context.tailOffset

                        // For ARROBASE: replace from after '@'; for ANNOTATION_NAME: replace the whole token
                        if (tokenType == RescriptTokenTypes.ARROBASE) {
                            doc.insertString(tailOffset, name)
                            editor.caretModel.moveToOffset(tailOffset + name.length)
                        }
                    }
            filteredResult.addElement(element)
        }
    }

    companion object {
        /**
         * Standard ReScript decorators with their descriptions.
         * Each pair is (decoratorName, shortDescription).
         */
        val DECORATORS: List<Pair<String, String>> =
            listOf(
                "genType" to "Generate TypeScript types",
                "module" to "Bind to a JS module",
                "val" to "Bind to a JS value",
                "scope" to "Nested module scope binding",
                "send" to "Bind to a method call",
                "get" to "Bind to a property getter",
                "set" to "Bind to a property setter",
                "new" to "Bind to a constructor",
                "variadic" to "Variadic function binding",
                "return" to "Return type annotation",
                "string" to "String enum encoding",
                "int" to "Int enum encoding",
                "unwrap" to "Unwrap polymorphic variant",
                "as" to "Rename JS output",
                "inline" to "Inline constant",
                "live" to "Mark as used (suppress unused warning)",
                "dead" to "Mark as dead code",
                "deriving" to "Auto-derive functions",
                "react.component" to "React component annotation",
                "jsx.component" to "JSX component annotation",
                "deprecated" to "Mark as deprecated",
                "unboxed" to "Unboxed representation",
                "tag" to "Tag for variant encoding",
                "obj" to "Object creation helper",
            )
    }
}
