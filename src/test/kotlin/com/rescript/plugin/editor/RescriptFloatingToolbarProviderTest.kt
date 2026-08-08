package com.rescript.plugin.editor

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.psi.PsiFile
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.RescriptLanguage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

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

    // -- isApplicableAsync tests --
    //
    // 2026.2 deprecated isApplicable(DataContext), whose default implementation returns
    // true. These cover the async replacement so the ReScript-only filter cannot regress
    // into showing the toolbar for every file type.

    @Test
    fun testIsApplicableAsyncRejectsContextWithoutPsiFile() =
        runBlocking {
            assertFalse(RescriptFloatingToolbarProvider().isApplicableAsync(SimpleDataContext.EMPTY_CONTEXT))
        }

    @Test
    fun testIsApplicableAsyncAcceptsRescriptFile() =
        runBlocking {
            val psiFile = mock(PsiFile::class.java)
            `when`(psiFile.language).thenReturn(RescriptLanguage)
            // The platform validates PSI passed through a DataContext and drops invalid
            // elements, so an unstubbed isValid() would make getData() return null.
            `when`(psiFile.isValid).thenReturn(true)
            val context = SimpleDataContext.getSimpleContext(CommonDataKeys.PSI_FILE, psiFile)
            assertTrue(RescriptFloatingToolbarProvider().isApplicableAsync(context))
        }
}
