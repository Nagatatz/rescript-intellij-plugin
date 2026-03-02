package com.rescript.plugin.refactor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescriptExtractComponentHandlerTest {
    @Test
    fun `looksLikeJsx detects JSX element`() {
        assertTrue(RescriptExtractComponentHandler.looksLikeJsx("<div>hello</div>"))
    }

    @Test
    fun `looksLikeJsx detects self-closing JSX`() {
        assertTrue(RescriptExtractComponentHandler.looksLikeJsx("<MyComponent />"))
    }

    @Test
    fun `looksLikeJsx rejects non-JSX`() {
        assertFalse(RescriptExtractComponentHandler.looksLikeJsx("let x = 42"))
    }

    @Test
    fun `looksLikeJsx rejects empty string`() {
        assertFalse(RescriptExtractComponentHandler.looksLikeJsx(""))
    }

    @Test
    fun `extractProps finds variable expressions`() {
        val jsx = "<div>{name} is {age} years old</div>"
        val props = RescriptExtractComponentHandler.extractProps(jsx)
        assertTrue(props.contains("name"))
        assertTrue(props.contains("age"))
    }

    @Test
    fun `extractProps excludes uppercase names`() {
        val jsx = "<div>{React.string(name)}</div>"
        val props = RescriptExtractComponentHandler.extractProps(jsx)
        assertFalse(props.contains("React"))
    }

    @Test
    fun `extractProps excludes known globals`() {
        val jsx = "<div>{true} {false}</div>"
        val props = RescriptExtractComponentHandler.extractProps(jsx)
        assertFalse(props.contains("true"))
        assertFalse(props.contains("false"))
    }

    @Test
    fun `extractProps returns sorted unique names`() {
        val jsx = "<div>{count} {name} {count}</div>"
        val props = RescriptExtractComponentHandler.extractProps(jsx)
        assertEquals(listOf("count", "name"), props)
    }

    @Test
    fun `extractProps returns empty for no variables`() {
        val jsx = "<div>static text</div>"
        val props = RescriptExtractComponentHandler.extractProps(jsx)
        assertTrue(props.isEmpty())
    }

    @Test
    fun `generateComponent creates module with no props`() {
        val result = RescriptExtractComponentHandler.generateComponent("Greeting", "<h1>hello</h1>", emptyList())
        assertTrue(result.contains("module Greeting = {"))
        assertTrue(result.contains("@react.component"))
        assertTrue(result.contains("let make = ()"))
        assertTrue(result.contains("<h1>hello</h1>"))
    }

    @Test
    fun `generateComponent creates module with props`() {
        val result = RescriptExtractComponentHandler.generateComponent("Card", "<div>{name}</div>", listOf("name"))
        assertTrue(result.contains("let make = (~name)"))
    }

    @Test
    fun `generateComponent creates module with multiple props`() {
        val result =
            RescriptExtractComponentHandler.generateComponent(
                "Card",
                "<div>{name} {age}</div>",
                listOf("age", "name"),
            )
        assertTrue(result.contains("(~age, ~name)"))
    }

    @Test
    fun `generateUsage creates JSX element with no props`() {
        val result = RescriptExtractComponentHandler.generateUsage("Card", emptyList())
        assertEquals("<Card />", result)
    }

    @Test
    fun `generateUsage creates JSX element with props`() {
        val result = RescriptExtractComponentHandler.generateUsage("Card", listOf("name", "age"))
        assertEquals("<Card name={name} age={age} />", result)
    }
}
