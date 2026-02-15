package com.rescript.plugin.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.rescript.plugin.RescriptIcons

class RescriptColorSettingsPage : ColorSettingsPage {
    companion object {
        // ── Lexer-based attributes ──
        private val LEXER_DESCRIPTORS =
            arrayOf(
                AttributesDescriptor("Keyword", RescriptSyntaxHighlighter.KEYWORD),
                AttributesDescriptor("String", RescriptSyntaxHighlighter.STRING),
                AttributesDescriptor("Number", RescriptSyntaxHighlighter.NUMBER),
                AttributesDescriptor("Comments//Line comment", RescriptSyntaxHighlighter.LINE_COMMENT),
                AttributesDescriptor("Comments//Block comment", RescriptSyntaxHighlighter.BLOCK_COMMENT),
                AttributesDescriptor("Operator", RescriptSyntaxHighlighter.OPERATOR),
                AttributesDescriptor("Braces and Operators//Braces", RescriptSyntaxHighlighter.BRACES),
                AttributesDescriptor("Braces and Operators//Brackets", RescriptSyntaxHighlighter.BRACKETS),
                AttributesDescriptor("Braces and Operators//Parentheses", RescriptSyntaxHighlighter.PARENS),
                AttributesDescriptor("Braces and Operators//Dot", RescriptSyntaxHighlighter.DOT),
                AttributesDescriptor("Braces and Operators//Comma", RescriptSyntaxHighlighter.COMMA),
                AttributesDescriptor("Braces and Operators//Semicolon", RescriptSyntaxHighlighter.SEMICOLON),
                AttributesDescriptor("Type argument", RescriptSyntaxHighlighter.TYPE_ARG),
                AttributesDescriptor("Polymorphic variant", RescriptSyntaxHighlighter.POLY_VARIANT),
                AttributesDescriptor("Module name", RescriptSyntaxHighlighter.MODULE_NAME),
                AttributesDescriptor("Annotation", RescriptSyntaxHighlighter.ANNOTATION),
                AttributesDescriptor("JSX tag", RescriptSyntaxHighlighter.MARKUP_TAG),
                AttributesDescriptor("JSX tag bracket", RescriptSyntaxHighlighter.MARKUP_TAG_BRACKET),
            )

        // ── Semantic token attributes (LSP) ──
        private val SEMANTIC_DESCRIPTORS =
            arrayOf(
                AttributesDescriptor("Semantic//Variable", RescriptSyntaxHighlighter.SEMANTIC_VARIABLE),
                AttributesDescriptor("Semantic//Type", RescriptSyntaxHighlighter.SEMANTIC_TYPE),
                AttributesDescriptor("Semantic//Namespace (module)", RescriptSyntaxHighlighter.SEMANTIC_NAMESPACE),
                AttributesDescriptor("Semantic//Enum member (variant)", RescriptSyntaxHighlighter.SEMANTIC_ENUM_MEMBER),
                AttributesDescriptor("Semantic//Property (record field)", RescriptSyntaxHighlighter.SEMANTIC_PROPERTY),
                AttributesDescriptor("Semantic//Interface (JSX tag)", RescriptSyntaxHighlighter.SEMANTIC_INTERFACE),
                AttributesDescriptor("Semantic//Operator", RescriptSyntaxHighlighter.SEMANTIC_OPERATOR),
                AttributesDescriptor("Semantic//Modifier (JSX bracket)", RescriptSyntaxHighlighter.SEMANTIC_MODIFIER),
            )

        private val ALL_DESCRIPTORS = LEXER_DESCRIPTORS + SEMANTIC_DESCRIPTORS

        // Tag → TextAttributesKey mapping for demo text
        private val TAG_MAP =
            mapOf(
                "var" to RescriptSyntaxHighlighter.SEMANTIC_VARIABLE,
                "typ" to RescriptSyntaxHighlighter.SEMANTIC_TYPE,
                "ns" to RescriptSyntaxHighlighter.SEMANTIC_NAMESPACE,
                "enum" to RescriptSyntaxHighlighter.SEMANTIC_ENUM_MEMBER,
                "prop" to RescriptSyntaxHighlighter.SEMANTIC_PROPERTY,
                "iface" to RescriptSyntaxHighlighter.SEMANTIC_INTERFACE,
                "sop" to RescriptSyntaxHighlighter.SEMANTIC_OPERATOR,
                "mod" to RescriptSyntaxHighlighter.SEMANTIC_MODIFIER,
            )

        //language=ReST
        private val DEMO_TEXT =
            """
// Line comment
/* Block comment */

@module("fs")
external <var>readFileSync</var>: string => string = "readFileSync"

let <var>greeting</var> = "hello"
let <var>count</var> = 42
let <var>pi</var> = 3.14

type <typ>color</typ> =
  | <enum>Red</enum>
  | <enum>Green</enum>
  | <enum>Blue</enum>

type <typ>user</typ> = {
  <prop>name</prop>: string,
  <prop>age</prop>: <typ>int</typ>,
}

module <ns>Utils</ns> = {
  let <var>add</var> = (<var>a</var>, <var>b</var>) => <var>a</var> + <var>b</var>
}

let <var>result</var> = switch <enum>Some</enum>("value") {
| <enum>Some</enum>(<var>v</var>) => <var>v</var>
| <enum>None</enum> => "default"
}

let <var>isSmall</var> = <var>count</var> <sop><</sop> 100

let <var>element</var> = <mod><</mod><iface>div</iface> className="app"<mod>></mod>
  {<ns>React</ns>.string(<var>greeting</var>)}
<mod><</mod><mod>/</mod><iface>div</iface><mod>></mod>

let <var>poly</var> = #success
            """.trimIndent()
    }

    override fun getDisplayName(): String = "ReScript"

    override fun getIcon() = RescriptIcons.FILE

    override fun getHighlighter() = RescriptSyntaxHighlighter()

    override fun getAttributeDescriptors() = ALL_DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = TAG_MAP
}
