package com.rescript.plugin.binding

import com.rescript.plugin.binding.DtsJsonModel.ClassDeclaration
import com.rescript.plugin.binding.DtsJsonModel.ConstructorDecl
import com.rescript.plugin.binding.DtsJsonModel.DtsFile
import com.rescript.plugin.binding.DtsJsonModel.EnumDeclaration
import com.rescript.plugin.binding.DtsJsonModel.EnumMember
import com.rescript.plugin.binding.DtsJsonModel.FunctionDeclaration
import com.rescript.plugin.binding.DtsJsonModel.InterfaceDeclaration
import com.rescript.plugin.binding.DtsJsonModel.Member
import com.rescript.plugin.binding.DtsJsonModel.MethodDecl
import com.rescript.plugin.binding.DtsJsonModel.Parameter
import com.rescript.plugin.binding.DtsJsonModel.PrimitiveType
import com.rescript.plugin.binding.DtsJsonModel.StringLiteralType
import com.rescript.plugin.binding.DtsJsonModel.TypeAliasDeclaration
import com.rescript.plugin.binding.DtsJsonModel.UnionType
import com.rescript.plugin.binding.DtsJsonModel.VariableDeclaration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DtsToRescriptConverterTest {
    // ── convertFunction ───────────────────────────────────────────────

    @Test
    fun `convert function with no params`() {
        val decl =
            FunctionDeclaration(
                name = "init",
                parameters = emptyList(),
                returnType = PrimitiveType("void"),
            )
        val result = DtsToRescriptConverter.convertFunction(decl, "mylib")
        assertEquals("@module(\"mylib\") external init: unit => unit = \"init\"\n", result)
    }

    @Test
    fun `convert function with single param`() {
        val decl =
            FunctionDeclaration(
                name = "greet",
                parameters = listOf(Parameter("name", PrimitiveType("string"))),
                returnType = PrimitiveType("void"),
            )
        val result = DtsToRescriptConverter.convertFunction(decl, "hello")
        assertEquals("@module(\"hello\") external greet: string => unit = \"greet\"\n", result)
    }

    @Test
    fun `convert function with multiple params`() {
        val decl =
            FunctionDeclaration(
                name = "add",
                parameters =
                    listOf(
                        Parameter("a", PrimitiveType("number")),
                        Parameter("b", PrimitiveType("number")),
                    ),
                returnType = PrimitiveType("number"),
            )
        val result = DtsToRescriptConverter.convertFunction(decl, "math")
        assertEquals("@module(\"math\") external add: (~a: float), (~b: float) => float = \"add\"\n", result)
    }

    // ── convertInterface ──────────────────────────────────────────────

    @Test
    fun `convert interface with required fields`() {
        val decl =
            InterfaceDeclaration(
                name = "User",
                members =
                    listOf(
                        Member("name", PrimitiveType("string")),
                        Member("age", PrimitiveType("number")),
                    ),
            )
        val result = DtsToRescriptConverter.convertInterface(decl)
        assertTrue(result.contains("type user = {"))
        assertTrue(result.contains("  name: string,"))
        assertTrue(result.contains("  age: float,"))
        assertTrue(result.contains("}"))
    }

    @Test
    fun `convert interface with optional fields`() {
        val decl =
            InterfaceDeclaration(
                name = "Config",
                members =
                    listOf(
                        Member("host", PrimitiveType("string")),
                        Member("port", PrimitiveType("number"), optional = true),
                    ),
            )
        val result = DtsToRescriptConverter.convertInterface(decl)
        assertTrue(result.contains("  host: string,"))
        assertTrue(result.contains("  port?: float,"))
    }

    @Test
    fun `convert interface with empty members`() {
        val decl = InterfaceDeclaration(name = "Empty", members = emptyList())
        val result = DtsToRescriptConverter.convertInterface(decl)
        assertTrue(result.contains("type empty = {"))
        assertTrue(result.contains("}"))
    }

    // ── convertTypeAlias ──────────────────────────────────────────────

    @Test
    fun `convert type alias primitive`() {
        val decl = TypeAliasDeclaration(name = "ID", type = PrimitiveType("string"))
        val result = DtsToRescriptConverter.convertTypeAlias(decl)
        assertEquals("type iD = string\n", result)
    }

    @Test
    fun `convert type alias string literal union`() {
        val decl =
            TypeAliasDeclaration(
                name = "Status",
                type = UnionType(listOf(StringLiteralType("active"), StringLiteralType("inactive"))),
            )
        val result = DtsToRescriptConverter.convertTypeAlias(decl)
        assertEquals("type status = [#\"active\" | #\"inactive\"]\n", result)
    }

    // ── convertVariable ───────────────────────────────────────────────

    @Test
    fun `convert variable`() {
        val decl = VariableDeclaration(name = "VERSION", type = PrimitiveType("string"), isConst = true)
        val result = DtsToRescriptConverter.convertVariable(decl, "config")
        assertEquals("@module(\"config\") external VERSION: string = \"VERSION\"\n", result)
    }

    // ── convertEnum ───────────────────────────────────────────────────

    @Test
    fun `convert string enum`() {
        val decl =
            EnumDeclaration(
                name = "Color",
                members =
                    listOf(
                        EnumMember("Red", "red"),
                        EnumMember("Green", "green"),
                        EnumMember("Blue", "blue"),
                    ),
                isStringEnum = true,
            )
        val result = DtsToRescriptConverter.convertEnum(decl)
        assertEquals("type color = [#\"red\" | #\"green\" | #\"blue\"]\n", result)
    }

    @Test
    fun `convert numeric enum`() {
        val decl =
            EnumDeclaration(
                name = "Direction",
                members =
                    listOf(
                        EnumMember("Up", "0"),
                        EnumMember("Down", "1"),
                        EnumMember("Left", "2"),
                        EnumMember("Right", "3"),
                    ),
                isStringEnum = false,
            )
        val result = DtsToRescriptConverter.convertEnum(decl)
        assertTrue(result.contains("module Direction = {"))
        assertTrue(result.contains("  type t = int"))
        assertTrue(result.contains("  @inline let Up: t = 0"))
        assertTrue(result.contains("  @inline let Down: t = 1"))
        assertTrue(result.contains("}"))
    }

    @Test
    fun `convert numeric enum with null values uses index`() {
        val decl =
            EnumDeclaration(
                name = "Prio",
                members =
                    listOf(
                        EnumMember("Low", null),
                        EnumMember("Med", null),
                        EnumMember("High", null),
                    ),
                isStringEnum = false,
            )
        val result = DtsToRescriptConverter.convertEnum(decl)
        assertTrue(result.contains("@inline let Low: t = 0"))
        assertTrue(result.contains("@inline let Med: t = 1"))
        assertTrue(result.contains("@inline let High: t = 2"))
    }

    // ── convertClass ──────────────────────────────────────────────────

    @Test
    fun `convert class with constructor`() {
        val decl =
            ClassDeclaration(
                name = "Greeter",
                constructors =
                    listOf(
                        ConstructorDecl(listOf(Parameter("msg", PrimitiveType("string")))),
                    ),
                methods = emptyList(),
                properties = emptyList(),
            )
        val result = DtsToRescriptConverter.convertClass(decl, "mylib")
        assertTrue(result.contains("module Greeter = {"))
        assertTrue(result.contains("  type t"))
        assertTrue(result.contains("@module(\"mylib\") @new external make: string => t = \"Greeter\""))
        assertTrue(result.contains("}"))
    }

    @Test
    fun `convert class with default constructor`() {
        val decl =
            ClassDeclaration(
                name = "Widget",
                constructors = emptyList(),
                methods = emptyList(),
                properties = emptyList(),
            )
        val result = DtsToRescriptConverter.convertClass(decl, "widgets")
        assertTrue(result.contains("@module(\"widgets\") @new external make: unit => t = \"Widget\""))
    }

    @Test
    fun `convert class with methods`() {
        val decl =
            ClassDeclaration(
                name = "Calculator",
                constructors = emptyList(),
                methods =
                    listOf(
                        MethodDecl("add", listOf(Parameter("x", PrimitiveType("number"))), PrimitiveType("number")),
                        MethodDecl("reset", emptyList(), PrimitiveType("void")),
                    ),
                properties = emptyList(),
            )
        val result = DtsToRescriptConverter.convertClass(decl, "calc")
        assertTrue(result.contains("@send external add: (t, (~x: float)) => float = \"add\""))
        assertTrue(result.contains("@send external reset: t => unit = \"reset\""))
    }

    @Test
    fun `convert class with properties`() {
        val decl =
            ClassDeclaration(
                name = "Widget",
                constructors = emptyList(),
                methods = emptyList(),
                properties =
                    listOf(
                        Member("width", PrimitiveType("number")),
                        Member("label", PrimitiveType("string")),
                    ),
            )
        val result = DtsToRescriptConverter.convertClass(decl, "widgets")
        assertTrue(result.contains("@get external width: t => float = \"width\""))
        assertTrue(result.contains("@get external label: t => string = \"label\""))
    }

    // ── convert (full file) ───────────────────────────────────────────

    @Test
    fun `convert full file with header`() {
        val file =
            DtsFile(
                fileName = "mylib.d.ts",
                moduleName = "mylib",
                declarations =
                    listOf(
                        FunctionDeclaration("init", true, emptyList(), PrimitiveType("void")),
                    ),
            )
        val result = DtsToRescriptConverter.convert(file)
        assertTrue(result.contains("// Generated from mylib.d.ts"))
        assertTrue(result.contains("// Module: mylib"))
        assertTrue(result.contains("@module(\"mylib\") external init: unit => unit = \"init\""))
    }

    @Test
    fun `convert file with errors includes comments`() {
        val file =
            DtsFile(
                fileName = "bad.d.ts",
                moduleName = "bad",
                declarations = emptyList(),
                errors = listOf("Could not parse line 42"),
            )
        val result = DtsToRescriptConverter.convert(file)
        assertTrue(result.contains("// Parsing errors:"))
        assertTrue(result.contains("// - Could not parse line 42"))
    }

    @Test
    fun `convert empty file`() {
        val file =
            DtsFile(
                fileName = "empty.d.ts",
                moduleName = "empty",
                declarations = emptyList(),
            )
        val result = DtsToRescriptConverter.convert(file)
        assertTrue(result.contains("// Generated from empty.d.ts"))
        assertTrue(result.contains("// Module: empty"))
    }

    @Test
    fun `convert file with multiple declarations`() {
        val file =
            DtsFile(
                fileName = "multi.d.ts",
                moduleName = "multi",
                declarations =
                    listOf(
                        VariableDeclaration("VERSION", true, PrimitiveType("string"), true),
                        FunctionDeclaration("init", true, emptyList(), PrimitiveType("void")),
                        InterfaceDeclaration(
                            "Config",
                            true,
                            listOf(Member("port", PrimitiveType("number"))),
                        ),
                    ),
            )
        val result = DtsToRescriptConverter.convert(file)
        assertTrue(result.contains("external VERSION: string"))
        assertTrue(result.contains("external init: unit => unit"))
        assertTrue(result.contains("type config = {"))
    }
}
