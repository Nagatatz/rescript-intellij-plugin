package com.rescript.plugin.lang

import com.intellij.lexer.FlexAdapter

class RescriptLexer : FlexAdapter(RescriptFlexLexer(null))
