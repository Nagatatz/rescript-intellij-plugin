package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [RescriptExpandOpenQualifierIntention].
 *
 * Note: Full intention integration (availability against a real PSI tree and the
 * `invoke()` document rewrite) requires IntelliJ Platform test fixtures; the rewrite
 * logic itself is covered by [com.rescript.plugin.imports.RescriptOpenExpansionPlannerTest]
 * and [com.rescript.plugin.imports.RescriptModuleMemberExtractorTest].
 */
class RescriptExpandOpenQualifierIntentionTest {
    @Test
    fun `intention text and family name are correct`() {
        val intention = RescriptExpandOpenQualifierIntention()
        assertEquals("Expand open into qualified references", intention.text)
        assertEquals("Expand open into qualified references", intention.familyName)
    }
}
