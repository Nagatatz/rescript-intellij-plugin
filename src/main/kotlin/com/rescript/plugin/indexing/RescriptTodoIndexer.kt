package com.rescript.plugin.indexing

import com.intellij.lexer.Lexer
import com.intellij.psi.impl.cache.impl.BaseFilterLexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer
import com.intellij.psi.search.UsageSearchContext
import com.rescript.plugin.lang.RescriptLexer

private class RescriptFilterLexer(
    originalLexer: Lexer,
    occurrenceConsumer: OccurrenceConsumer,
) : BaseFilterLexer(originalLexer, occurrenceConsumer) {
    override fun advance() {
        scanWordsInToken(UsageSearchContext.IN_COMMENTS.toInt(), false, false)
        advanceTodoItemCountsInToken()
        myDelegate.advance()
    }
}

class RescriptTodoIndexer : LexerBasedTodoIndexer() {
    override fun createLexer(consumer: OccurrenceConsumer): Lexer = RescriptFilterLexer(RescriptLexer(), consumer)
}
