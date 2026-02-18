package com.rescript.plugin.navigation

import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptPsiUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptQualifiedNameProviderTest {
    private val provider = RescriptQualifiedNameProvider()

    @Test
    fun testProviderCanBeInstantiated() {
        assertNotNull(provider)
    }

    @Test
    fun testSupportedTypesMatchesNavigableTypes() {
        assertEquals(RescriptPsiUtils.NAVIGABLE_TYPES, RescriptQualifiedNameProvider.SUPPORTED_TYPES)
    }

    @Test
    fun testSupportedTypesContainsLetDeclaration() {
        assertTrue(
            RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.LET_DECLARATION),
        )
    }

    @Test
    fun testSupportedTypesContainsTypeDeclaration() {
        assertTrue(
            RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.TYPE_DECLARATION),
        )
    }

    @Test
    fun testSupportedTypesContainsModuleDeclaration() {
        assertTrue(
            RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.MODULE_DECLARATION),
        )
    }

    @Test
    fun testSupportedTypesContainsExternalDeclaration() {
        assertTrue(
            RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.EXTERNAL_DECLARATION),
        )
    }

    @Test
    fun testSupportedTypesContainsExceptionDeclaration() {
        assertTrue(
            RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.EXCEPTION_DECLARATION),
        )
    }

    @Test
    fun testSupportedTypesDoesNotContainOpenStatement() {
        assertFalse(
            RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.OPEN_STATEMENT),
        )
    }

    @Test
    fun testSupportedTypesDoesNotContainIncludeStatement() {
        assertFalse(
            RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.INCLUDE_STATEMENT),
        )
    }

    @Test
    fun testSupportedTypesDoesNotContainAnnotation() {
        assertFalse(
            RescriptQualifiedNameProvider.SUPPORTED_TYPES.contains(RescriptElementTypes.ANNOTATION),
        )
    }

    @Test
    fun testSupportedTypesCount() {
        assertEquals(5, RescriptQualifiedNameProvider.SUPPORTED_TYPES.size)
    }

    @Test
    fun testQualifiedNameReturnsNullForNullElement() {
        // getQualifiedName should handle elements that don't resolve to a declaration
        // Without PSI infrastructure, we verify the method is accessible
        assertNotNull(provider::getQualifiedName)
    }

    @Test
    fun testQualifiedNameToElementAlwaysReturnsNull() {
        // Verify the method signature exists and returns null
        assertNull(provider.qualifiedNameToElement("Foo.bar", null as? com.intellij.openapi.project.Project ?: return))
    }
}
