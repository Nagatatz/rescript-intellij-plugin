package com.rescript.plugin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RescriptEditorUtilsTest {
    @Test
    fun testReplaceInWriteActionIsAccessible() {
        // Verify the extension function exists and is importable
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "replaceInWriteAction"
            }
        assertNotNull(method, "replaceInWriteAction should be declared in RescriptEditorUtils")
    }

    @Test
    fun testInsertInWriteActionIsAccessible() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "insertInWriteAction"
            }
        assertNotNull(method, "insertInWriteAction should be declared in RescriptEditorUtils")
    }

    @Test
    fun testDeleteInWriteActionIsAccessible() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "deleteInWriteAction"
            }
        assertNotNull(method, "deleteInWriteAction should be declared in RescriptEditorUtils")
    }

    @Test
    fun testReplaceInWriteActionHasCorrectParameterCount() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "replaceInWriteAction"
            }
        assertNotNull(method)
        // Document (receiver) + Project + startOffset + endOffset + newText = 5 params
        assertEquals(5, method!!.parameterCount)
    }

    @Test
    fun testInsertInWriteActionHasCorrectParameterCount() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "insertInWriteAction"
            }
        assertNotNull(method)
        // Document (receiver) + Project + offset + text = 4 params
        assertEquals(4, method!!.parameterCount)
    }

    @Test
    fun testDeleteInWriteActionHasCorrectParameterCount() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "deleteInWriteAction"
            }
        assertNotNull(method)
        // Document (receiver) + Project + startOffset + endOffset = 4 params
        assertEquals(4, method!!.parameterCount)
    }

    @Test
    fun testGetLineTextAtIsAccessible() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "getLineTextAt"
            }
        assertNotNull(method, "getLineTextAt should be declared in RescriptEditorUtils")
    }

    @Test
    fun testGetLineRangeAtIsAccessible() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "getLineRangeAt"
            }
        assertNotNull(method, "getLineRangeAt should be declared in RescriptEditorUtils")
    }

    @Test
    fun testGetLineTextAtHasCorrectParameterCount() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "getLineTextAt"
            }
        assertNotNull(method)
        // Document (receiver) + offset = 2 params
        assertEquals(2, method!!.parameterCount)
    }

    @Test
    fun testGetLineRangeAtHasCorrectParameterCount() {
        val method =
            RescriptEditorUtils::class.java.declaredMethods.find {
                it.name == "getLineRangeAt"
            }
        assertNotNull(method)
        // Document (receiver) + offset = 2 params
        assertEquals(2, method!!.parameterCount)
    }
}
