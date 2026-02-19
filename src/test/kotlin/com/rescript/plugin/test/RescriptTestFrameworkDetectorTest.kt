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

    @Test
    fun `TestFramework has correct ids`() {
        assertEquals("jest", TestFramework.JEST.id)
        assertEquals("vitest", TestFramework.VITEST.id)
        assertEquals("custom", TestFramework.CUSTOM.id)
    }

    @Test
    fun `detectFromPackageJsonContent returns null for no devDependencies or dependencies`() {
        val content = """{"name": "my-project", "version": "1.0.0"}"""
        assertNull(RescriptTestFrameworkDetector.detectFromPackageJsonContent(content))
    }

    @Test
    fun `detectFromPackageJsonContent returns vitest from dependencies not devDependencies`() {
        val content =
            """
            {
                "dependencies": {
                    "vitest": "^1.0.0"
                }
            }
            """.trimIndent()
        assertEquals(TestFramework.VITEST, RescriptTestFrameworkDetector.detectFromPackageJsonContent(content))
    }

    @Test
    fun `detectFromPackageJsonContent merges dependencies and devDependencies`() {
        val content =
            """
            {
                "dependencies": {
                    "react": "^18.0.0"
                },
                "devDependencies": {
                    "jest": "^29.0.0"
                }
            }
            """.trimIndent()
        assertEquals(TestFramework.JEST, RescriptTestFrameworkDetector.detectFromPackageJsonContent(content))
    }

    @Test
    fun `detectFromConfigFiles returns vitest for vitest config ts`() {
        val dir = createTempDirWithFiles("vitest.config.ts")
        assertEquals(TestFramework.VITEST, RescriptTestFrameworkDetector.detectFromConfigFiles(dir))
    }

    @Test
    fun `detectFromConfigFiles returns vitest for vitest config js`() {
        val dir = createTempDirWithFiles("vitest.config.js")
        assertEquals(TestFramework.VITEST, RescriptTestFrameworkDetector.detectFromConfigFiles(dir))
    }

    @Test
    fun `detectFromConfigFiles returns vitest for vitest config mts`() {
        val dir = createTempDirWithFiles("vitest.config.mts")
        assertEquals(TestFramework.VITEST, RescriptTestFrameworkDetector.detectFromConfigFiles(dir))
    }

    @Test
    fun `detectFromConfigFiles returns jest for jest config js`() {
        val dir = createTempDirWithFiles("jest.config.js")
        assertEquals(TestFramework.JEST, RescriptTestFrameworkDetector.detectFromConfigFiles(dir))
    }

    @Test
    fun `detectFromConfigFiles returns jest for jest config ts`() {
        val dir = createTempDirWithFiles("jest.config.ts")
        assertEquals(TestFramework.JEST, RescriptTestFrameworkDetector.detectFromConfigFiles(dir))
    }

    @Test
    fun `detectFromConfigFiles returns jest for jest config mjs`() {
        val dir = createTempDirWithFiles("jest.config.mjs")
        assertEquals(TestFramework.JEST, RescriptTestFrameworkDetector.detectFromConfigFiles(dir))
    }

    @Test
    fun `detectFromConfigFiles returns null when no config files`() {
        val dir = createTempDirWithFiles("package.json")
        assertNull(RescriptTestFrameworkDetector.detectFromConfigFiles(dir))
    }

    @Test
    fun `detectFromConfigFiles prefers vitest when both configs exist`() {
        val dir = createTempDirWithFiles("vitest.config.ts", "jest.config.js")
        assertEquals(TestFramework.VITEST, RescriptTestFrameworkDetector.detectFromConfigFiles(dir))
    }

    private fun createTempDirWithFiles(vararg fileNames: String): com.intellij.openapi.vfs.VirtualFile {
        val tempDir =
            java.nio.file.Files
                .createTempDirectory("test-framework-detector")
        for (name in fileNames) {
            java.nio.file.Files
                .createFile(tempDir.resolve(name))
        }
        val vDir =
            com.intellij.openapi.vfs.LocalFileSystem
                .getInstance()
                .refreshAndFindFileByNioFile(tempDir)!!
        vDir.refresh(false, true)
        return vDir
    }
}
