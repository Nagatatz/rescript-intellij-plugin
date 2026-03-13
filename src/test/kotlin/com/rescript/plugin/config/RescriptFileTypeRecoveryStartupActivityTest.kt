package com.rescript.plugin.config

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType

class RescriptFileTypeRecoveryStartupActivityTest : BasePlatformTestCase() {
    fun testActivityCanBeInstantiated() {
        val activity = RescriptFileTypeRecoveryStartupActivity()
        assertNotNull(activity)
    }

    fun testActivityImplementsProjectActivity() {
        val activity = RescriptFileTypeRecoveryStartupActivity()
        assertTrue(activity is ProjectActivity)
    }

    fun testResExtensionIsAssociatedWithRescriptFileTypeByDefault() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("res")
        assertEquals(RescriptFileType, fileType)
    }

    fun testResiExtensionIsAssociatedWithRescriptInterfaceFileTypeByDefault() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("resi")
        assertEquals(RescriptInterfaceFileType, fileType)
    }
}
