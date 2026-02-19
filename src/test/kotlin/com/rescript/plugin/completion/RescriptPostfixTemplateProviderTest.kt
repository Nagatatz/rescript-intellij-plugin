package com.rescript.plugin.completion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptPostfixTemplateProviderTest {
    private val provider = RescriptPostfixTemplateProvider()

    @Test
    fun testTemplateCountIsSeven() {
        assertEquals(7, provider.templates.size)
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
    fun testAllTemplateKeysExactly() {
        val expectedKeys = setOf(".switch", ".pipe", ".log", ".some", ".ok", ".error", ".ignore")
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
            assertTrue("Template key '${template.key}' should not be blank", template.key.isNotBlank())
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
}
