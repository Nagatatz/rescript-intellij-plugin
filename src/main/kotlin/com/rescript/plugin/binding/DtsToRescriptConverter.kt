package com.rescript.plugin.binding

import com.rescript.plugin.binding.DtsJsonModel.ClassDeclaration
import com.rescript.plugin.binding.DtsJsonModel.DtsFile
import com.rescript.plugin.binding.DtsJsonModel.EnumDeclaration
import com.rescript.plugin.binding.DtsJsonModel.FunctionDeclaration
import com.rescript.plugin.binding.DtsJsonModel.InterfaceDeclaration
import com.rescript.plugin.binding.DtsJsonModel.TypeAliasDeclaration
import com.rescript.plugin.binding.DtsJsonModel.UnionType
import com.rescript.plugin.binding.DtsJsonModel.VariableDeclaration

/**
 * Converts a [DtsFile] JSON model into ReScript binding source code.
 *
 * Generates `external` declarations, type definitions, and module structures
 * from parsed `.d.ts` file data. Each TypeScript declaration kind maps to
 * an idiomatic ReScript binding pattern.
 *
 * @see DtsJsonModel for the input data model
 * @see DtsTypeMapper for individual type conversions
 */
object DtsToRescriptConverter {
    /**
     * Converts a parsed [DtsFile] to a ReScript binding source string.
     *
     * @param file the parsed `.d.ts` file model
     * @return the generated ReScript binding code
     */
    fun convert(file: DtsFile): String {
        val sb = StringBuilder()
        sb.appendLine("// Generated from ${file.fileName}")
        sb.appendLine("// Module: ${file.moduleName}")
        sb.appendLine()

        for ((index, decl) in file.declarations.withIndex()) {
            if (index > 0) sb.appendLine()
            when (decl) {
                is FunctionDeclaration -> sb.append(convertFunction(decl, file.moduleName))
                is InterfaceDeclaration -> sb.append(convertInterface(decl))
                is TypeAliasDeclaration -> sb.append(convertTypeAlias(decl))
                is VariableDeclaration -> sb.append(convertVariable(decl, file.moduleName))
                is EnumDeclaration -> sb.append(convertEnum(decl))
                is ClassDeclaration -> sb.append(convertClass(decl, file.moduleName))
            }
        }

        if (file.errors.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("// Parsing errors:")
            for (error in file.errors) {
                sb.appendLine("// - $error")
            }
        }

        return sb.toString()
    }

    /**
     * Converts a function declaration to a `@module` external binding.
     *
     * @param decl the function declaration
     * @param moduleName the JS module name for `@module` attribute
     * @return the ReScript external declaration string
     */
    fun convertFunction(
        decl: FunctionDeclaration,
        moduleName: String,
    ): String {
        val params = DtsTypeMapper.mapParameters(decl.parameters)
        val ret = DtsTypeMapper.mapType(decl.returnType)
        val name = DtsTypeMapper.toReScriptIdentifier(decl.name)
        return "@module(\"$moduleName\") external $name: $params => $ret = \"${decl.name}\"\n"
    }

    /**
     * Converts an interface declaration to a ReScript record type.
     *
     * @param decl the interface declaration
     * @return the ReScript type definition string
     */
    fun convertInterface(decl: InterfaceDeclaration): String {
        val sb = StringBuilder()
        val typeName = decl.name.replaceFirstChar { it.lowercase() }
        sb.appendLine("type $typeName = {")
        for (member in decl.members) {
            val fieldName = DtsTypeMapper.toReScriptIdentifier(member.name)
            val fieldType = DtsTypeMapper.mapType(member.type)
            if (member.optional) {
                sb.appendLine("  $fieldName?: $fieldType,")
            } else {
                sb.appendLine("  $fieldName: $fieldType,")
            }
        }
        sb.appendLine("}")
        return sb.toString()
    }

