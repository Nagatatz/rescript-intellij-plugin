package com.rescript.plugin.binding

import com.rescript.plugin.binding.DtsJsonModel.FunctionDeclaration
import com.rescript.plugin.binding.DtsJsonModel.PrimitiveType
import com.rescript.plugin.binding.DtsJsonModel.VariableDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DtsJsonParserTest {
    @Test
    fun `createGson returns configured instance`() {
        val gson = DtsJsonParser.createGson()
        val json = """{"fileName": "a.d.ts", "moduleName": "a", "declarations": [], "errors": []}"""
        val file = gson.fromJson<DtsJsonModel.DtsFile>(json, DtsJsonModel.DtsFile::class.java)
        assertEquals("a.d.ts", file.fileName)
    }

    @Test
    fun `parse deserializes DtsFile from JSON`() {
        val json =
            """
            {
              "fileName": "test.d.ts",
              "moduleName": "Test",
              "declarations": [
                {"kind": "variable", "name": "x", "exported": true, "type": {"kind": "primitive", "name": "string"}}
              ],
              "errors": []
            }
            """.trimIndent()
        val file = DtsJsonParser.parse(json)
        assertEquals("test.d.ts", file.fileName)
        assertEquals("Test", file.moduleName)
        assertEquals(1, file.declarations.size)
        val decl = file.declarations[0]
        assertTrue(decl is VariableDeclaration)
        assertEquals("x", decl.name)
        assertTrue((decl as VariableDeclaration).type is PrimitiveType)
    }

    @Test
    fun `parse handles function declarations`() {
        val json =
            """
            {
              "fileName": "fn.d.ts",
              "moduleName": "Fn",
              "declarations": [
                {
                  "kind": "function",
                  "name": "greet",
                  "exported": true,
                  "parameters": [{"name": "name", "type": {"kind": "primitive", "name": "string"}}],
                  "returnType": {"kind": "primitive", "name": "void"}
                }
              ],
              "errors": []
            }
            """.trimIndent()
        val file = DtsJsonParser.parse(json)
        val decl = file.declarations[0]
        assertTrue(decl is FunctionDeclaration)
        val fn = decl as FunctionDeclaration
        assertEquals("greet", fn.name)
        assertEquals(1, fn.parameters.size)
        assertEquals("name", fn.parameters[0].name)
    }

    @Test
    fun `parse handles empty file`() {
        val json = """{"fileName": "", "moduleName": "", "declarations": [], "errors": []}"""
        val file = DtsJsonParser.parse(json)
        assertTrue(file.declarations.isEmpty())
        assertTrue(file.errors.isEmpty())
    }
}
