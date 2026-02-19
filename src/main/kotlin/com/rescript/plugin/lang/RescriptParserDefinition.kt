package com.rescript.plugin.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.psi.RescriptFile

/**
 * Parser definition that wires together the ReScript lexer, parser, and PSI element factory.
 *
 * Provides the IntelliJ Platform with the components needed to tokenize and parse
 * ReScript files into PSI trees. The actual parsing is lightweight — only top-level
 * declarations and JSX structures are recognized; deeper semantic analysis is
 * delegated to the LSP server.
 *
 * @see RescriptLexer
 * @see RescriptParser
 */
class RescriptParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(RescriptLanguage)
    }

    override fun createLexer(project: Project?): Lexer = RescriptLexer()

    override fun createParser(project: Project?): PsiParser = RescriptParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = RescriptTokenTypes.COMMENTS

    override fun getStringLiteralElements(): TokenSet = RescriptTokenTypes.STRINGS

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = RescriptFile(viewProvider)
}
