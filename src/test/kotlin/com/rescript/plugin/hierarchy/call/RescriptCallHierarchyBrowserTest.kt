package com.rescript.plugin.hierarchy.call

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
 * Integration tests for [RescriptCallHierarchyBrowser].
 *
 * Constructs a browser over a real PSI fixture and verifies:
 * - Applicability gating accepts let / external bindings and rejects
 *   non-binding declarations like type / module.
 * - createHierarchyTreeStructure routes typeName to the matching
 *   caller / callee tree structure (and returns null for unknown types).
 * - getContentDisplayName returns the binding name extracted from PSI.
 * - Action place and prev/next occurrence labels are constant.
 *
 * Browser methods are inherited as protected from `HierarchyBrowserBaseEx`
 * and accessed via reflection.
 *
 * @see RescriptCalleeTreeStructureTest
 * @see RescriptCallerTreeStructureTest
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptCallHierarchyBrowserTest {
    private lateinit var myFixture: CodeInsightTestFixture

    @Suppress("unused")
    private lateinit var project: Project

    @Test
    fun testIsApplicableElementAcceptsLetDeclaration() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!
        val browser = RescriptCallHierarchyBrowser(letDecl)
        assertTrue(invokeIsApplicable(browser, letDecl))
    }

    @Test
    fun testIsApplicableElementAcceptsExternalDeclaration() {
        val file =
            myFixture.configureByText(
                "Foo.res",
                """external log: string => unit = "console.log"""",
            ) as RescriptFile
        val ext = findFirst(file, RescriptElementTypes.EXTERNAL_DECLARATION)!!
        val browser = RescriptCallHierarchyBrowser(ext)
        assertTrue(invokeIsApplicable(browser, ext))
    }

    @Test
    fun testIsApplicableElementRejectsTypeDeclaration() {
        val file = myFixture.configureByText("Foo.res", "type t = int") as RescriptFile
        val type = findFirst(file, RescriptElementTypes.TYPE_DECLARATION)!!
        // Need an applicable PsiElement for the constructor; reuse the type itself — applicability is checked separately.
        val browser = RescriptCallHierarchyBrowser(type)
        assertFalse(invokeIsApplicable(browser, type))
    }

    @Test
    fun testCreateHierarchyTreeStructureForCallers() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!
        val browser = RescriptCallHierarchyBrowser(letDecl)
        val structure = invokeCreateStructure(browser, RescriptCallHierarchyBrowser.CALLERS_TYPE, letDecl)
        assertNotNull(structure)
        assertTrue(structure is RescriptCallerTreeStructure, "got: ${structure?.javaClass}")
    }

    @Test
    fun testCreateHierarchyTreeStructureForCallees() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!
        val browser = RescriptCallHierarchyBrowser(letDecl)
        val structure = invokeCreateStructure(browser, RescriptCallHierarchyBrowser.CALLEES_TYPE, letDecl)
        assertNotNull(structure)
        assertTrue(structure is RescriptCalleeTreeStructure, "got: ${structure?.javaClass}")
    }

    @Test
    fun testCreateHierarchyTreeStructureForUnknownTypeReturnsNull() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!
        val browser = RescriptCallHierarchyBrowser(letDecl)
        val structure = invokeCreateStructure(browser, "Bogus", letDecl)
        assertNull(structure)
    }

    @Test
    fun testGetContentDisplayNameReturnsExtractedName() {
        val file = myFixture.configureByText("Foo.res", "let myFunc = 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!
        val browser = RescriptCallHierarchyBrowser(letDecl)
        val name = invokeContentDisplayName(browser, RescriptCallHierarchyBrowser.CALLERS_TYPE, letDecl)
        assertEquals("myFunc", name)
    }

    @Test
    fun testActionPlaceConstant() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!
        val browser = RescriptCallHierarchyBrowser(letDecl)
        assertEquals("RescriptCallHierarchy", invokeStringMethod(browser, "getActionPlace"))
    }

    @Test
    fun testPreviousAndNextOccurrenceNames() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1") as RescriptFile
        val letDecl = findFirst(file, RescriptElementTypes.LET_DECLARATION)!!
        val browser = RescriptCallHierarchyBrowser(letDecl)
        assertEquals("Previous Function", invokeStringMethod(browser, "getPrevOccurenceActionNameImpl"))
        assertEquals("Next Function", invokeStringMethod(browser, "getNextOccurenceActionNameImpl"))
    }

    // ── reflection helpers ────────────────────────────────────────────

    private fun invokeIsApplicable(
        browser: RescriptCallHierarchyBrowser,
        element: PsiElement,
    ): Boolean {
        val method = findMethod(browser.javaClass, "isApplicableElement", PsiElement::class.java)
        return method.invoke(browser, element) as Boolean
    }

    private fun invokeCreateStructure(
        browser: RescriptCallHierarchyBrowser,
        typeName: String,
        element: PsiElement,
    ): com.intellij.ide.hierarchy.HierarchyTreeStructure? {
        val method =
            findMethod(browser.javaClass, "createHierarchyTreeStructure", String::class.java, PsiElement::class.java)
        return method.invoke(browser, typeName, element) as com.intellij.ide.hierarchy.HierarchyTreeStructure?
    }

    private fun invokeContentDisplayName(
        browser: RescriptCallHierarchyBrowser,
        typeName: String,
        element: PsiElement,
    ): String? {
        val method = findMethod(browser.javaClass, "getContentDisplayName", String::class.java, PsiElement::class.java)
        return method.invoke(browser, typeName, element) as String?
    }

    private fun invokeStringMethod(
        browser: RescriptCallHierarchyBrowser,
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
