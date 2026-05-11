package com.rescript.plugin.navigation

import com.intellij.testFramework.LightVirtualFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Shape tests for [RescriptTypeSignatureSearchHit] — the row value the
 * Hoogle-style "ReScript Types" Search Everywhere tab renders and
 * navigates to.
 */
class RescriptTypeSignatureSearchHitTest {
    @Test
    fun `hit retains every property verbatim`() {
        val file = LightVirtualFile("Lib.res")
        val hit =
            RescriptTypeSignatureSearchHit(
                name = "parse",
                signatureDisplay = "string => result<int, string>",
                file = file,
                declarationOffset = 64,
                line = 12,
                relativePath = "src/Lib.res",
            )
        assertEquals("parse", hit.name)
        assertEquals("string => result<int, string>", hit.signatureDisplay)
        assertEquals(file, hit.file)
        assertEquals(64, hit.declarationOffset)
        assertEquals(12, hit.line)
        assertEquals("src/Lib.res", hit.relativePath)
    }

    @Test
    fun `hit equality is by all fields`() {
        val file = LightVirtualFile("Lib.res")
        val a = RescriptTypeSignatureSearchHit("parse", "string => int", file, 0, 1, "Lib.res")
        val b = RescriptTypeSignatureSearchHit("parse", "string => int", file, 0, 1, "Lib.res")
        val different = a.copy(line = 2)
        assertEquals(a, b)
        assertNotEquals(a, different)
    }
}
