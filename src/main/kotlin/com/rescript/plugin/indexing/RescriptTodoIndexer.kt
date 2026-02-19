package com.rescript.plugin.indexing

import com.intellij.lexer.Lexer
import com.intellij.psi.impl.cache.impl.BaseFilterLexer
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer
import com.intellij.psi.search.UsageSearchContext
import com.rescript.plugin.lang.RescriptLexer

/**
 * Filter lexer that scans ReScript comment tokens for TODO/FIXME patterns
 * and feeds them to the IDE's TODO indexing system.
 */
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

/** Indexes TODO comments in ReScript files using the JFlex lexer. */
class RescriptTodoIndexer : LexerBasedTodoIndexer() {
    override fun createLexer(consumer: OccurrenceConsumer): Lexer = RescriptFilterLexer(RescriptLexer(), consumer)
}
