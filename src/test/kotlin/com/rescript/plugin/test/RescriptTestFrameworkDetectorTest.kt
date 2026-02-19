package com.rescript.plugin.test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescriptTestFrameworkDetectorTest {
    @Test
    fun `detectFromPackageJsonContent returns JEST when jest is in devDependencies`() {
        val content =
            """
            {
                "devDependencies": {
                    "jest": "^29.0.0",
                    "rescript": "^11.0.0"
                }
            }
            """.trimIndent()
        assertEquals(TestFramework.JEST, RescriptTestFrameworkDetector.detectFromPackageJsonContent(content))
    }

    @Test
    fun `detectFromPackageJsonContent returns VITEST when vitest is in devDependencies`() {
        val content =
            """
            {
                "devDependencies": {
                    "vitest": "^1.0.0",
                    "rescript": "^11.0.0"
                }
            }
            """.trimIndent()
        assertEquals(TestFramework.VITEST, RescriptTestFrameworkDetector.detectFromPackageJsonContent(content))
    }

    @Test
    fun `detectFromPackageJsonContent prefers vitest over jest when both present`() {
        val content =
            """
            {
                "devDependencies": {
                    "jest": "^29.0.0",
                    "vitest": "^1.0.0"
                }
            }
            """.trimIndent()
        assertEquals(TestFramework.VITEST, RescriptTestFrameworkDetector.detectFromPackageJsonContent(content))
    }

    @Test
    fun `detectFromPackageJsonContent returns null when no test framework found`() {
        val content =
            """
            {
                "devDependencies": {
                    "rescript": "^11.0.0"
                }
            }
            """.trimIndent()
        assertNull(RescriptTestFrameworkDetector.detectFromPackageJsonContent(content))
    }

    @Test
    fun `detectFromPackageJsonContent returns null for empty JSON`() {
        assertNull(RescriptTestFrameworkDetector.detectFromPackageJsonContent("{}"))
    }

    @Test
    fun `detectFromPackageJsonContent returns null for invalid JSON`() {
        assertNull(RescriptTestFrameworkDetector.detectFromPackageJsonContent("not json"))
    }

    @Test
    fun `detectFromPackageJsonContent checks dependencies too`() {
        val content =
            """
            {
                "dependencies": {
                    "jest": "^29.0.0"
                }
            }
            """.trimIndent()
        assertEquals(TestFramework.JEST, RescriptTestFrameworkDetector.detectFromPackageJsonContent(content))
    }

    @Test
    fun `TestFramework has correct default commands`() {
        assertEquals("npx jest", TestFramework.JEST.defaultCommand)
        assertEquals("npx vitest run", TestFramework.VITEST.defaultCommand)
        assertEquals("", TestFramework.CUSTOM.defaultCommand)
    }

    @Test
    fun `TestFramework has correct display names`() {
        assertEquals("Jest", TestFramework.JEST.displayName)
        assertEquals("Vitest", TestFramework.VITEST.displayName)
        assertEquals("Custom", TestFramework.CUSTOM.displayName)
    }
}
