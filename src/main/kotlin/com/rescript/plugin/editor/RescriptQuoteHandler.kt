package com.rescript.plugin.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.rescript.plugin.lang.RescriptTokenTypes

class RescriptQuoteHandler :
    SimpleTokenSetQuoteHandler(
        RescriptTokenTypes.STRING_VALUE,
        RescriptTokenTypes.JS_STRING_OPEN,
        RescriptTokenTypes.JS_STRING_CLOSE,
    )
