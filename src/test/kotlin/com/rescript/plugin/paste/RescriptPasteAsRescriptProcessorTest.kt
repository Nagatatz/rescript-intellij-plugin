package com.rescript.plugin.paste

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RescriptPasteAsRescriptProcessorTest {
    @Test
    fun `looksLikeJavaScript detects const declaration`() {
        assertTrue(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("const x = 42;"))
    }

    @Test
    fun `looksLikeJavaScript detects function declaration`() {
        assertTrue(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("function foo() { return 1; }"))
    }

    @Test
    fun `looksLikeJavaScript detects var declaration`() {
        assertTrue(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("var x = 'hello';"))
    }

    @Test
    fun `looksLikeJavaScript rejects ReScript code`() {
        assertFalse(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("let x = 42"))
    }

    @Test
    fun `looksLikeJavaScript rejects empty string`() {
        assertFalse(RescriptPasteAsRescriptProcessor.looksLikeJavaScript(""))
    }

    @Test
    fun `convertLine converts const to let`() {
        assertEquals("let x = 42", RescriptPasteAsRescriptProcessor.convertLine("const x = 42;"))
    }

    @Test
    fun `convertLine converts var to let`() {
        assertEquals("let x = 42", RescriptPasteAsRescriptProcessor.convertLine("var x = 42;"))
    }

    @Test
    fun `convertLine converts triple equals`() {
        assertEquals("x == y", RescriptPasteAsRescriptProcessor.convertLine("x === y"))
    }

    @Test
    fun `convertLine converts not triple equals`() {
        assertEquals("x != y", RescriptPasteAsRescriptProcessor.convertLine("x !== y"))
    }

    @Test
    fun `convertLine converts console log`() {
        assertEquals("Js.log(x)", RescriptPasteAsRescriptProcessor.convertLine("console.log(x)"))
    }

    @Test
    fun `convertLine converts null to None`() {
        assertEquals("let x = None", RescriptPasteAsRescriptProcessor.convertLine("const x = null;"))
    }

    @Test
    fun `convertLine strips semicolons`() {
        assertEquals("let x = 1", RescriptPasteAsRescriptProcessor.convertLine("let x = 1;"))
    }

    @Test
    fun `convertJsToRescript converts multiple lines`() {
        val js =
            """
            const x = 42;
            console.log(x);
            """.trimIndent()
        val result = RescriptPasteAsRescriptProcessor.convertJsToRescript(js)
        assertTrue(result.contains("let x = 42"))
        assertTrue(result.contains("Js.log(x)"))
    }

    @Test
    fun `convertLine converts function declaration`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("function add(a, b) {")
        assertTrue(result.contains("let add = (a, b) => {"))
    }

    @Test
    fun `convertLine preserves comments`() {
        assertEquals("// this is a comment", RescriptPasteAsRescriptProcessor.convertLine("// this is a comment"))
    }

    // --- TypeScript type annotation tests ---

    @Test
    fun `convertLine strips variable type annotation`() {
        assertEquals(
            "let x = \"hello\"",
            RescriptPasteAsRescriptProcessor.convertLine("const x: string = \"hello\";"),
        )
    }

    @Test
    fun `convertLine strips complex variable type annotation`() {
        assertEquals(
            "let arr = [1, 2, 3]",
            RescriptPasteAsRescriptProcessor.convertLine("const arr: Array<number> = [1, 2, 3];"),
        )
    }

    @Test
    fun `convertLine strips function parameter type annotations`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("function foo(a: number, b: string) {")
        assertEquals("let foo = (a, b) => {", result)
    }

    @Test
    fun `convertLine strips return type annotation`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("function foo(): boolean {")
        assertEquals("let foo = () => {", result)
    }

    @Test
    fun `convertLine strips type assertion`() {
        assertEquals(
            "let x = value",
            RescriptPasteAsRescriptProcessor.convertLine("const x = value as string;"),
        )
    }

    @Test
    fun `convertLine strips optional parameter type annotation`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("function foo(x?: number) {")
        assertEquals("let foo = (x) => {", result)
    }

    // --- TypeScript declaration tests ---

    @Test
    fun `convertLine comments out interface declaration`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("interface Props { name: string }")
        assertEquals("// interface Props { name: string }", result)
    }

    @Test
    fun `convertLine comments out enum declaration`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("enum Color { Red, Green, Blue }")
        assertEquals("// enum Color { Red, Green, Blue }", result)
    }

    @Test
    fun `convertLine comments out export interface`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("export interface Props { name: string }")
        assertEquals("// export interface Props { name: string }", result)
    }

    @Test
    fun `convertLine comments out export enum`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("export enum Color { Red }")
        assertEquals("// export enum Color { Red }", result)
    }

    @Test
    fun `convertLine comments out export type`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("export type Props = { name: string }")
        assertEquals("// export type Props = { name: string }", result)
    }

    // --- JSX pattern tests ---

    @Test
    fun `convertLine converts conditional rendering`() {
        assertEquals(
            "{visible ? <div /> : React.null}",
            RescriptPasteAsRescriptProcessor.convertLine("{visible && <div />}"),
        )
    }

    @Test
    fun `convertLine converts dot map to Array map`() {
        assertEquals(
            "items->Array.map(x =>",
            RescriptPasteAsRescriptProcessor.convertLine("items.map(x =>"),
        )
    }

    @Test
    fun `convertLine converts dot filter to Array filter`() {
        assertEquals(
            "items->Array.filter(x =>",
            RescriptPasteAsRescriptProcessor.convertLine("items.filter(x =>"),
        )
    }

    @Test
    fun `convertLine converts dot forEach to Array forEach`() {
        assertEquals(
            "items->Array.forEach(x =>",
            RescriptPasteAsRescriptProcessor.convertLine("items.forEach(x =>"),
        )
    }

    @Test
    fun `convertLine adds spread warning comment`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("<Component {...props} />")
        assertTrue(result.contains("{...props}"))
        assertTrue(result.contains("/* spread is not supported in ReScript JSX */"))
    }

    // --- Detection logic tests ---

    @Test
    fun `looksLikeJavaScript detects TypeScript type annotation`() {
        assertTrue(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("const x: string = \"hello\""))
    }

    @Test
    fun `looksLikeJavaScript detects interface keyword`() {
        assertTrue(
            RescriptPasteAsRescriptProcessor.looksLikeJavaScript(
                """
                interface Props {
                  name: string
                }
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `looksLikeJavaScript detects React type patterns`() {
        assertTrue(
            RescriptPasteAsRescriptProcessor.looksLikeJavaScript(
                """
                const App: React.FC<Props> = () => {
                  return <div />
                }
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `looksLikeJavaScript detects enum keyword`() {
        assertTrue(RescriptPasteAsRescriptProcessor.looksLikeJavaScript("enum Color { Red, Green }"))
    }

    @Test
    fun `convertLine handles export async function`() {
        val result = RescriptPasteAsRescriptProcessor.convertLine("export async function fetchData(url: string) {")
        assertEquals("let fetchData = async (url) => {", result)
    }
}
