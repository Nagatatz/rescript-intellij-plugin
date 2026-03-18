package com.rescript.plugin.template

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [RescriptCreateFileAction]'s companion utility functions.
 *
 * Tests the file name capitalization logic that enforces ReScript module naming
 * conventions, and the action name formatting used in undo/redo history.
 *
 * @see RescriptCreateFileAction
 */
class RescriptCreateFileActionTest {
    @Test
    fun testCapitalizeFileNameWithLowercaseFirst() {
        assertEquals("MyModule", RescriptCreateFileAction.capitalizeFileName("myModule"))
    }

    @Test
    fun testCapitalizeFileNameAlreadyCapitalized() {
        assertEquals("MyModule", RescriptCreateFileAction.capitalizeFileName("MyModule"))
    }

    @Test
    fun testCapitalizeFileNameSingleChar() {
        assertEquals("A", RescriptCreateFileAction.capitalizeFileName("a"))
    }

    @Test
    fun testCapitalizeFileNameSingleUpperChar() {
        assertEquals("Z", RescriptCreateFileAction.capitalizeFileName("Z"))
    }

    @Test
    fun testCapitalizeFileNameAllLowercase() {
        assertEquals("Utils", RescriptCreateFileAction.capitalizeFileName("utils"))
    }

    @Test
    fun testCapitalizeFileNameWithUnderscore() {
        assertEquals("My_module", RescriptCreateFileAction.capitalizeFileName("my_module"))
    }

    @Test
    fun testCapitalizeFileNameWithNumbers() {
        assertEquals("Module2", RescriptCreateFileAction.capitalizeFileName("module2"))
    }

    @Test
    fun testCapitalizeFileNameStartingWithNumber() {
        // Numbers don't have uppercase; replaceFirstChar returns the char as-is
        assertEquals("2module", RescriptCreateFileAction.capitalizeFileName("2module"))
    }

    @Test
    fun testCapitalizeFileNameEmpty() {
        assertEquals("", RescriptCreateFileAction.capitalizeFileName(""))
    }

    @Test
    fun testFormatActionNameSimple() {
        assertEquals("Create ReScript File: MyModule", RescriptCreateFileAction.formatActionName("MyModule"))
    }

    @Test
    fun testFormatActionNameEmpty() {
        assertEquals("Create ReScript File: ", RescriptCreateFileAction.formatActionName(""))
    }

    @Test
    fun testFormatActionNameWithSpaces() {
        assertEquals("Create ReScript File: My Module", RescriptCreateFileAction.formatActionName("My Module"))
    }
}
