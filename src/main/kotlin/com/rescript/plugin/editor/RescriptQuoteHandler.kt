package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * Handles smart quote insertion and completion for ReScript string literals.
 *
 * Automatically inserts matching closing quotes for regular strings and
 * template string delimiters.
 */
class RescriptQuoteHandler :
    SimpleTokenSetQuoteHandler(
        RescriptTokenTypes.STRING_VALUE,
        RescriptTokenTypes.JS_STRING_OPEN,
        RescriptTokenTypes.JS_STRING_CLOSE,
    )
