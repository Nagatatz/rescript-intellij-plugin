package com.rescript.plugin.indexing

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.intellij.util.io.VoidDataExternalizer
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import com.rescript.plugin.lang.RescriptLexer
import com.rescript.plugin.lang.RescriptTokenTypes

/**
 * File-based index that maps open statement module paths to the files that contain them.
 *
 * Scans ReScript files for `open ModuleName.SubModule` patterns using the JFlex lexer
 * and indexes the fully qualified module path as the key. This enables fast lookup of
 * "which files open this module?" via [FileBasedIndex].
 *
 * @see RescriptTodoIndexer for another lexer-based indexer example
 */
class RescriptOpenStatementIndex : FileBasedIndexExtension<String, Void>() {
    companion object {
        val NAME: ID<String, Void> = ID.create("rescript.open.statements")
    }

    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> =
        DataIndexer { inputData ->
            val result = mutableMapOf<String, Void?>()
            val lexer = RescriptLexer()
            lexer.start(inputData.contentAsText)

            while (lexer.tokenType != null) {
                if (lexer.tokenType == RescriptTokenTypes.OPEN) {
                    // Advance past whitespace to collect the module path
                    lexer.advance()
                    skipWhitespace(lexer)

                    val modulePath = collectModulePath(lexer)
                    if (modulePath.isNotEmpty()) {
                        result[modulePath] = null
                    }
                } else {
                    lexer.advance()
                }
            }
            result
        }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor()

    override fun getValueExternalizer(): VoidDataExternalizer = VoidDataExternalizer()

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter =
        DefaultFileTypeSpecificInputFilter(RescriptFileType, RescriptInterfaceFileType)

    override fun dependsOnFileContent(): Boolean = true
}

/**
 * Collects a dot-separated module path from consecutive UIDENT.DOT.UIDENT tokens.
 *
 * @param lexer the lexer positioned at the first UIDENT token after `open`
 * @return the full module path (e.g., "Belt.Array"), or empty string if no UIDENT found
 */
private fun collectModulePath(lexer: RescriptLexer): String {
    val parts = mutableListOf<String>()

    // Expect UIDENT
    if (lexer.tokenType == RescriptTokenTypes.UIDENT) {
        parts.add(lexer.tokenText)
        lexer.advance()
    }

    // Collect .UIDENT chain
    while (lexer.tokenType == RescriptTokenTypes.DOT) {
        lexer.advance()
        if (lexer.tokenType == RescriptTokenTypes.UIDENT) {
            parts.add(lexer.tokenText)
            lexer.advance()
        } else {
            break
        }
    }

    return parts.joinToString(".")
}

/** Advances the lexer past any whitespace/EOL tokens. */
private fun skipWhitespace(lexer: RescriptLexer) {
    while (lexer.tokenType == RescriptTokenTypes.EOL ||
        lexer.tokenType == com.intellij.psi.TokenType.WHITE_SPACE
    ) {
        lexer.advance()
    }
}
