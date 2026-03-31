package com.rescript.plugin.uitest.screenshot

import com.intellij.remoterobot.utils.keyboard
import com.rescript.plugin.uitest.UiTestBase
import com.rescript.plugin.uitest.fixtures.IdeFixtureUtils
import com.rescript.plugin.uitest.fixtures.IdeFrameFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import java.awt.event.KeyEvent

/**
 * Automated screenshot capture for JetBrains Marketplace listing.
 *
 * Each test method captures one screenshot demonstrating a key plugin feature.
 * Tests are ordered to minimize unnecessary file switching and UI state changes.
 * The IDE must be started with `./gradlew runIdeForUiTests` and the sample project
 * must be opened before running these tests.
 *
 * @see UiTestBase
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MarketplaceScreenshotTest : UiTestBase() {
    private lateinit var ideFrame: IdeFrameFixture

    /**
     * Waits for the IDE to be fully loaded before running any screenshot tests.
     */
    @BeforeAll
    fun waitForIde() {
        waitForIdeReady()
        ideFrame = IdeFixtureUtils.findIdeFrame(remoteRobot)
    }

    @Test
    @Order(1)
    fun `01 - syntax highlighting`() {
        openFile("Demo.res")
        waitForRendering(3000)

        val file = takeScreenshot("01-syntax-highlighting")
        assertTrue(file.exists(), "Screenshot should be saved")
        assertTrue(file.length() > 0, "Screenshot should not be empty")
    }

    @Test
    @Order(2)
    fun `02 - code completion`() {
        openFile("Demo.res")
        waitForRendering()

        // Navigate to end of file and type to trigger completion
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_META, KeyEvent.VK_END)
            enter()
            enterText("let z = Array.")
        }
        waitForRendering()

        // Trigger completion explicitly
        IdeFixtureUtils.triggerCompletion(remoteRobot)
        waitForRendering(2000)

        val file = takeScreenshot("02-code-completion")
        assertTrue(file.exists())

        // Undo the typed text to leave the file clean
        repeat(20) {
            remoteRobot.keyboard { hotKey(KeyEvent.VK_META, KeyEvent.VK_Z) }
        }
    }

    @Test
    @Order(3)
    fun `03 - error lens`() {
        openFile("ErrorDemo.res")
        waitForRendering(5000) // Allow time for LSP diagnostics

        val file = takeScreenshot("03-error-lens")
        assertTrue(file.exists())
    }

    @Test
    @Order(4)
    fun `04 - inlay hints`() {
        openFile("Demo.res")
        waitForRendering(3000) // Allow time for LSP inlay hints to load

        val file = takeScreenshot("04-inlay-hints")
        assertTrue(file.exists())
    }

    @Test
    @Order(5)
    fun `05 - structure view`() {
        openFile("Demo.res")
        waitForRendering()

        // Open Structure tool window
        IdeFixtureUtils.openStructureView(remoteRobot)
        waitForRendering(2000)

        val file = takeScreenshot("05-structure-view")
        assertTrue(file.exists())

        // Close Structure tool window
        IdeFixtureUtils.openStructureView(remoteRobot)
    }

    @Test
    @Order(6)
    fun `06 - code vision`() {
        openFile("Demo.res")
        waitForRendering(5000) // Code Vision needs LSP hover data

        val file = takeScreenshot("06-code-vision")
        assertTrue(file.exists())
    }

    @Test
    @Order(7)
    fun `07 - jsx support`() {
        openFile("JsxDemo.res")
        waitForRendering(3000)

        val file = takeScreenshot("07-jsx-support")
        assertTrue(file.exists())
    }

    @Test
    @Order(8)
    fun `08 - project view`() {
        // Ensure project tool window is visible
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_META, KeyEvent.VK_1)
        }
        waitForRendering(2000)

        // Try to expand the src directory in the project tree
        try {
            val projectTree = ideFrame.projectTree()
            takeComponentScreenshot(projectTree, "08-project-view")
        } catch (_: Exception) {
            // Fallback: take full IDE screenshot if tree not found
            takeScreenshot("08-project-view")
        }
    }

    @Test
    @Order(9)
    fun `09 - quick fix and intention`() {
        openFile("Demo.res")
        waitForRendering()

        // Place cursor on a symbol and trigger intentions
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_META, KeyEvent.VK_HOME)
        }
        waitForRendering(500)

        // Move to a let binding line
        repeat(14) {
            remoteRobot.keyboard { key(KeyEvent.VK_DOWN) }
        }
        remoteRobot.keyboard { hotKey(KeyEvent.VK_HOME) }
        waitForRendering(500)

        IdeFixtureUtils.triggerIntentionActions(remoteRobot)
        waitForRendering(2000)

        val file = takeScreenshot("09-quick-fix-intention")
        assertTrue(file.exists())

        // Dismiss the popup
        remoteRobot.keyboard { key(KeyEvent.VK_ESCAPE) }
    }

    @Test
    @Order(10)
    fun `10 - hover documentation`() {
        openFile("Demo.res")
        waitForRendering()

        // Use Ctrl+Shift+P (Expression Type) or Ctrl+Q (Quick Documentation)
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_META, KeyEvent.VK_HOME)
        }
        // Navigate to the `greet` function
        repeat(14) {
            remoteRobot.keyboard { key(KeyEvent.VK_DOWN) }
        }
        remoteRobot.keyboard { hotKey(KeyEvent.VK_HOME) }
        // Move to function name
        repeat(4) {
            remoteRobot.keyboard { key(KeyEvent.VK_RIGHT) }
        }
        waitForRendering(500)

        // Trigger Quick Documentation
        remoteRobot.keyboard { key(KeyEvent.VK_F1) }
        waitForRendering(2000)

        val file = takeScreenshot("10-hover-documentation")
        assertTrue(file.exists())

        // Dismiss documentation popup
        remoteRobot.keyboard { key(KeyEvent.VK_ESCAPE) }
    }

    @Test
    @Order(11)
    fun `11 - repl`() {
        // Open REPL tool window via Tools menu or action (must run on EDT)
        remoteRobot.runJs(
            """
            importClass(com.intellij.openapi.application.ApplicationManager)
            importClass(com.intellij.openapi.wm.ToolWindowManager)
            ApplicationManager.getApplication().invokeLater(new Runnable({
                run: function() {
                    const project = com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0]
                    const toolWindow = ToolWindowManager.getInstance(project).getToolWindow("ReScript REPL")
                    if (toolWindow != null) {
                        toolWindow.show()
                    }
                }
            }))
            """.trimIndent(),
        )
        waitForRendering(3000)

        val file = takeScreenshot("11-repl")
        assertTrue(file.exists())
    }

    // ── Helper methods ──

    /**
     * Opens a file from the sample project by name using the Go to File action.
     *
     * @param fileName the name of the file to open
     */
    private fun openFile(fileName: String) {
        IdeFixtureUtils.openFileByName(remoteRobot, fileName)
        waitForRendering()
    }
}
