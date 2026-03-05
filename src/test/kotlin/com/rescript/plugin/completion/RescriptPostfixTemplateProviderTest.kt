package com.rescript.plugin.completion

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.lang.RescriptTokenTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptPostfixTemplateProviderTest {
    private val provider = RescriptPostfixTemplateProvider()

    @Test
    fun testTemplateCountIsNine() {
        assertEquals(9, provider.templates.size)
    }

    @Test
    fun testTemplateKeysContainSwitch() {
        assertTrue(provider.templates.any { it.key == ".switch" })
    }

    @Test
    fun testTemplateKeysContainPipe() {
        assertTrue(provider.templates.any { it.key == ".pipe" })
    }

    @Test
    fun testTemplateKeysContainLog() {
        assertTrue(provider.templates.any { it.key == ".log" })
    }

    @Test
    fun testTemplateKeysContainSome() {
        assertTrue(provider.templates.any { it.key == ".some" })
    }

    @Test
    fun testTemplateKeysContainOk() {
        assertTrue(provider.templates.any { it.key == ".ok" })
    }

    @Test
    fun testTemplateKeysContainError() {
        assertTrue(provider.templates.any { it.key == ".error" })
    }

    @Test
    fun testTemplateKeysContainIgnore() {
        assertTrue(provider.templates.any { it.key == ".ignore" })
    }

    @Test
    fun testTemplateKeysContainPromise() {
        assertTrue(provider.templates.any { it.key == ".promise" })
    }

    @Test
    fun testTemplateKeysContainAwait() {
        assertTrue(provider.templates.any { it.key == ".await" })
    }

    @Test
    fun testAllTemplateKeysExactly() {
        val expectedKeys = setOf(".switch", ".pipe", ".log", ".some", ".ok", ".error", ".ignore", ".promise", ".await")
        val actualKeys = provider.templates.map { it.key }.toSet()
        assertEquals(expectedKeys, actualKeys)
    }

    @Test
    fun testIsTerminalSymbolForDot() {
        assertTrue(provider.isTerminalSymbol('.'))
    }

    @Test
    fun testIsTerminalSymbolForNonDot() {
        assertFalse(provider.isTerminalSymbol(' '))
        assertFalse(provider.isTerminalSymbol(';'))
        assertFalse(provider.isTerminalSymbol(','))
        assertFalse(provider.isTerminalSymbol('\n'))
    }

    @Test
    fun testTemplatesAreNotEmpty() {
        for (template in provider.templates) {
            assertNotNull(template.key)
            assertTrue(template.key.isNotBlank(), "Template key '${template.key}' should not be blank")
        }
    }

    @Test
    fun testSwitchTemplateExample() {
        val switchTemplate = provider.templates.first { it.key == ".switch" }
        assertEquals("switch expr { | _ => }", switchTemplate.example)
    }

    @Test
    fun testPipeTemplateExample() {
        val pipeTemplate = provider.templates.first { it.key == ".pipe" }
        assertEquals("expr->", pipeTemplate.example)
    }

    @Test
    fun testLogTemplateExample() {
        val logTemplate = provider.templates.first { it.key == ".log" }
        assertEquals("Console.log(expr)", logTemplate.example)
    }

    @Test
    fun testSomeTemplateExample() {
        val someTemplate = provider.templates.first { it.key == ".some" }
        assertEquals("Some(expr)", someTemplate.example)
    }

    @Test
    fun testOkTemplateExample() {
        val okTemplate = provider.templates.first { it.key == ".ok" }
        assertEquals("Ok(expr)", okTemplate.example)
    }

    @Test
    fun testErrorTemplateExample() {
        val errorTemplate = provider.templates.first { it.key == ".error" }
        assertEquals("Error(expr)", errorTemplate.example)
    }

    @Test
    fun testIgnoreTemplateExample() {
        val ignoreTemplate = provider.templates.first { it.key == ".ignore" }
        assertEquals("expr->ignore", ignoreTemplate.example)
    }

    @Test
    fun testPromiseTemplateExample() {
        val promiseTemplate = provider.templates.first { it.key == ".promise" }
        assertEquals("expr->Promise.then(result => { ... })", promiseTemplate.example)
    }

    @Test
    fun testAwaitTemplateExample() {
        val awaitTemplate = provider.templates.first { it.key == ".await" }
        assertEquals("await expr", awaitTemplate.example)
    }

    @Test
    fun testPreCheckReturnsSameFile() {
        val file = stubPsiFile()
        val result = provider.preCheck(file, stubEditor(), 0)
        assertTrue(file === result)
    }

    @Test
    fun testIsRescriptApplicableReturnsFalseForNonRescriptFile() {
        val element = stubPsiElementWithLanguage(null, RescriptTokenTypes.LIDENT)
        assertFalse(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    @Test
    fun testIsRescriptApplicableReturnsFalseForComment() {
        val element = stubPsiElementWithLanguage(RescriptLanguage, RescriptTokenTypes.SINGLE_COMMENT)
        assertFalse(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    @Test
    fun testIsRescriptApplicableReturnsFalseForMultiComment() {
        val element = stubPsiElementWithLanguage(RescriptLanguage, RescriptTokenTypes.MULTI_COMMENT)
        assertFalse(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    @Test
    fun testIsRescriptApplicableReturnsFalseForString() {
        val element = stubPsiElementWithLanguage(RescriptLanguage, RescriptTokenTypes.STRING_VALUE)
        assertFalse(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    @Test
    fun testIsRescriptApplicableReturnsTrueForIdentifier() {
        val element = stubPsiElementWithLanguage(RescriptLanguage, RescriptTokenTypes.LIDENT)
        assertTrue(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    @Test
    fun testIsRescriptApplicableReturnsTrueForUpperIdentifier() {
        val element = stubPsiElementWithLanguage(RescriptLanguage, RescriptTokenTypes.UIDENT)
        assertTrue(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    @Test
    fun testIsRescriptApplicableReturnsFalseForJsStringOpen() {
        val element = stubPsiElementWithLanguage(RescriptLanguage, RescriptTokenTypes.JS_STRING_OPEN)
        assertFalse(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    @Test
    fun testIsRescriptApplicableReturnsFalseForJsStringClose() {
        val element = stubPsiElementWithLanguage(RescriptLanguage, RescriptTokenTypes.JS_STRING_CLOSE)
        assertFalse(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    @Test
    fun testIsRescriptApplicableReturnsFalseForNullNode() {
        // Element with RescriptLanguage file but null node
        val file = stubPsiFileWithLanguage(RescriptLanguage)
        val element =
            java.lang.reflect.Proxy.newProxyInstance(
                PsiElement::class.java.classLoader,
                arrayOf(PsiElement::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getContainingFile" -> file
                    "getNode" -> null
                    "toString" -> "StubPsiElement(nullNode)"
                    "hashCode" -> 0
                    "equals" -> false
                    else -> null
                }
            } as PsiElement
        assertFalse(RescriptPostfixTemplateProvider.isRescriptApplicable(element))
    }

    // -- stub helpers --

    private fun stubPsiFile(): PsiFile =
        java.lang.reflect.Proxy.newProxyInstance(
            PsiFile::class.java.classLoader,
            arrayOf(PsiFile::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "StubPsiFile"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as PsiFile

    private fun stubEditor(): com.intellij.openapi.editor.Editor =
        java.lang.reflect.Proxy.newProxyInstance(
            com.intellij.openapi.editor.Editor::class.java.classLoader,
            arrayOf(com.intellij.openapi.editor.Editor::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "StubEditor"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as com.intellij.openapi.editor.Editor

    private fun stubPsiFileWithLanguage(lang: com.intellij.lang.Language?): PsiFile =
        java.lang.reflect.Proxy.newProxyInstance(
            PsiFile::class.java.classLoader,
            arrayOf(PsiFile::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getLanguage" -> lang
                "toString" -> "StubPsiFile($lang)"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as PsiFile

    private fun stubPsiElementWithLanguage(
        lang: com.intellij.lang.Language?,
        tokenType: IElementType,
    ): PsiElement {
        val file = stubPsiFileWithLanguage(lang)
        val node =
            java.lang.reflect.Proxy.newProxyInstance(
                ASTNode::class.java.classLoader,
                arrayOf(ASTNode::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getElementType" -> tokenType
                    "toString" -> "StubASTNode($tokenType)"
                    "hashCode" -> tokenType.hashCode()
                    "equals" -> false
                    else -> null
                }
            } as ASTNode

        return java.lang.reflect.Proxy.newProxyInstance(
            PsiElement::class.java.classLoader,
            arrayOf(PsiElement::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getContainingFile" -> file
                "getNode" -> node
                "toString" -> "StubPsiElement($tokenType)"
                "hashCode" -> tokenType.hashCode()
                "equals" -> false
                else -> null
            }
        } as PsiElement
    }
}
