package com.rescript.plugin.uitest.screenshot

import com.intellij.remoterobot.utils.keyboard
import com.rescript.plugin.uitest.UiTestBase
import com.rescript.plugin.uitest.fixtures.IdeFixtureUtils
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
    /**
     * Waits for the IDE to be fully loaded, dismisses notifications,
     * and initializes the IDE frame fixture for window-scoped screenshots.
     */
    @BeforeAll
    fun waitForIde() {
        waitForIdeReady()
        ideFrame = IdeFixtureUtils.findIdeFrame(remoteRobot)
        dismissNotifications()
    }

    @Test
    @Order(1)
    fun `01 - syntax highlighting`() {
        openFile("Demo.res")
        prepareForScreenshot()
        waitForRendering(3000)

        val file = takeScreenshot("01-syntax-highlighting")
        assertTrue(file.exists(), "Screenshot should be saved")
        assertTrue(file.length() > 0, "Screenshot should not be empty")
    }

    @Test
    @Order(2)
    fun `02 - code completion`() {
        openFile("Demo.res")
        prepareForScreenshot()

        // Navigate to end of file and type to trigger completion
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_META, KeyEvent.VK_END)
            enter()
            enterText("let z = Array.")
        }
        waitForRendering()

        // Trigger completion explicitly
        IdeFixtureUtils.triggerCompletion(remoteRobot)
        waitForRendering(3000)

        dismissNotifications()
        val file = takeScreenshot("02-code-completion")
        assertTrue(file.exists())

        // Dismiss completion popup and undo typed text
        remoteRobot.keyboard { key(KeyEvent.VK_ESCAPE) }
        repeat(20) {
            remoteRobot.keyboard { hotKey(KeyEvent.VK_META, KeyEvent.VK_Z) }
        }
    }

    @Test
    @Order(3)
    fun `03 - error lens`() {
        openFile("ErrorDemo.res")
        prepareForScreenshot()
        waitForRendering(5000) // Allow time for LSP diagnostics

        val file = takeScreenshot("03-error-lens")
        assertTrue(file.exists())
    }

    @Test
    @Order(4)
    fun `04 - inlay hints`() {
        openFile("Demo.res")
        prepareForScreenshot()
        waitForRendering(5000) // Allow time for LSP inlay hints to load

        val file = takeScreenshot("04-inlay-hints")
        assertTrue(file.exists())
    }

    @Test
    @Order(5)
    fun `05 - structure view`() {
        openFile("Demo.res")
        prepareForScreenshot()

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
        prepareForScreenshot()
        waitForRendering(5000) // Code Vision needs LSP hover data

        val file = takeScreenshot("06-code-vision")
        assertTrue(file.exists())
    }

    @Test
    @Order(7)
    fun `07 - jsx support`() {
        openFile("JsxDemo.res")
        prepareForScreenshot()
        waitForRendering(3000)

        val file = takeScreenshot("07-jsx-support")
        assertTrue(file.exists())
    }

    @Test
    @Order(8)
    fun `08 - project view`() {
        // Open a file first so the project tree shows the src directory context
        openFile("Demo.res")
        prepareForScreenshot()

        // Show Project tool window and select the open file in the tree
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_META, KeyEvent.VK_1)
        }
        waitForRendering(1000)

        // Use "Select Opened File" to focus on src/ in project tree
        try {
            remoteRobot.runJs(
                """
                importClass(com.intellij.openapi.application.ApplicationManager)
                importClass(com.intellij.openapi.project.ProjectManager)
                importClass(com.intellij.ide.SelectInContext)
                importClass(com.intellij.ide.actions.SelectInContextImpl)
                var project = ProjectManager.getInstance().getOpenProjects()[0]
                ApplicationManager.getApplication().invokeAndWait(new Runnable({
                    run: function() {
                        var action = com.intellij.openapi.actionSystem.ActionManager.getInstance()
                            .getAction("SelectInProjectView")
                        if (action != null) {
                            var event = com.intellij.openapi.actionSystem.AnActionEvent.createFromAnAction(
                                action,
                                null,
                                "MainMenu",
                                com.intellij.openapi.actionSystem.impl.SimpleDataContext.getProjectContext(project)
                            )
                            action.actionPerformed(event)
                        }
                    }
                }))
                """.trimIndent(),
            )
        } catch (_: Exception) {
            // Fallback: use keyboard shortcut Alt+F1, 1
            remoteRobot.keyboard {
                hotKey(KeyEvent.VK_ALT, KeyEvent.VK_F1)
            }
            waitForRendering(500)
            remoteRobot.keyboard { key(KeyEvent.VK_1) }
        }
        waitForRendering(2000)

        val file = takeScreenshot("08-project-view")
        assertTrue(file.exists())
    }

    @Test
    @Order(9)
    fun `09 - quick fix and intention`() {
        openFile("Demo.res")
        prepareForScreenshot()

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

        try {
            IdeFixtureUtils.triggerIntentionActions(remoteRobot)
            waitForRendering(2000)
        } catch (_: Exception) {
            // Intention actions may not be available without LSP
        }

        val file = takeScreenshot("09-quick-fix-intention")
        assertTrue(file.exists())

        // Dismiss the popup
        remoteRobot.keyboard { key(KeyEvent.VK_ESCAPE) }
    }

    @Test
    @Order(10)
    fun `10 - hover documentation`() {
        openFile("Demo.res")
        prepareForScreenshot()

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

        try {
            // Trigger Quick Documentation
            remoteRobot.keyboard { key(KeyEvent.VK_F1) }
            waitForRendering(2000)
        } catch (_: Exception) {
            // Documentation may not be available without LSP
        }

        val file = takeScreenshot("10-hover-documentation")
        assertTrue(file.exists())

        // Dismiss documentation popup
        remoteRobot.keyboard { key(KeyEvent.VK_ESCAPE) }
    }

    @Test
    @Order(11)
    fun `11 - repl`() {
        // Open REPL tool window on EDT with blocking call
        remoteRobot.runJs(
            """
            importClass(com.intellij.openapi.application.ApplicationManager)
            importClass(com.intellij.openapi.wm.ToolWindowManager)
            importClass(com.intellij.openapi.project.ProjectManager)
            var project = ProjectManager.getInstance().getOpenProjects()[0]
            ApplicationManager.getApplication().invokeAndWait(new Runnable({
                run: function() {
                    var toolWindow = ToolWindowManager.getInstance(project).getToolWindow("ReScript REPL")
                    if (toolWindow != null) {
                        toolWindow.show()
                    }
                }
            }))
            """.trimIndent(),
        )
        waitForRendering(2000)

        // Type a sample expression into the REPL input
        remoteRobot.keyboard {
            enterText("let greeting = \"Hello, ReScript!\"")
        }
        waitForRendering(1000)

        dismissNotifications()
        val file = takeScreenshot("11-repl")
        assertTrue(file.exists())
    }

    // ── Helper methods ──

    /**
     * Opens a file from the sample project by name.
     *
     * @param fileName the name of the file to open
     */
    private fun openFile(fileName: String) {
        IdeFixtureUtils.openFileByName(remoteRobot, fileName)
        waitForRendering()
    }

    /**
     * Prepares the IDE for a clean screenshot by dismissing all notifications
     * and editor notification bars.
     */
    private fun prepareForScreenshot() {
        dismissNotifications()
        dismissEditorNotificationBar()
    }

    /**
     * Dismisses all IDE notification balloons and error indicators.
     *
     * Expires pending notifications (including IDE internal error reports)
     * so they do not appear in screenshots.
     */
    private fun dismissNotifications() {
        try {
            remoteRobot.runJs(
                """
                importClass(com.intellij.openapi.application.ApplicationManager)
                importClass(com.intellij.notification.NotificationsManager)
                ApplicationManager.getApplication().invokeAndWait(new Runnable({
                    run: function() {
                        var notifications = NotificationsManager.getNotificationsManager()
                            .getNotificationsOfType(com.intellij.notification.Notification.class, null)
                        for (var i = 0; i < notifications.length; i++) {
                            notifications[i].expire()
                        }
                    }
                }))
                """.trimIndent(),
            )
        } catch (_: Exception) {
            // Best effort — notification API may vary across IDE versions
        }
        waitForRendering(500)
    }

    /**
     * Dismisses the editor notification bar (e.g. "Language Server not found").
     *
     * Closes any [com.intellij.ui.EditorNotificationPanel] visible at the top
     * of the current editor by programmatically hiding it.
     */
    private fun dismissEditorNotificationBar() {
        try {
            remoteRobot.runJs(
                """
                importClass(com.intellij.openapi.application.ApplicationManager)
                importClass(com.intellij.openapi.project.ProjectManager)
                importClass(com.intellij.openapi.fileEditor.FileEditorManager)
                importClass(com.intellij.ui.EditorNotificationProvider)
                ApplicationManager.getApplication().invokeAndWait(new Runnable({
                    run: function() {
                        var project = ProjectManager.getInstance().getOpenProjects()[0]
                        var editor = FileEditorManager.getInstance(project).getSelectedEditor()
                        if (editor != null) {
                            com.intellij.ui.EditorNotifications.getInstance(project).updateNotifications(
                                editor.getFile()
                            )
                        }
                    }
                }))
                """.trimIndent(),
            )
        } catch (_: Exception) {
            // Best effort
        }
        // Also try closing via keyboard (Escape dismisses some notification bars)
        try {
            remoteRobot.keyboard { key(KeyEvent.VK_ESCAPE) }
        } catch (_: Exception) {
            // Ignore
        }
        waitForRendering(300)
    }
}
