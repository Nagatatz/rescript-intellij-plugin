package com.rescript.plugin.indexing

import com.intellij.lexer.Lexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import com.intellij.psi.impl.cache.impl.id.IdAndTodoScannerBasedOnFilterLexer
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer
import com.rescript.plugin.lang.RescriptLexer

class RescriptTodoIndexer : LexerBasedTodoIndexer() {
    override fun createLexer(consumer: OccurrenceConsumer): Lexer =
        IdAndTodoScannerBasedOnFilterLexer(
            RescriptLexer(),
            consumer,
        )
}
