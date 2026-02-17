package com.rescript.plugin.run

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptConsoleFilterProviderTest {
    @Test
    fun testPatternMatchesAbsolutePathUnix() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  /Users/dev/project/src/App.res:10:5-15",
            )
        assertNotNull(match)
        assertEquals("/Users/dev/project/src/App.res", match!!.groupValues[1])
        assertEquals("10", match.groupValues[2])
        assertEquals("5", match.groupValues[3])
    }

    @Test
    fun testPatternMatchesAbsolutePathWindows() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  C:\\Users\\dev\\project\\src\\App.res:10:5",
            )
        assertNotNull(match)
        assertEquals("C:\\Users\\dev\\project\\src\\App.res", match!!.groupValues[1])
        assertEquals("10", match.groupValues[2])
        assertEquals("5", match.groupValues[3])
    }

    @Test
    fun testPatternMatchesRelativePath() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/App.res:10:5-10:15",
            )
        assertNotNull(match)
        assertEquals("src/App.res", match!!.groupValues[1])
        assertEquals("10", match.groupValues[2])
        assertEquals("5", match.groupValues[3])
    }

    @Test
    fun testPatternMatchesResiFile() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/App.resi:20:3",
            )
        assertNotNull(match)
        assertEquals("src/App.resi", match!!.groupValues[1])
        assertEquals("20", match.groupValues[2])
        assertEquals("3", match.groupValues[3])
    }

    @Test
    fun testPatternDoesNotMatchNonResFile() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/App.js:10:5",
            )
        assertNull(match)
    }

    @Test
    fun testPatternDoesNotMatchLineWithoutNumbers() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/App.res",
            )
        assertNull(match)
    }

    @Test
    fun testPatternMatchesTypicalCompilerError() {
        val line = "  We've found a bug for you!"
        val match = RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(line)
        assertNull(match)
    }

    @Test
    fun testPatternMatchesNestedPath() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/components/deep/nested/MyComponent.res:100:1",
            )
        assertNotNull(match)
        assertEquals("src/components/deep/nested/MyComponent.res", match!!.groupValues[1])
        assertEquals("100", match.groupValues[2])
        assertEquals("1", match.groupValues[3])
    }

    @Test
    fun testPatternMatchesWithoutLeadingWhitespace() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "src/App.res:1:1",
            )
        assertNotNull(match)
        assertEquals("src/App.res", match!!.groupValues[1])
    }
}
