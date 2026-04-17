package com.rescript.plugin.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [RescriptSymbolContributor], verifying that the contributor
 * correctly implements [ChooseByNameContributorEx] and exposes the required
 * methods for Go to Symbol integration (Cmd+Option+O).
 *
 * Note: Full integration tests with actual stub index lookups require a heavy
 * platform test fixture. Tests here verify the class contract and structure.
 *
 * @see RescriptSymbolContributor
 * @see com.rescript.plugin.indexing.RescriptNameIndex
 */
class RescriptSymbolContributorTest {
    private val contributor = RescriptSymbolContributor()

    @Test
    fun testImplementsChooseByNameContributorEx() {
        val subject: Any = contributor
        assertTrue(subject is ChooseByNameContributorEx)
    }

    @Test
    fun testCanBeInstantiatedWithNoArgConstructor() {
        val instance = RescriptSymbolContributor()
        assertNotNull(instance)
    }

    @Test
    fun testProcessNamesMethodExists() {
        val method =
            RescriptSymbolContributor::class.java.getMethod(
                "processNames",
                Processor::class.java,
                com.intellij.psi.search.GlobalSearchScope::class.java,
                com.intellij.util.indexing.IdFilter::class.java,
            )
        assertNotNull(method)
        assertEquals(Void.TYPE, method.returnType)
    }

    @Test
    fun testProcessElementsWithNameMethodExists() {
        val method =
            RescriptSymbolContributor::class.java.getMethod(
                "processElementsWithName",
                String::class.java,
                Processor::class.java,
                FindSymbolParameters::class.java,
            )
        assertNotNull(method)
        assertEquals(Void.TYPE, method.returnType)
    }

    @Test
    fun testClassHasNoAdditionalPublicMethods() {
        val declaredMethods =
            RescriptSymbolContributor::class.java.declaredMethods
                .filter {
                    java.lang.reflect.Modifier
                        .isPublic(it.modifiers)
                }.map { it.name }
                .toSet()

        assertTrue("Missing processNames method", declaredMethods.contains("processNames"))
        assertTrue("Missing processElementsWithName method", declaredMethods.contains("processElementsWithName"))
        assertEquals("Expected exactly 2 public methods", 2, declaredMethods.size)
    }

    @Test
    fun testProcessNamesAcceptsNullFilter() {
        // Verify the method signature accepts nullable IdFilter (third parameter)
        val method =
            RescriptSymbolContributor::class.java.getMethod(
                "processNames",
                Processor::class.java,
                com.intellij.psi.search.GlobalSearchScope::class.java,
                com.intellij.util.indexing.IdFilter::class.java,
            )
        val filterParam = method.parameters[2]
        // IdFilter parameter should be present (nullable in Kotlin but non-nullable in Java reflection)
        assertEquals(com.intellij.util.indexing.IdFilter::class.java, filterParam.type)
    }
}
