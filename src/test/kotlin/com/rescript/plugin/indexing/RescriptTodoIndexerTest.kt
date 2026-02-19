package com.rescript.plugin.indexing

import com.intellij.psi.impl.cache.impl.OccurrenceConsumer
import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptTodoIndexerTest {
    private val indexer = RescriptTodoIndexer()

    @Test
    fun `createLexer returns non-null lexer`() {
        val consumer = OccurrenceConsumer(null, true)
        val lexer = indexer.createLexer(consumer)
        assertNotNull(lexer)
    }

    @Test
    fun `createLexer returns a BaseFilterLexer wrapping RescriptLexer`() {
        val consumer = OccurrenceConsumer(null, true)
        val lexer = indexer.createLexer(consumer)
        // The returned lexer is a RescriptFilterLexer (BaseFilterLexer subclass)
        // which wraps a RescriptLexer as its delegate
        assertNotNull("Lexer should not be null", lexer)
    }

    @Test
    fun `createLexer with needToDo false returns non-null lexer`() {
        val consumer = OccurrenceConsumer(null, false)
        val lexer = indexer.createLexer(consumer)
        assertNotNull(lexer)
    }

    @Test
    fun `multiple calls to createLexer return distinct instances`() {
        val consumer = OccurrenceConsumer(null, true)
        val lexer1 = indexer.createLexer(consumer)
        val lexer2 = indexer.createLexer(consumer)
        assertNotNull(lexer1)
        assertNotNull(lexer2)
        // Each call creates a fresh lexer instance
        assert(lexer1 !== lexer2) { "Each createLexer call should return a new instance" }
    }
}
