package com.rescript.plugin.lang

import com.intellij.lexer.FlexAdapter

/**
 * Lexer adapter for ReScript source code.
 *
 * Wraps the JFlex-generated [RescriptFlexLexer] (from `Rescript.flex`) with IntelliJ's
 * [FlexAdapter] to provide the [com.intellij.lexer.Lexer] interface required by the
 * platform for syntax highlighting, parsing, and other token-based operations.
 */
class RescriptLexer : FlexAdapter(RescriptFlexLexer(null))
