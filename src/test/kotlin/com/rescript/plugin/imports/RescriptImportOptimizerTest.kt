package com.rescript.plugin.imports

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptTestUtils.SimpleStubElement
import com.rescript.plugin.RescriptTestUtils.stubProxy
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.lang.psi.RescriptElementTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RescriptImportOptimizerTest {
    private val optimizer = RescriptImportOptimizer()

    // ── supports() tests ─────────────────────────────────────────────

    @Test
    fun testSupportsReturnsFalseForNonRescriptFile() {
        val file = stubProxy<com.intellij.psi.PsiFile>()
        assertFalse(optimizer.supports(file))
    }

    // ── extractModulePath() tests ────────────────────────────────────

    @Test
    fun testExtractSimpleModulePath() {
        val openStmt = buildOpenStatement(listOf("Belt"))
        assertEquals("Belt", RescriptImportUtil.extractModulePath(openStmt))
    }

    @Test
    fun testExtractDottedModulePath() {
        val openStmt = buildOpenStatement(listOf("Belt", ".", "Array"))
        assertEquals("Belt.Array", RescriptImportUtil.extractModulePath(openStmt))
    }

    @Test
    fun testExtractDeepModulePath() {
        val openStmt = buildOpenStatement(listOf("Js", ".", "Promise2", ".", "Result"))
        assertEquals("Js.Promise2.Result", RescriptImportUtil.extractModulePath(openStmt))
    }

    @Test
    fun testExtractEmptyModulePath() {
        val openStmt = buildOpenStatement(emptyList())
        assertEquals("", RescriptImportUtil.extractModulePath(openStmt))
    }

    @Test
    fun testExtractModulePathSkipsChildWithNullNode() {
        // Build open statement with a child whose node returns null
        val openChild = SimpleStubElement(RescriptTokenTypes.OPEN, "open")
        val nullNodeChild = NullNodeStubElement("whitespace")
        val moduleChild = SimpleStubElement(RescriptTokenTypes.UIDENT, "Belt")

        openChild.next = nullNodeChild
        nullNodeChild.next = moduleChild

        val openStmt =
            object : SimpleStubElement(RescriptElementTypes.OPEN_STATEMENT, "open Belt") {
                override fun getFirstChild(): PsiElement = openChild
            }

        assertEquals("Belt", RescriptImportUtil.extractModulePath(openStmt))
    }

    // ── buildNotificationMessage() tests ─────────────────────────────

    @Test
    fun testNotificationMessageNoDuplicatesNoUnused() {
        assertEquals(
            "No open statements to remove",
            RescriptImportOptimizer.buildNotificationMessage(0, 0),
        )
    }

    @Test
    fun testNotificationMessageOnlyDuplicates() {
        assertEquals(
            "Removed 3 duplicate open statement(s)",
            RescriptImportOptimizer.buildNotificationMessage(3, 0),
        )
    }

    @Test
    fun testNotificationMessageOnlyUnused() {
        assertEquals(
            "Removed 2 unused open statement(s)",
            RescriptImportOptimizer.buildNotificationMessage(0, 2),
        )
    }

    @Test
    fun testNotificationMessageBothDuplicatesAndUnused() {
        assertEquals(
            "Removed 1 duplicate and 4 unused open statement(s)",
            RescriptImportOptimizer.buildNotificationMessage(1, 4),
        )
    }

    @Test
    fun testNotificationMessageSingleDuplicate() {
        assertEquals(
            "Removed 1 duplicate open statement(s)",
            RescriptImportOptimizer.buildNotificationMessage(1, 0),
        )
    }

    @Test
    fun testNotificationMessageSingleUnused() {
        assertEquals(
            "Removed 1 unused open statement(s)",
            RescriptImportOptimizer.buildNotificationMessage(0, 1),
        )
    }

    // ── Stub helpers ─────────────────────────────────────────────────

    private fun buildOpenStatement(pathTokens: List<String>): PsiElement {
        data class TokenInfo(
            val type: IElementType,
            val text: String,
        )

        val tokens = mutableListOf(TokenInfo(RescriptTokenTypes.OPEN, "open"))
        for (token in pathTokens) {
            if (token == ".") {
                tokens.add(TokenInfo(RescriptTokenTypes.DOT, "."))
            } else {
                tokens.add(TokenInfo(RescriptTokenTypes.UIDENT, token))
            }
        }

        // Create stub child elements as linked list
        val children = tokens.map { info -> SimpleStubElement(info.type, info.text) }
        for (i in children.indices) {
            if (i + 1 < children.size) {
                children[i].next = children[i + 1]
            }
        }

        return object : SimpleStubElement(RescriptElementTypes.OPEN_STATEMENT, "open ${pathTokens.joinToString("")}") {
            override fun getFirstChild(): PsiElement = children.first()
        }
    }

    /** PsiElement stub whose getNode() returns null, simulating missing AST. */
    private class NullNodeStubElement(
        private val textContent: String,
    ) : PsiElement by stubProxy() {
        var next: PsiElement? = null

        override fun getNode(): com.intellij.lang.ASTNode? = null

        override fun getText(): String = textContent

        override fun getNextSibling(): PsiElement? = next
    }
}
