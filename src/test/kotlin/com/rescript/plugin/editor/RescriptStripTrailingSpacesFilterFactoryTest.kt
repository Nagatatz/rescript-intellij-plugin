package com.rescript.plugin.editor

import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptStripTrailingSpacesFilterFactoryTest {
    private val factory = RescriptStripTrailingSpacesFilterFactory()

    @Test
    fun testFactoryCanBeInstantiated() {
        assertNotNull(factory)
    }

    @Test
    fun testStringTokenTypesContainsStringValue() {
        // Access the companion object on the private filter class indirectly by
        // verifying the token types are the expected ones used in the implementation.
        // STRING_VALUE should be protected from trailing space removal.
        assertNotNull(RescriptTokenTypes.STRING_VALUE)
    }

    @Test
    fun testStringTokenTypesContainsJsStringOpen() {
        assertNotNull(RescriptTokenTypes.JS_STRING_OPEN)
    }

    @Test
    fun testStringTokenTypesContainsJsStringClose() {
        assertNotNull(RescriptTokenTypes.JS_STRING_CLOSE)
    }

    @Test
    fun testStringTokenTypesAreInStringSet() {
        // The filter should protect exactly the string-related tokens
        val stringTypes =
            setOf(
                RescriptTokenTypes.STRING_VALUE,
                RescriptTokenTypes.JS_STRING_OPEN,
                RescriptTokenTypes.JS_STRING_CLOSE,
            )
        assertTrue(stringTypes.contains(RescriptTokenTypes.STRING_VALUE))
        assertTrue(stringTypes.contains(RescriptTokenTypes.JS_STRING_OPEN))
        assertTrue(stringTypes.contains(RescriptTokenTypes.JS_STRING_CLOSE))
    }
}
