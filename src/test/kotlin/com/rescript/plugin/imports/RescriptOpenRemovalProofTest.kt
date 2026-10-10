package com.rescript.plugin.imports

import com.rescript.plugin.ParsingTestHelper
import com.rescript.plugin.RescriptParsingTestExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/** Verifies module identity proofs using the actual lexer and parser. */
@ExtendWith(RescriptParsingTestExtension::class)
class RescriptOpenRemovalProofTest {
    private lateinit var parsingHelper: ParsingTestHelper

    @Test
    fun `adjacent opens of a literal local module have the same target`() {
        val file = parsingHelper.parseCode("module A = {let value = 1}\nopen A\n// preserve comment\nopen A\nopen A")
        assertEquals(
            listOf("open A", "open A"),
            RescriptOpenRemovalProof.findRedundantOpens(file).map { it.text.trim() },
        )
    }

    @Test
    fun `reopens intervening declarations and unknown modules are preserved`() {
        for (source in listOf(
            "module A = {let value = 1}\nmodule B = {let value = 2}\nopen A\nopen B\nopen A",
            "module A = {let value = 1}\nopen A\nlet value = 2\nopen A",
            "open External\nopen External",
            "module A = {let value = 1}\nopen External\nopen A\nopen A",
            "module A = External\nopen A\nopen A",
            "module A = {let value = compute()}\nopen A\nopen A",
            "@genType\nmodule A = {let value = 1}\nopen A\nopen A",
            "module A = {module A = {let value = 2}}\nopen A\nopen A",
            "module Outer = {open A\nopen A}",
        )) {
            assertTrue(RescriptOpenRemovalProof.findRedundantOpens(parsingHelper.parseCode(source)).isEmpty(), source)
        }
    }

    @Test
    fun `module redefinition and qualified paths invalidate identity assumptions`() {
        for (source in listOf(
            "module A = {let value = 1}\nopen A\nmodule A = External\nopen A\nopen A",
            "module A = {let value = 1}\nopen A.Sub\nopen A.Sub",
            "module A = {let value = \"module A open A\"}\nopen Other\nopen A\nopen A",
        )) {
            assertTrue(RescriptOpenRemovalProof.findRedundantOpens(parsingHelper.parseCode(source)).isEmpty(), source)
        }
    }
}
