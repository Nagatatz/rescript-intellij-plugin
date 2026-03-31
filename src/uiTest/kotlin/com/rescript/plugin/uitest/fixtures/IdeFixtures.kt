package com.rescript.plugin.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.keyboard
import java.awt.event.KeyEvent
import java.time.Duration

/**
 * Fixture for the main IDE frame (IdeFrameImpl).
 *
 * Provides access to the editor, project tree, tool windows, and other
 * top-level IDE components.
 */
@FixtureName("IDE Frame")
@DefaultXpath("IdeFrameImpl", "//div[@class='IdeFrameImpl']")
class IdeFrameFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent,
) : CommonContainerFixture(remoteRobot, remoteComponent) {
    /**
     * Finds the editor component within the IDE frame.
     *
     * @param timeout maximum wait time for the editor to appear
     * @return the editor fixture
     */
    fun editor(timeout: Duration = Duration.ofSeconds(10)): ComponentFixture =
        find(byXpath("//div[@class='EditorComponentImpl']"), timeout)

    /**
     * Finds the project tool window tree.
     *
     * @param timeout maximum wait time for the tree to appear
     * @return the project tree fixture
     */
    fun projectTree(timeout: Duration = Duration.ofSeconds(10)): ComponentFixture =
        find(byXpath("//div[@class='ProjectViewTree']"), timeout)
}

/**
 * Fixture for a tool window content panel.
 *
 * Used to interact with tool windows like Structure View, REPL, etc.
 */
@FixtureName("Tool Window")
class ToolWindowFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent,
) : CommonContainerFixture(remoteRobot, remoteComponent)

/**
 * Utility functions for finding and interacting with IDE components.
 *
 * @see IdeFrameFixture
 */
object IdeFixtureUtils {
    /**
     * Finds the IDE main frame.
     *
     * @param remoteRobot the Remote-Robot client
     * @param timeout maximum wait time
     * @return the IDE frame fixture
     */
    fun findIdeFrame(
        remoteRobot: RemoteRobot,
        timeout: Duration = Duration.ofSeconds(30),
    ): IdeFrameFixture = remoteRobot.find(timeout)

    /**
     * Opens a file programmatically via FileEditorManager on the EDT.
     *
     * More reliable than the keyboard-shortcut approach (Cmd+Shift+O)
     * because it does not depend on Search Everywhere dialog timing.
     *
     * @param remoteRobot the Remote-Robot client
     * @param fileName the file name relative to the project's `src/` directory
     */
    fun openFileByName(
        remoteRobot: RemoteRobot,
        fileName: String,
    ) {
        remoteRobot.runJs(
            """
            importClass(com.intellij.openapi.application.ApplicationManager)
            importClass(com.intellij.openapi.project.ProjectManager)
            importClass(com.intellij.openapi.fileEditor.FileEditorManager)
            var project = ProjectManager.getInstance().getOpenProjects()[0]
            var baseDir = project.getBaseDir()
            var file = baseDir.findFileByRelativePath("src/$fileName")
            if (file == null) { throw new Error("File not found: src/$fileName") }
            ApplicationManager.getApplication().invokeAndWait(new Runnable({
                run: function() {
                    FileEditorManager.getInstance(project).openFile(file, true)
                }
            }))
            """.trimIndent(),
        )
        Thread.sleep(500)
    }

    /**
     * Triggers the code completion popup at the current cursor position.
     *
     * @param remoteRobot the Remote-Robot client
     */
    fun triggerCompletion(remoteRobot: RemoteRobot) {
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_SPACE)
        }
    }

    /**
     * Opens the Structure tool window (Cmd+7).
     *
     * @param remoteRobot the Remote-Robot client
     */
    fun openStructureView(remoteRobot: RemoteRobot) {
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_META, KeyEvent.VK_7)
        }
    }

    /**
     * Triggers the Intention Actions popup (Alt+Enter).
     *
     * @param remoteRobot the Remote-Robot client
     */
    fun triggerIntentionActions(remoteRobot: RemoteRobot) {
        remoteRobot.keyboard {
            hotKey(KeyEvent.VK_ALT, KeyEvent.VK_ENTER)
        }
    }
}
