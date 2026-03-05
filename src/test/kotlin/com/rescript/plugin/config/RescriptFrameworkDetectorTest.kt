package com.rescript.plugin.config

import com.intellij.json.JsonFileType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptFrameworkDetectorTest {
    private val detector = RescriptFrameworkDetector()

    @Test
    fun `detector can be instantiated`() {
        assertNotNull(detector)
    }

    @Test
    fun `detector is a FrameworkDetector`() {
        assertTrue(detector is com.intellij.framework.detection.FrameworkDetector)
    }

    @Test
    fun `fileType returns JsonFileType`() {
        assertEquals(JsonFileType.INSTANCE, detector.fileType)
    }

    @Test
    fun `frameworkType is RescriptFrameworkType`() {
        val frameworkType = detector.frameworkType
        assertNotNull(frameworkType)
        assertEquals("ReScript", frameworkType.presentableName)
    }

    @Test
    fun `suitableFilePattern is not null`() {
        assertNotNull(detector.createSuitableFilePattern())
    }

    @Test
    fun `framework type has correct id`() {
        assertEquals("rescript", RescriptFrameworkType.INSTANCE.id)
    }
}
