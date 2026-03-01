package com.rescript.plugin.indexing

import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RescriptIndexPatternBuilderTest {
    private val builder = RescriptIndexPatternBuilder()

    @Test
    fun testBuilderCanBeInstantiated() {
        assertNotNull(builder)
    }

    // -- getCommentStartDelta tests --

    @Test
    fun testCommentStartDeltaForSingleComment() {
        assertEquals(2, builder.getCommentStartDelta(RescriptTokenTypes.SINGLE_COMMENT))
    }

    @Test
    fun testCommentStartDeltaForMultiComment() {
        assertEquals(2, builder.getCommentStartDelta(RescriptTokenTypes.MULTI_COMMENT))
    }

    @Test
    fun testCommentStartDeltaForNonComment() {
        assertEquals(0, builder.getCommentStartDelta(RescriptTokenTypes.LIDENT))
    }

    @Test
    fun testCommentStartDeltaForNull() {
        assertEquals(0, builder.getCommentStartDelta(null))
    }

    // -- getCommentEndDelta tests --

    @Test
    fun testCommentEndDeltaForMultiComment() {
        assertEquals(2, builder.getCommentEndDelta(RescriptTokenTypes.MULTI_COMMENT))
    }

    @Test
    fun testCommentEndDeltaForSingleComment() {
        assertEquals(0, builder.getCommentEndDelta(RescriptTokenTypes.SINGLE_COMMENT))
    }

    @Test
    fun testCommentEndDeltaForNonComment() {
        assertEquals(0, builder.getCommentEndDelta(RescriptTokenTypes.LIDENT))
    }

    @Test
    fun testCommentEndDeltaForNull() {
        assertEquals(0, builder.getCommentEndDelta(null))
    }

    // -- constants --

    @Test
    fun testSingleCommentStartDeltaConstant() {
        assertEquals(2, RescriptIndexPatternBuilder.SINGLE_COMMENT_START_DELTA)
    }

    @Test
    fun testMultiCommentStartDeltaConstant() {
        assertEquals(2, RescriptIndexPatternBuilder.MULTI_COMMENT_START_DELTA)
    }

    @Test
    fun testMultiCommentEndDeltaConstant() {
        assertEquals(2, RescriptIndexPatternBuilder.MULTI_COMMENT_END_DELTA)
    }
}
