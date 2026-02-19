package com.rescript.plugin.commenter

import com.intellij.lang.Commenter

/**
 * Defines comment syntax for ReScript: `//` for line comments and `/* */` for block comments.
 *
 * Enables Cmd+/ (line comment) and Cmd+Shift+/ (block comment) in the editor.
 */
class RescriptCommenter : Commenter {
    override fun getLineCommentPrefix(): String = "//"

    override fun getBlockCommentPrefix(): String = "/*"

    override fun getBlockCommentSuffix(): String = "*/"

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}
