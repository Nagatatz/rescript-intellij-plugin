package com.rescript.plugin.intention

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptFilterMapChainIntentionTest {
    @Test
    fun `convertToFilterMap converts Array filter+map chain`() {
        val input = "  arr->Array.filter(isPositive)->Array.map(double)"
        val result = RescriptFilterMapChainIntention.convertToFilterMap(input)
        assertEquals(
            "  arr->Array.filterMap(x => isPositive(x) ? Some(double(x)) : None)",
            result,
        )
    }

    @Test
    fun `convertToFilterMap converts List filter+map chain`() {
        val input = "xs->List.filter(pred)->List.map(fn)"
        val result = RescriptFilterMapChainIntention.convertToFilterMap(input)
        assertEquals(
            "xs->List.filterMap(x => pred(x) ? Some(fn(x)) : None)",
            result,
        )
    }

    @Test
    fun `convertToFilterMap returns null for non-matching line`() {
        val result =
            RescriptFilterMapChainIntention.convertToFilterMap(
                "arr->Array.map(double)",
            )
        assertNull(result)
    }

    @Test
    fun `convertToFilterMap returns null for mismatched modules`() {
        val result =
            RescriptFilterMapChainIntention.convertToFilterMap(
                "arr->Array.filter(pred)->List.map(fn)",
            )
        assertNull(result)
    }

    @Test
    fun `FILTER_MAP_CHAIN_PATTERN matches Array chain`() {
        assertTrue(
            RescriptFilterMapChainIntention.FILTER_MAP_CHAIN_PATTERN.containsMatchIn(
                "->Array.filter(pred)->Array.map(fn)",
            ),
        )
    }

    @Test
    fun `FILTER_MAP_CHAIN_PATTERN matches List chain`() {
        assertTrue(
            RescriptFilterMapChainIntention.FILTER_MAP_CHAIN_PATTERN.containsMatchIn(
                "->List.filter(pred)->List.map(fn)",
            ),
        )
    }

    @Test
    fun `intention can be instantiated`() {
        assertNotNull(RescriptFilterMapChainIntention())
    }

    @Test
    fun `getText returns correct text`() {
        assertEquals("Convert filter+map to filterMap", RescriptFilterMapChainIntention().text)
    }

    @Test
    fun `getFamilyName returns correct name`() {
        assertEquals("Convert filter+map to filterMap", RescriptFilterMapChainIntention().familyName)
    }
}
