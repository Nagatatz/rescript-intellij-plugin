package com.rescript.plugin.run

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RescriptCommandTest {
    @Test
    fun `fromId build returns BUILD`() {
        assertEquals(RescriptCommand.BUILD, RescriptCommand.fromId("build"))
    }

    @Test
    fun `fromId build-watch returns BUILD_WATCH`() {
        assertEquals(RescriptCommand.BUILD_WATCH, RescriptCommand.fromId("build-watch"))
    }

    @Test
    fun `fromId clean returns CLEAN`() {
        assertEquals(RescriptCommand.CLEAN, RescriptCommand.fromId("clean"))
    }

    @Test
    fun `fromId unknown returns BUILD as default`() {
        assertEquals(RescriptCommand.BUILD, RescriptCommand.fromId("unknown"))
    }

    @Test
    fun `fromId empty returns BUILD as default`() {
        assertEquals(RescriptCommand.BUILD, RescriptCommand.fromId(""))
    }

    @Test
    fun `BUILD args contains build`() {
        assertEquals(listOf("build"), RescriptCommand.BUILD.args)
    }

    @Test
    fun `BUILD_WATCH args contains build and -w`() {
        assertEquals(listOf("build", "-w"), RescriptCommand.BUILD_WATCH.args)
    }

    @Test
    fun `CLEAN args contains clean`() {
        assertEquals(listOf("clean"), RescriptCommand.CLEAN.args)
    }

    @Test
    fun `entries count is 3`() {
        assertEquals(3, RescriptCommand.entries.size)
    }

    @Test
    fun `BUILD id is build`() {
        assertEquals("build", RescriptCommand.BUILD.id)
    }

    @Test
    fun `BUILD displayName is Build`() {
        assertEquals("Build", RescriptCommand.BUILD.displayName)
    }

    @Test
    fun `BUILD_WATCH displayName is Build Watch`() {
        assertEquals("Build (Watch)", RescriptCommand.BUILD_WATCH.displayName)
    }
}
