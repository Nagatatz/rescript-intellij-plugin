package com.rescript.plugin.lang.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.StubBuilderType
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import com.rescript.plugin.indexing.RescriptNameIndex
import com.rescript.plugin.lang.RescriptParserDefinition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration tests for [RescriptDeclarationPsiElement] using parsed PSI.
 *
 * Verifies that declarations created by the parser expose the declared name
 * via [RescriptDeclarationPsiElement.getDeclarationName] (falling back to AST
 * extraction when no stub is present) and that [toString] embeds the element
 * type for debug display.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptDeclarationPsiElementTest {
    private lateinit var myFixture: CodeInsightTestFixture

    @Suppress("unused")
    private lateinit var project: Project

    @Test
    fun testGetDeclarationNameForLet() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1")
        val decl = findDeclaration(file, RescriptElementTypes.LET_DECLARATION)
        assertNotNull(decl)
        assertEquals("foo", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForType() {
        val file = myFixture.configureByText("Foo.res", "type myType = int")
        val decl = findDeclaration(file, RescriptElementTypes.TYPE_DECLARATION)
        assertNotNull(decl)
        assertEquals("myType", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForModule() {
        val file = myFixture.configureByText("Foo.res", "module Bar = { let x = 1 }")
        val decl = findDeclaration(file, RescriptElementTypes.MODULE_DECLARATION)
        assertNotNull(decl)
        assertEquals("Bar", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForExternal() {
        val file = myFixture.configureByText("Foo.res", """external log: string => unit = "console.log"""")
        val decl = findDeclaration(file, RescriptElementTypes.EXTERNAL_DECLARATION)
        assertNotNull(decl)
        assertEquals("log", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForException() {
        val file = myFixture.configureByText("Foo.res", "exception MyError(string)")
        val decl = findDeclaration(file, RescriptElementTypes.EXCEPTION_DECLARATION)
        assertNotNull(decl)
        assertEquals("MyError", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testGetDeclarationNameForRecLet() {
        // 'rec' must be skipped — name follows after it
        val file = myFixture.configureByText("Foo.res", "let rec loop = () => loop()")
        val decl = findDeclaration(file, RescriptElementTypes.LET_DECLARATION)
        assertNotNull(decl)
        assertEquals("loop", (decl as RescriptDeclarationPsiElement).getDeclarationName())
    }

    @Test
    fun testToStringEmbedsElementType() {
        val file = myFixture.configureByText("Foo.res", "let foo = 1")
        val decl = findDeclaration(file, RescriptElementTypes.LET_DECLARATION) as RescriptDeclarationPsiElement
        val text = decl.toString()
        assertTrue(text.startsWith("RescriptDeclarationPsiElement("), "expected toString prefix, got: $text")
        assertTrue(text.endsWith(")"), "expected toString to be parenthesized, got: $text")
    }

    @Test
    fun testAstDeclarationsImplementTheStubContract() {
        val file = myFixture.configureByText("StubContract.res", declarationSource)
        for ((type, name) in declarationTypes) {
            val declaration = findDeclaration(file, type)
            assertTrue(declaration is StubBasedPsiElement<*>, "$name must implement the platform stub contract")
            assertSame(type, (declaration as StubBasedPsiElement<*>).getIElementType())
            assertTrue(type.shouldCreateStub(declaration.node), "$name must remain eligible for indexing")
        }
    }

    @Test
    fun testDefaultStubBuilderAndRestoredDeclarationsKeepCanonicalTypes() {
        val file = myFixture.configureByText("StubContract.res", declarationSource)
        val root = DefaultStubBuilder().buildStubTree(file)
        val stubs = root.childrenStubs.filterIsInstance<RescriptDeclarationStub>()
        assertEquals(declarationTypes.map { it.second }, stubs.map { it.name })
        for ((stub, entry) in stubs.zip(declarationTypes)) {
            val (type, name) = entry
            assertSame(type, stub.elementType)
            val declaration: PsiElement = type.createPsi(stub)
            assertTrue(declaration is StubBasedPsiElement<*>)
            assertSame(type, (declaration as StubBasedPsiElement<*>).getIElementType())
            assertSame(stub, declaration.stub)
            assertEquals(name, (declaration as RescriptDeclarationPsiElement).getDeclarationName())
        }
    }

    @Test
    fun testStoredFileStubVersionResolvesOnlyToTheRescriptFileType() {
        myFixture.configureByText("StubContract.res", declarationSource)
        // Initialize the generic file stub too, matching the competing IDE serializer.
        DefaultStubBuilder().buildStubTree(myFixture.file)
        assertEquals(
            listOf(RescriptParserDefinition.FILE),
            StubBuilderType.getStubFileElementTypeFromVersion("psi.file:0:RESCRIPT_FILE"),
        )
    }

    @Test
    fun testPhysicalProjectDeclarationsSupportHighlightingIndexingAndIntentionDiscovery() {
        val contentRoot = myFixture.tempDirFixture.findOrCreateDir("")
        PsiTestUtil.addContentRoot(myFixture.module, contentRoot)
        try {
            myFixture.configureByText(
                "StubContract.res",
                declarationSource.replace("let stubValue = 1", "let stubValue = 1-><caret>Int.toString"),
            )
            myFixture.doHighlighting()
            assertNotNull(myFixture.findSingleIntention("Convert pipe to function call"))
            myFixture.launchAction(myFixture.findSingleIntention("Convert pipe to function call"))
            assertTrue(
                myFixture.editor.document.text
                    .contains("let stubValue = Int.toString(1)"),
            )
            myFixture.doHighlighting()
            for ((type, name) in declarationTypes) {
                val declarations =
                    StubIndex.getElements(
                        RescriptNameIndex.KEY,
                        name,
                        project,
                        GlobalSearchScope.projectScope(project),
                        RescriptDeclarationPsiElement::class.java,
                    )
                assertEquals(1, declarations.size, "$name must be indexed from the physical project file")
                assertEquals(name, declarations.single().getDeclarationName())
                assertSame(type, declarations.single().node.elementType)
            }
        } finally {
            PsiTestUtil.removeContentEntry(myFixture.module, contentRoot)
        }
    }

    private val declarationTypes =
        listOf(
            RescriptStubElementTypes.LET_DECLARATION to "stubValue",
            RescriptStubElementTypes.TYPE_DECLARATION to "stubType",
            RescriptStubElementTypes.MODULE_DECLARATION to "StubModule",
            RescriptStubElementTypes.EXTERNAL_DECLARATION to "stubExternal",
            RescriptStubElementTypes.EXCEPTION_DECLARATION to "StubException",
        )

    private val declarationSource =
        """
        let stubValue = 1
        type stubType = int
        module StubModule = {}
        external stubExternal: string => unit = "console.log"
        exception StubException(string)
        """.trimIndent()

    private fun findDeclaration(
        scope: PsiElement,
        elementType: com.intellij.psi.tree.IElementType,
    ): PsiElement? {
        if (scope.node?.elementType == elementType) return scope
        for (child in scope.children) {
            val found = findDeclaration(child, elementType)
            if (found != null) return found
        }
        return null
    }
}
