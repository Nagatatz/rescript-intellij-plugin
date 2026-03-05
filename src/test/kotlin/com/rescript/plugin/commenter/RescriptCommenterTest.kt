package com.rescript.plugin.commenter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RescriptCommenterTest {
    private val commenter = RescriptCommenter()

    @Test
    fun testLineCommentPrefix() {
        assertEquals("//", commenter.lineCommentPrefix)
    }

    @Test
    fun testBlockCommentPrefix() {
        assertEquals("/*", commenter.blockCommentPrefix)
    }

    @Test
    fun testBlockCommentSuffix() {
        assertEquals("*/", commenter.blockCommentSuffix)
    }

    @Test
    fun testCommentedBlockCommentPrefixIsNull() {
        assertNull(commenter.commentedBlockCommentPrefix)
    }

    @Test
    fun testCommentedBlockCommentSuffixIsNull() {
        assertNull(commenter.commentedBlockCommentSuffix)
    }
}
