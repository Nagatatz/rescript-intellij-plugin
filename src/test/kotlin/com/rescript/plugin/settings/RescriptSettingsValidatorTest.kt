package com.rescript.plugin.settings

import com.intellij.openapi.options.ConfigurationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class RescriptSettingsValidatorTest {
    @TempDir
    lateinit var tempDir: Path

    // --- LSP path ---

    @Test
    fun `validateLspPath accepts empty path`() {
        RescriptSettingsValidator.validateLspPath("")
    }

    @Test
    fun `validateLspPath throws when path does not exist`() {
        val missing = tempDir.resolve("missing.js").toString()
        val ex =
            assertThrows(ConfigurationException::class.java) {
                RescriptSettingsValidator.validateLspPath(missing)
            }
        assertTrue(ex.text.startsWith("Language server path does not exist: $missing."))
        assertTrue(ex.text.contains("npm install @rescript/language-server"))
    }

    @Test
    fun `validateLspPath accepts existing js file even when not executable`() {
        val file = makeFile("server.js", executable = false)
        RescriptSettingsValidator.validateLspPath(file.absolutePath)
    }

    @Test
    fun `validateLspPath throws when non-js file is not executable`() {
        val file = makeFile("server", executable = false)
        val ex =
            assertThrows(ConfigurationException::class.java) {
                RescriptSettingsValidator.validateLspPath(file.absolutePath)
            }
        assertTrue(ex.text.startsWith("Language server path is not an executable file: ${file.absolutePath}."))
        assertTrue(ex.text.contains("chmod +x"))
    }

    @Test
    fun `validateLspPath accepts non-js file when executable`() {
        val file = makeFile("server", executable = true)
        RescriptSettingsValidator.validateLspPath(file.absolutePath)
    }

    // --- Node path ---

    @Test
    fun `validateNodePath accepts empty path`() {
        RescriptSettingsValidator.validateNodePath("")
    }

    @Test
    fun `validateNodePath throws when path does not exist`() {
        val missing = tempDir.resolve("missing-node").toString()
        val ex =
            assertThrows(ConfigurationException::class.java) {
                RescriptSettingsValidator.validateNodePath(missing)
            }
        assertTrue(ex.text.startsWith("Node.js interpreter path does not exist: $missing."))
        assertTrue(ex.text.contains("https://nodejs.org"))
    }

    @Test
    fun `validateNodePath throws when file is not executable`() {
        val file = makeFile("node", executable = false)
        val ex =
            assertThrows(ConfigurationException::class.java) {
                RescriptSettingsValidator.validateNodePath(file.absolutePath)
            }
        assertEquals(
            "Node.js interpreter path is not an executable file: ${file.absolutePath}. " +
                "Ensure the file has execute permissions (chmod +x on Unix).",
            ex.text,
        )
    }

    @Test
    fun `validateNodePath accepts executable file`() {
        val file = makeFile("node", executable = true)
        RescriptSettingsValidator.validateNodePath(file.absolutePath)
    }

    // --- ReScript binary ---

    @Test
    fun `validateRescriptBinaryPath accepts empty path`() {
        RescriptSettingsValidator.validateRescriptBinaryPath("")
    }

    @Test
    fun `validateRescriptBinaryPath throws when path does not exist`() {
        val missing = tempDir.resolve("rescript").toString()
        val ex =
            assertThrows(ConfigurationException::class.java) {
                RescriptSettingsValidator.validateRescriptBinaryPath(missing)
            }
        assertTrue(ex.text.startsWith("ReScript binary path does not exist: $missing."))
        assertTrue(ex.text.contains("npm install rescript"))
    }

    @Test
    fun `validateRescriptBinaryPath accepts existing file without executable bit`() {
        val file = makeFile("rescript", executable = false)
        RescriptSettingsValidator.validateRescriptBinaryPath(file.absolutePath)
    }

    // --- Platform path ---

    @Test
    fun `validatePlatformPath accepts empty path`() {
        RescriptSettingsValidator.validatePlatformPath("")
    }

    @Test
    fun `validatePlatformPath throws when path does not exist`() {
        val missing = tempDir.resolve("platform").toString()
        val ex =
            assertThrows(ConfigurationException::class.java) {
                RescriptSettingsValidator.validatePlatformPath(missing)
            }
        assertTrue(ex.text.startsWith("Platform path does not exist: $missing."))
    }

    @Test
    fun `validatePlatformPath accepts existing directory`() {
        val dir = tempDir.resolve("platform").toFile()
        dir.mkdirs()
        RescriptSettingsValidator.validatePlatformPath(dir.absolutePath)
    }

    // --- Runtime path ---

    @Test
    fun `validateRuntimePath accepts empty path`() {
        RescriptSettingsValidator.validateRuntimePath("")
    }

    @Test
    fun `validateRuntimePath throws when path does not exist`() {
        val missing = tempDir.resolve("runtime").toString()
        val ex =
            assertThrows(ConfigurationException::class.java) {
                RescriptSettingsValidator.validateRuntimePath(missing)
            }
        assertTrue(ex.text.startsWith("Runtime path does not exist: $missing."))
    }

    @Test
    fun `validateRuntimePath accepts existing directory`() {
        val dir = tempDir.resolve("runtime").toFile()
        dir.mkdirs()
        RescriptSettingsValidator.validateRuntimePath(dir.absolutePath)
    }

    private fun makeFile(
        name: String,
        executable: Boolean,
    ): File {
        val f = tempDir.resolve(name).toFile()
        f.writeText("")
        f.setExecutable(executable, false)
        return f
    }

    // Read the message via Throwable so the compiler resolves the non-deprecated base getter.
    private val ConfigurationException.text: String
        get() = (this as Throwable).message!!
}