    /**
     * Converts a type alias to a ReScript type definition.
     *
     * @param decl the type alias declaration
     * @return the ReScript type definition string
     */
    fun convertTypeAlias(decl: TypeAliasDeclaration): String {
        val typeName = decl.name.replaceFirstChar { it.lowercase() }
        val typeStr = DtsTypeMapper.mapType(decl.type)

        // String literal unions get a special treatment
        if (decl.type is UnionType && DtsTypeMapper.isStringLiteralUnion(decl.type)) {
            return "type $typeName = $typeStr\n"
        }

        return "type $typeName = $typeStr\n"
    }

    /**
     * Converts a variable declaration to a `@module` external binding.
     *
     * @param decl the variable declaration
     * @param moduleName the JS module name for `@module` attribute
     * @return the ReScript external declaration string
     */
    fun convertVariable(
        decl: VariableDeclaration,
        moduleName: String,
    ): String {
        val typeStr = DtsTypeMapper.mapType(decl.type)
        val name = DtsTypeMapper.toReScriptIdentifier(decl.name)
        return "@module(\"$moduleName\") external $name: $typeStr = \"${decl.name}\"\n"
    }

    /**
     * Converts an enum to either a polymorphic variant type (string enum)
     * or a module with `@int` externals (numeric enum).
     *
     * @param decl the enum declaration
     * @return the ReScript type or module string
     */
    fun convertEnum(decl: EnumDeclaration): String {
        val typeName = decl.name.replaceFirstChar { it.lowercase() }
        if (decl.isStringEnum) {
            val variants = decl.members.joinToString(" | ") { "#\"${it.value ?: it.name}\"" }
            return "type $typeName = [$variants]\n"
        }

        // Numeric enum → module with integer values
        val sb = StringBuilder()
        sb.appendLine("module ${DtsTypeMapper.toModuleName(decl.name)} = {")
        sb.appendLine("  type t = int")
        for ((index, member) in decl.members.withIndex()) {
            val value = member.value ?: index.toString()
            sb.appendLine("  @inline let ${DtsTypeMapper.toReScriptIdentifier(member.name)}: t = $value")
        }
        sb.appendLine("}")
        return sb.toString()
    }

    /**
     * Converts a class declaration to a ReScript module with
     * `@new` constructor, `@get`/`@send` accessors, and methods.
     *
     * @param decl the class declaration
     * @param moduleName the JS module name for `@module` attribute
     * @return the ReScript module string
     */
    fun convertClass(
        decl: ClassDeclaration,
        moduleName: String,
    ): String {
        val sb = StringBuilder()
        sb.appendLine("module ${DtsTypeMapper.toModuleName(decl.name)} = {")
        sb.appendLine("  type t")
        sb.appendLine()

        // Constructors
        for (ctor in decl.constructors) {
            val params = DtsTypeMapper.mapParameters(ctor.parameters)
            sb.appendLine("  @module(\"$moduleName\") @new external make: $params => t = \"${decl.name}\"")
        }
        if (decl.constructors.isEmpty()) {
            sb.appendLine("  @module(\"$moduleName\") @new external make: unit => t = \"${decl.name}\"")
        }

        // Properties
        for (prop in decl.properties) {
            val propName = DtsTypeMapper.toReScriptIdentifier(prop.name)
            val propType = DtsTypeMapper.mapType(prop.type)
            sb.appendLine("  @get external $propName: t => $propType = \"${prop.name}\"")
        }

        // Methods
        for (method in decl.methods) {
            val methodName = DtsTypeMapper.toReScriptIdentifier(method.name)
            val ret = DtsTypeMapper.mapType(method.returnType)
            if (method.parameters.isEmpty()) {
                sb.appendLine("  @send external $methodName: t => $ret = \"${method.name}\"")
            } else {
                val paramTypes =
                    method.parameters.joinToString(", ") { param ->
                        val label = DtsTypeMapper.toReScriptIdentifier(param.name)
                        val type = DtsTypeMapper.mapType(param.type)
                        if (param.optional) "(~$label: $type=?)" else "(~$label: $type)"
                    }
                sb.appendLine("  @send external $methodName: (t, $paramTypes) => $ret = \"${method.name}\"")
            }
        }

        sb.appendLine("}")
        return sb.toString()
    }
}
