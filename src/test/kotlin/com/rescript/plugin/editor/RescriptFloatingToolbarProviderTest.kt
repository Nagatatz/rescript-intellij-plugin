package com.rescript.plugin.editor

import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [RescriptFloatingToolbarProvider].
 *
 * The provider constructs its [com.intellij.openapi.actionSystem.ActionGroup]
 * via [com.intellij.openapi.actionSystem.ActionManager], which requires the
 * IntelliJ Platform application to be initialised — hence the use of
 * [IntelliJPlatformExtension].
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptFloatingToolbarProviderTest {
    @Test
    fun testProviderCanBeInstantiated() {
        assertNotNull(RescriptFloatingToolbarProvider())
    }

    @Test
    fun testActionGroupIsNotNull() {
        assertNotNull(RescriptFloatingToolbarProvider().actionGroup)
    }

    @Test
    fun testBuildActionGroupReturnsDefaultGroup() {
        assertNotNull(RescriptFloatingToolbarProvider.buildActionGroup())
    }
}
