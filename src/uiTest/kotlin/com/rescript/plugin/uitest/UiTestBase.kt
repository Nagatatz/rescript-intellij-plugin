package com.rescript.plugin.uitest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.Fixture
import com.intellij.remoterobot.search.locators.byXpath
import org.junit.jupiter.api.BeforeEach
import java.awt.image.BufferedImage
import java.io.File
import java.time.Duration
import javax.imageio.ImageIO

/**
 * Base class for UI tests using IntelliJ Remote-Robot.
 *
 * Provides common utilities for connecting to the IDE, waiting for readiness,
 * and taking screenshots. Subclasses should call [waitForIdeReady] before
 * interacting with the IDE.
 */
abstract class UiTestBase {
    /** Remote-Robot client connected to the IDE under test. */
    protected lateinit var remoteRobot: RemoteRobot

    /** Directory where screenshots are saved. */
    protected val screenshotDir: String
        get() = System.getProperty("screenshot.output.dir", "build/screenshots")

    /** Path to the sample ReScript project used for testing. */
    protected val testProjectPath: String
        get() = System.getProperty("test.project.path", "")

    companion object {
        private const val DEFAULT_PORT = "8082"
        private val CONNECTION_TIMEOUT = Duration.ofSeconds(10)

        /** Default timeout for waiting for IDE components to appear. */
        val COMPONENT_TIMEOUT: Duration = Duration.ofSeconds(30)

        /** Short pause to allow UI updates to render. */
        const val UI_RENDER_DELAY_MS = 1500L
    }

    /**
     * Ensures the Remote-Robot connection is established and the screenshot directory exists.
     *
     * Safe to call multiple times — only initializes on first invocation.
     * Called automatically before each test via [BeforeEach], and can also be
     * called from [BeforeAll][org.junit.jupiter.api.BeforeAll] in subclasses.
     */
    @BeforeEach
    fun connectToIde() {
        ensureConnected()
    }

    /**
     * Initializes the Remote-Robot connection if not already done.
     */
    protected fun ensureConnected() {
        if (!::remoteRobot.isInitialized) {
            val port = System.getProperty("robot-server.port", DEFAULT_PORT)
            remoteRobot = RemoteRobot("http://127.0.0.1:$port")
        }
        File(screenshotDir).mkdirs()
    }

    /**
     * Takes a full IDE screenshot and saves it with the given name.
     *
     * @param name the file name (without extension) for the screenshot
     * @return the saved screenshot file
     */
    protected fun takeScreenshot(name: String): File {
        val screenshot: BufferedImage = remoteRobot.getScreenshot()
        val file = File(screenshotDir, "$name.png")
        ImageIO.write(screenshot, "png", file)
        return file
    }

    /**
     * Takes a screenshot of a specific fixture and saves it.
     *
     * @param fixture the fixture to screenshot
     * @param name the file name (without extension) for the screenshot
     * @return the saved screenshot file
     */
    protected fun takeComponentScreenshot(
        fixture: Fixture,
        name: String,
    ): File {
        val screenshot: BufferedImage = fixture.getScreenshot()
        val file = File(screenshotDir, "$name.png")
        ImageIO.write(screenshot, "png", file)
        return file
    }

    /**
     * Waits until the IDE main window is visible and ready for interaction.
     *
     * @param timeout maximum time to wait for the IDE to become ready
     */
    protected fun waitForIdeReady(timeout: Duration = Duration.ofSeconds(60)) {
        ensureConnected()
        remoteRobot.find<ComponentFixture>(
            byXpath("//div[@class='IdeFrameImpl']"),
            timeout,
        )
    }

    /**
     * Pauses execution briefly to allow UI rendering to complete.
     *
     * @param delayMs time to pause in milliseconds
     */
    protected fun waitForRendering(delayMs: Long = UI_RENDER_DELAY_MS) {
        Thread.sleep(delayMs)
    }
}
