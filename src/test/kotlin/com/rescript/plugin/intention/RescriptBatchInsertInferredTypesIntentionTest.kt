package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Smoke test for the intention metadata. The behavioural logic is covered by
 * [RescriptBatchAnnotationPlannerTest]; this test simply guards the
 * user-visible label and family name from accidental regressions.
 */
class RescriptBatchInsertInferredTypesIntentionTest {
    @Test
    fun `getText returns the expected label`() {
        val intention = RescriptBatchInsertInferredTypesIntention()
        assertEquals("Insert inferred type annotations", intention.text)
    }

    @Test
    fun `getFamilyName matches getText`() {
        val intention = RescriptBatchInsertInferredTypesIntention()
        assertEquals(intention.text, intention.familyName)
    }
}
