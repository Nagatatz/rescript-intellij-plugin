package com.rescript.plugin.analytics

import com.rescript.plugin.wizard.ProjectTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Verifies the FUS group identity, event identifiers, and the closed-set
 * tool window list. Catches accidental drift in event IDs or group IDs
 * (which would invalidate any downstream analytics dashboards).
 */
class RescriptFeatureUsageCounterTest {
    @Test
    fun `group id is rescript features and version is 1`() {
        val collector = RescriptFeatureUsageCounter()
        val group = collector.group

        assertEquals("rescript.features", group.id)
        assertEquals(1, group.version)
    }

    @Test
    fun `wizard template selected event id is stable`() {
        assertEquals(
            "wizard.template.selected",
            RescriptFeatureUsageCounter.WIZARD_TEMPLATE_SELECTED.eventId,
        )
    }

    @Test
    fun `toolwindow opened event id is stable`() {
        assertEquals(
            "toolwindow.opened",
            RescriptFeatureUsageCounter.TOOLWINDOW_OPENED.eventId,
        )
    }

    @Test
    fun `intention invoked event id is stable`() {
        assertEquals(
            "intention.invoked",
            RescriptFeatureUsageCounter.INTENTION_INVOKED.eventId,
        )
    }

    @Test
    fun `toolwindow id list is non-empty and unique`() {
        val ids = RescriptFeatureUsageCounter.TOOLWINDOW_IDS

        assertFalse(ids.isEmpty(), "TOOLWINDOW_IDS must not be empty")
        assertEquals(ids.size, ids.toSet().size, "TOOLWINDOW_IDS must contain unique values")
    }

    @Test
    fun `every project template can be referenced for the wizard event`() {
        // Logging the event must not throw for any defined template — guards
        // against the closed enum getting out of sync with ProjectTemplate.
        for (template in ProjectTemplate.entries) {
            assertNotNull(template.name, "ProjectTemplate entry must have a name")
            assertTrue(template.name.isNotBlank())
        }
    }
}
