package com.rescript.plugin.binding

import com.rescript.plugin.binding.DtsJsonModel.ArrayType
import com.rescript.plugin.binding.DtsJsonModel.ClassDeclaration
import com.rescript.plugin.binding.DtsJsonModel.EnumDeclaration
import com.rescript.plugin.binding.DtsJsonModel.FunctionDeclaration
import com.rescript.plugin.binding.DtsJsonModel.FunctionType
import com.rescript.plugin.binding.DtsJsonModel.InterfaceDeclaration
import com.rescript.plugin.binding.DtsJsonModel.PrimitiveType
import com.rescript.plugin.binding.DtsJsonModel.ReferenceType
import com.rescript.plugin.binding.DtsJsonModel.StringLiteralType
import com.rescript.plugin.binding.DtsJsonModel.TupleType
import com.rescript.plugin.binding.DtsJsonModel.TypeAliasDeclaration
import com.rescript.plugin.binding.DtsJsonModel.UnionType
import com.rescript.plugin.binding.DtsJsonModel.UnknownType
import com.rescript.plugin.binding.DtsJsonModel.VariableDeclaration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DtsJsonModelTest {
    @Test
    fun `parse empty file`() {
        val json = """{"fileName": "test.d.ts", "moduleName": "test", "declarations": [], "errors": []}"""
        val result = DtsJsonModel.parse(json)
        assertEquals("test.d.ts", result.fileName)
        assertEquals("test", result.moduleName)
        assertTrue(result.declarations.isEmpty())
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `parse function declaration`() {
        val json =
            """{
            "fileName": "lib.d.ts",
            "moduleName": "lib",
            "declarations": [{
                "kind": "function",
                "name": "greet",
                "exported": true,
                "parameters": [{"name": "name", "type": {"kind": "primitive", "name": "string"}, "optional": false}],
                "returnType": {"kind": "primitive", "name": "void"}
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        assertEquals(1, result.declarations.size)
        val func = result.declarations[0] as FunctionDeclaration
        assertEquals("greet", func.name)
        assertTrue(func.exported)
        assertEquals(1, func.parameters.size)
        assertEquals("name", func.parameters[0].name)
        assertTrue(func.parameters[0].type is PrimitiveType)
        assertEquals("string", (func.parameters[0].type as PrimitiveType).name)
        assertTrue(func.returnType is PrimitiveType)
        assertEquals("void", (func.returnType as PrimitiveType).name)
    }

    @Test
    fun `parse interface declaration`() {
        val json =
            """{
            "fileName": "types.d.ts",
            "moduleName": "types",
            "declarations": [{
                "kind": "interface",
                "name": "User",
                "exported": true,
                "members": [
                    {"name": "id", "type": {"kind": "primitive", "name": "number"}, "optional": false, "readonly": false},
                    {"name": "email", "type": {"kind": "primitive", "name": "string"}, "optional": true, "readonly": false}
                ]
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val iface = result.declarations[0] as InterfaceDeclaration
        assertEquals("User", iface.name)
        assertEquals(2, iface.members.size)
        assertEquals("id", iface.members[0].name)
        assertFalse(iface.members[0].optional)
        assertEquals("email", iface.members[1].name)
        assertTrue(iface.members[1].optional)
    }

    @Test
    fun `parse type alias with union`() {
        val json =
            """{
            "fileName": "types.d.ts",
            "moduleName": "types",
            "declarations": [{
                "kind": "typeAlias",
                "name": "Status",
                "exported": true,
                "type": {
                    "kind": "union",
                    "types": [
                        {"kind": "stringLiteral", "value": "active"},
                        {"kind": "stringLiteral", "value": "inactive"}
                    ]
                }
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val alias = result.declarations[0] as TypeAliasDeclaration
        assertEquals("Status", alias.name)
        assertTrue(alias.type is UnionType)
        val union = alias.type as UnionType
        assertEquals(2, union.types.size)
        assertTrue(union.types[0] is StringLiteralType)
        assertEquals("active", (union.types[0] as StringLiteralType).value)
    }

    @Test
    fun `parse variable declaration`() {
        val json =
            """{
            "fileName": "config.d.ts",
            "moduleName": "config",
            "declarations": [{
                "kind": "variable",
                "name": "VERSION",
                "exported": true,
                "type": {"kind": "primitive", "name": "string"},
                "isConst": true
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val variable = result.declarations[0] as VariableDeclaration
        assertEquals("VERSION", variable.name)
        assertTrue(variable.isConst)
        assertTrue(variable.type is PrimitiveType)
    }

    @Test
    fun `parse enum declaration`() {
        val json =
            """{
            "fileName": "enums.d.ts",
            "moduleName": "enums",
            "declarations": [{
                "kind": "enum",
                "name": "Color",
                "exported": true,
                "members": [
                    {"name": "Red", "value": "red"},
                    {"name": "Green", "value": "green"}
                ],
                "isStringEnum": true
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val enum = result.declarations[0] as EnumDeclaration
        assertEquals("Color", enum.name)
        assertTrue(enum.isStringEnum)
        assertEquals(2, enum.members.size)
        assertEquals("Red", enum.members[0].name)
        assertEquals("red", enum.members[0].value)
    }

    @Test
    fun `parse class declaration`() {
        val json =
            """{
            "fileName": "classes.d.ts",
            "moduleName": "classes",
            "declarations": [{
                "kind": "class",
                "name": "MyClass",
                "exported": true,
                "constructors": [{"parameters": [{"name": "value", "type": {"kind": "primitive", "name": "string"}, "optional": false}]}],
                "methods": [{"name": "getValue", "parameters": [], "returnType": {"kind": "primitive", "name": "string"}}],
                "properties": [{"name": "count", "type": {"kind": "primitive", "name": "number"}, "optional": false, "readonly": false}]
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val cls = result.declarations[0] as ClassDeclaration
        assertEquals("MyClass", cls.name)
        assertEquals(1, cls.constructors.size)
        assertEquals(1, cls.methods.size)
        assertEquals("getValue", cls.methods[0].name)
        assertEquals(1, cls.properties.size)
        assertEquals("count", cls.properties[0].name)
    }

    @Test
    fun `parse array type`() {
        val json =
            """{
            "fileName": "test.d.ts",
            "moduleName": "test",
            "declarations": [{
                "kind": "variable",
                "name": "items",
                "exported": true,
                "type": {"kind": "array", "elementType": {"kind": "primitive", "name": "string"}},
                "isConst": false
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val variable = result.declarations[0] as VariableDeclaration
        assertTrue(variable.type is ArrayType)
        assertEquals("string", ((variable.type as ArrayType).elementType as PrimitiveType).name)
    }

    @Test
    fun `parse tuple type`() {
        val json =
            """{
            "fileName": "test.d.ts",
            "moduleName": "test",
            "declarations": [{
                "kind": "variable",
                "name": "pair",
                "exported": true,
                "type": {"kind": "tuple", "elements": [{"kind": "primitive", "name": "string"}, {"kind": "primitive", "name": "number"}]},
                "isConst": false
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val variable = result.declarations[0] as VariableDeclaration
        assertTrue(variable.type is TupleType)
        val tuple = variable.type as TupleType
        assertEquals(2, tuple.elements.size)
    }

    @Test
    fun `parse reference type with type arguments`() {
        val json =
            """{
            "fileName": "test.d.ts",
            "moduleName": "test",
            "declarations": [{
                "kind": "variable",
                "name": "data",
                "exported": true,
                "type": {"kind": "reference", "name": "Promise", "typeArguments": [{"kind": "primitive", "name": "string"}]},
                "isConst": false
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val variable = result.declarations[0] as VariableDeclaration
        assertTrue(variable.type is ReferenceType)
        val ref = variable.type as ReferenceType
        assertEquals("Promise", ref.name)
        assertEquals(1, ref.typeArguments.size)
    }

    @Test
    fun `parse function type`() {
        val json =
            """{
            "fileName": "test.d.ts",
            "moduleName": "test",
            "declarations": [{
                "kind": "variable",
                "name": "callback",
                "exported": true,
                "type": {
                    "kind": "function",
                    "parameters": [{"name": "err", "type": {"kind": "primitive", "name": "any"}, "optional": false}],
                    "returnType": {"kind": "primitive", "name": "void"}
                },
                "isConst": false
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val variable = result.declarations[0] as VariableDeclaration
        assertTrue(variable.type is FunctionType)
        val fn = variable.type as FunctionType
        assertEquals(1, fn.parameters.size)
        assertEquals("err", fn.parameters[0].name)
    }

    @Test
    fun `parse unknown type`() {
        val json =
            """{
            "fileName": "test.d.ts",
            "moduleName": "test",
            "declarations": [{
                "kind": "variable",
                "name": "x",
                "exported": true,
                "type": {"kind": "unknown", "text": "SomeComplexType"},
                "isConst": false
            }],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        val variable = result.declarations[0] as VariableDeclaration
        assertTrue(variable.type is UnknownType)
        assertEquals("SomeComplexType", (variable.type as UnknownType).text)
    }

    @Test
    fun `parse with errors`() {
        val json =
            """{
            "fileName": "broken.d.ts",
            "moduleName": "broken",
            "declarations": [],
            "errors": ["Error processing 123: some error"]
        }"""
        val result = DtsJsonModel.parse(json)
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].contains("some error"))
    }

    @Test
    fun `parse unknown declaration kind falls back to VariableDeclaration`() {
        val json =
            """{
            "fileName": "test.d.ts",
            "moduleName": "test",
            "declarations": [{"kind": "namespace", "name": "foo", "exported": true}],
            "errors": []
        }"""
        val result = DtsJsonModel.parse(json)
        assertEquals(1, result.declarations.size)
        assertTrue(result.declarations[0] is VariableDeclaration)
    }

    @Test
    fun `DtsFile data class defaults`() {
        val file = DtsJsonModel.DtsFile()
        assertEquals("", file.fileName)
        assertEquals("", file.moduleName)
        assertTrue(file.declarations.isEmpty())
        assertTrue(file.errors.isEmpty())
    }

    @Test
    fun `createGson returns configured instance`() {
        val gson = DtsJsonModel.createGson()
        // Smoke test: can deserialize a simple file
        val json = """{"fileName": "a.d.ts", "moduleName": "a", "declarations": [], "errors": []}"""
        val file = gson.fromJson<DtsJsonModel.DtsFile>(json, DtsJsonModel.DtsFile::class.java)
        assertEquals("a.d.ts", file.fileName)
    }
}
