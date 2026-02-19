package com.rescript.plugin.run

import org.junit.Assert.assertEquals
import org.junit.Test

class RescriptCommandTest {
    // ── BUILD ──

    @Test
    fun `BUILD has correct id`() {
        assertEquals("build", RescriptCommand.BUILD.id)
    }

    @Test
    fun `BUILD has correct displayName`() {
        assertEquals("Build", RescriptCommand.BUILD.displayName)
    }

    @Test
    fun `BUILD has correct args`() {
        assertEquals(listOf("build"), RescriptCommand.BUILD.args)
    }

    // ── BUILD_WATCH ──

    @Test
    fun `BUILD_WATCH has correct id`() {
        assertEquals("build-watch", RescriptCommand.BUILD_WATCH.id)
    }

    @Test
    fun `BUILD_WATCH has correct displayName`() {
        assertEquals("Build (Watch)", RescriptCommand.BUILD_WATCH.displayName)
    }

    @Test
    fun `BUILD_WATCH has correct args`() {
        assertEquals(listOf("build", "-w"), RescriptCommand.BUILD_WATCH.args)
    }

    // ── CLEAN ──

    @Test
    fun `CLEAN has correct id`() {
        assertEquals("clean", RescriptCommand.CLEAN.id)
    }

    @Test
    fun `CLEAN has correct displayName`() {
        assertEquals("Clean", RescriptCommand.CLEAN.displayName)
    }

    @Test
    fun `CLEAN has correct args`() {
        assertEquals(listOf("clean"), RescriptCommand.CLEAN.args)
    }

    // ── fromId ──

    @Test
    fun `fromId returns BUILD for build`() {
        assertEquals(RescriptCommand.BUILD, RescriptCommand.fromId("build"))
    }

    @Test
    fun `fromId returns BUILD_WATCH for build-watch`() {
        assertEquals(RescriptCommand.BUILD_WATCH, RescriptCommand.fromId("build-watch"))
    }

    @Test
    fun `fromId returns CLEAN for clean`() {
        assertEquals(RescriptCommand.CLEAN, RescriptCommand.fromId("clean"))
    }

    @Test
    fun `fromId returns BUILD for unknown id`() {
        assertEquals(RescriptCommand.BUILD, RescriptCommand.fromId("unknown"))
    }

    @Test
    fun `fromId returns BUILD for empty string`() {
        assertEquals(RescriptCommand.BUILD, RescriptCommand.fromId(""))
    }

    // ── entries count ──

    @Test
    fun `there are exactly 3 commands`() {
        assertEquals(3, RescriptCommand.entries.size)
    }
}
