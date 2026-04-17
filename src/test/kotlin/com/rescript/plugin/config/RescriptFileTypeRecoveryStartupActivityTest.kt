package com.rescript.plugin.config

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.RescriptFileType
import com.rescript.plugin.RescriptInterfaceFileType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(IntelliJPlatformExtension::class)
class RescriptFileTypeRecoveryStartupActivityTest {
    @Suppress("unused")
    private lateinit var myFixture: CodeInsightTestFixture

    @Test
    fun testActivityCanBeInstantiated() {
        val activity = RescriptFileTypeRecoveryStartupActivity()
        assertNotNull(activity)
    }

    @Test
    fun testActivityImplementsProjectActivity() {
        val activity: Any = RescriptFileTypeRecoveryStartupActivity()
        assertTrue(activity is ProjectActivity)
    }

    @Test
    fun testResExtensionIsAssociatedWithRescriptFileTypeByDefault() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("res")
        assertEquals(RescriptFileType, fileType)
    }

    @Test
    fun testResiExtensionIsAssociatedWithRescriptInterfaceFileTypeByDefault() {
        val fileType = FileTypeManager.getInstance().getFileTypeByExtension("resi")
        assertEquals(RescriptInterfaceFileType, fileType)
    }
}
