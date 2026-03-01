package com.rescript.plugin.grazie

import com.intellij.grazie.text.TextContent
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptGrazieTextExtractorTest {
    @Test
    fun testExtractorCanBeInstantiated() {
        val extractor = RescriptGrazieTextExtractor()
        assertNotNull(extractor)
    }

    @Test
    fun testDomainForSingleComment() {
        assertEquals(
            TextContent.TextDomain.COMMENTS,
            RescriptGrazieTextExtractor.domainFor(RescriptTokenTypes.SINGLE_COMMENT),
        )
    }

    @Test
    fun testDomainForMultiComment() {
        assertEquals(
            TextContent.TextDomain.COMMENTS,
            RescriptGrazieTextExtractor.domainFor(RescriptTokenTypes.MULTI_COMMENT),
        )
    }

    @Test
    fun testDomainForStringValue() {
        assertEquals(
            TextContent.TextDomain.LITERALS,
            RescriptGrazieTextExtractor.domainFor(RescriptTokenTypes.STRING_VALUE),
        )
    }

    @Test
    fun testDomainForKeywordReturnsNull() {
        assertNull(RescriptGrazieTextExtractor.domainFor(RescriptTokenTypes.LET))
    }

    @Test
    fun testDomainForOperatorReturnsNull() {
        assertNull(RescriptGrazieTextExtractor.domainFor(RescriptTokenTypes.PLUS))
    }

    @Test
    fun testDomainForNullReturnsNull() {
        assertNull(RescriptGrazieTextExtractor.domainFor(null))
    }

    @Test
    fun testCommentTypesContainsBothCommentTokens() {
        assertEquals(2, RescriptGrazieTextExtractor.COMMENT_TYPES.size)
        assert(RescriptTokenTypes.SINGLE_COMMENT in RescriptGrazieTextExtractor.COMMENT_TYPES)
        assert(RescriptTokenTypes.MULTI_COMMENT in RescriptGrazieTextExtractor.COMMENT_TYPES)
    }

    @Test
    fun testStringTypesContainsStringValue() {
        assertEquals(1, RescriptGrazieTextExtractor.STRING_TYPES.size)
        assert(RescriptTokenTypes.STRING_VALUE in RescriptGrazieTextExtractor.STRING_TYPES)
    }
}
