package com.rescript.plugin.run

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

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

    @Test
    fun testPatternMatchesPathWithHyphens() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  my-project/src/my-component.res:5:10",
            )
        assertNotNull(match)
        assertEquals("my-project/src/my-component.res", match!!.groupValues[1])
    }

    @Test
    fun testPatternMatchesPathWithDots() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/App.Component.res:1:1",
            )
        assertNotNull(match)
        assertEquals("src/App.Component.res", match!!.groupValues[1])
    }

    @Test
    fun testPatternMatchesLargeLineNumbers() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/App.res:99999:12345",
            )
        assertNotNull(match)
        assertEquals("99999", match!!.groupValues[2])
        assertEquals("12345", match.groupValues[3])
    }

    @Test
    fun testPatternMatchesTwoReferencesOnOneLine() {
        val line = "  src/App.res:10:5  src/Utils.res:20:3"
        val matches = RescriptConsoleFilter.FILE_LINE_COL_PATTERN.findAll(line).toList()
        assertEquals(2, matches.size)
        assertEquals("src/App.res", matches[0].groupValues[1])
        assertEquals("src/Utils.res", matches[1].groupValues[1])
    }

    @Test
    fun testPatternDoesNotMatchTsFile() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/App.ts:10:5",
            )
        assertNull(match)
    }

    @Test
    fun testPatternMatchesDeeplyNestedResiFile() {
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/components/ui/buttons/PrimaryButton.resi:42:8",
            )
        assertNotNull(match)
        assertEquals("src/components/ui/buttons/PrimaryButton.resi", match!!.groupValues[1])
    }

    @Test
    fun testPatternMatchesRangeWithEndInfo() {
        // Pattern matches up to the first :line:col; the range extension (-15) is not captured
        val match =
            RescriptConsoleFilter.FILE_LINE_COL_PATTERN.find(
                "  src/App.res:10:5-15",
            )
        assertNotNull(match)
        assertEquals("10", match!!.groupValues[2])
        assertEquals("5", match.groupValues[3])
    }
}
