package com.rescript.plugin.hierarchy

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.lang.psi.RescriptElementTypes
import com.rescript.plugin.lang.psi.RescriptFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration tests for [RescriptModuleHierarchyBrowser].
 *
 * Constructs a browser over a real PSI fixture and verifies the routing
 * methods that decide which elements the browser accepts and which tree
 * structure each view type maps to. Browser methods are inherited as
 * protected from `HierarchyBrowserBaseEx` and accessed via reflection.
 *
 * @see RescriptModuleHierarchyTreeStructureTest for the tree structures themselves
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptModuleHierarchyBrowserTest {
    private lateinit var myFixture: CodeInsightTestFixture

    @Suppress("unused")
    private lateinit var project: Project

    @Test
    fun testIsApplicableElementAcceptsRescriptFile() {
        val file = myFixture.configureByText("Foo.res", "let x = 1") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        assertTrue(invokeIsApplicable(browser, file))
    }

    @Test
    fun testIsApplicableElementAcceptsModuleDeclaration() {
        val file = myFixture.configureByText("Foo.res", "module Bar = { let x = 1 }") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        val module = findFirst(file, RescriptElementTypes.MODULE_DECLARATION)!!
        assertTrue(invokeIsApplicable(browser, module))
    }

    @Test
    fun testIsApplicableElementRejectsLetDeclaration() {
        val file = myFixture.configureByText("Foo.res", "let x = 1") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!
        assertFalse(invokeIsApplicable(browser, letDecl))
    }

    @Test
    fun testCreateHierarchyTreeStructureForNesting() {
        val file = myFixture.configureByText("Foo.res", "module Bar = {}") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        val structure = invokeCreateStructure(browser, RescriptModuleHierarchyBrowser.MODULE_NESTING_TYPE, file)
        assertNotNull(structure)
        assertTrue(structure is RescriptModuleHierarchyTreeStructure, "got: ${structure?.javaClass}")
    }

    @Test
    fun testCreateHierarchyTreeStructureForDependency() {
        val file = myFixture.configureByText("Foo.res", "open Belt") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        val structure = invokeCreateStructure(browser, RescriptModuleHierarchyBrowser.MODULE_DEPENDENCIES_TYPE, file)
        assertNotNull(structure)
        assertTrue(structure is RescriptModuleDependencyTreeStructure, "got: ${structure?.javaClass}")
    }

    @Test
    fun testCreateHierarchyTreeStructureForUnknownTypeReturnsNull() {
        val file = myFixture.configureByText("Foo.res", "let x = 1") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        val structure = invokeCreateStructure(browser, "Bogus", file)
        assertNull(structure)
    }

    @Test
    fun testGetContentDisplayNameForFile() {
        val file = myFixture.configureByText("Hello.res", "let x = 1") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        val name = invokeContentDisplayName(browser, RescriptModuleHierarchyBrowser.MODULE_NESTING_TYPE, file)
        assertEquals("Hello.res", name)
    }

    @Test
    fun testGetContentDisplayNameForModule() {
        val file = myFixture.configureByText("Foo.res", "module Greeter = { let hello = 1 }") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        val module = findFirst(file, RescriptElementTypes.MODULE_DECLARATION)!!
        val name = invokeContentDisplayName(browser, RescriptModuleHierarchyBrowser.MODULE_NESTING_TYPE, module)
        assertEquals("Greeter", name)
    }

    @Test
    fun testActionPlaceConstant() {
        val file = myFixture.configureByText("Foo.res", "let x = 1") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        val place = invokeStringMethod(browser, "getActionPlace")
        assertEquals("RescriptModuleHierarchy", place)
    }

    @Test
    fun testPreviousAndNextOccurrenceNames() {
        val file = myFixture.configureByText("Foo.res", "let x = 1") as RescriptFile
        val browser = RescriptModuleHierarchyBrowser(file)
        assertEquals("Previous Module", invokeStringMethod(browser, "getPrevOccurenceActionNameImpl"))
        assertEquals("Next Module", invokeStringMethod(browser, "getNextOccurenceActionNameImpl"))
    }

    // ── reflection helpers ────────────────────────────────────────────

    private fun invokeIsApplicable(
        browser: RescriptModuleHierarchyBrowser,
        element: PsiElement,
    ): Boolean {
        val method = findMethod(browser.javaClass, "isApplicableElement", PsiElement::class.java)
        return method.invoke(browser, element) as Boolean
    }

    private fun invokeCreateStructure(
        browser: RescriptModuleHierarchyBrowser,
        typeName: String,
        element: PsiElement,
    ): com.intellij.ide.hierarchy.HierarchyTreeStructure? {
        val method =
            findMethod(browser.javaClass, "createHierarchyTreeStructure", String::class.java, PsiElement::class.java)
        return method.invoke(browser, typeName, element) as com.intellij.ide.hierarchy.HierarchyTreeStructure?
    }

    private fun invokeContentDisplayName(
        browser: RescriptModuleHierarchyBrowser,
        typeName: String,
        element: PsiElement,
    ): String? {
        val method = findMethod(browser.javaClass, "getContentDisplayName", String::class.java, PsiElement::class.java)
        return method.invoke(browser, typeName, element) as String?
    }

    private fun invokeStringMethod(
        browser: RescriptModuleHierarchyBrowser,
        name: String,
    ): String {
        val method = findMethod(browser.javaClass, name)
        return method.invoke(browser) as String
    }

    private fun findMethod(
        clazz: Class<*>,
        name: String,
        vararg paramTypes: Class<*>,
    ): java.lang.reflect.Method {
        val method = clazz.getDeclaredMethod(name, *paramTypes)
        method.isAccessible = true
        return method
    }

    private fun findFirst(
        scope: PsiElement,
        elementType: com.intellij.psi.tree.IElementType,
    ): PsiElement? {
        if (scope.node?.elementType == elementType) return scope
        for (child in scope.children) {
            val found = findFirst(child, elementType)
            if (found != null) return found
        }
        return null
    }
}
