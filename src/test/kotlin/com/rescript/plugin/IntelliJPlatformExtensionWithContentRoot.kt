package com.rescript.plugin

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.runInEdtAndWait
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path

/**
 * JUnit 5 extension that builds a heavy IDE fixture rooted at a real
 * temp directory.
 *
 * Unlike [IntelliJPlatformExtension] (which uses a light descriptor
 * with no content root), this extension creates a project on the
 * actual file system through
 * [IdeaTestFixtureFactory.createFixtureBuilder]. As a result, files
 * added via `myFixture.addFileToProject(...)` are picked up by
 * [com.intellij.psi.search.FileTypeIndex] and
 * [com.intellij.psi.search.FilenameIndex] — exactly what tests for
 * scanner/finder integration need.
 *
 * Cost: each test method spends roughly 3–10 seconds on fixture
 * setup and tear-down, compared to ~0.5–2 seconds for the light
 * extension. Use this only when populated indexes are required.
 *
 * Existing tests on [IntelliJPlatformExtension] are unaffected.
 */
class IntelliJPlatformExtensionWithContentRoot :
    BeforeEachCallback,
    AfterEachCallback,
    InvocationInterceptor {
    private var fixture: CodeInsightTestFixture? = null
    private var tempDir: Path? = null

    override fun beforeEach(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance

        val name = sanitize(context.displayName)
        val tmp = Files.createTempDirectory("rescript-test-$name-")
        tempDir = tmp

        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        // Three-argument overload: project name, base path, and the
        // `isDirectoryBased` flag (false → file-based project storage,
        // which is what we want for an ad-hoc temp dir).
        val builder = factory.createFixtureBuilder(name, tmp, false)
        val codeInsightFixture = factory.createCodeInsightFixture(builder.fixture)
        codeInsightFixture.setUp()

        fixture = codeInsightFixture

        injectField(testInstance, "myFixture", codeInsightFixture)
        injectField(testInstance, "project", codeInsightFixture.project)
    }

    override fun afterEach(context: ExtensionContext) {
        try {
            fixture?.tearDown()
        } finally {
            fixture = null
            tempDir?.toFile()?.deleteRecursively()
            tempDir = null
        }
    }

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void?>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext,
    ) {
        runInEdtAndWait { invocation.proceed() }
    }

    /**
     * Strips characters that are unsafe for filesystem paths so the
     * temp directory name stays readable and portable across OSes.
     */
    private fun sanitize(displayName: String): String = displayName.replace(Regex("[^A-Za-z0-9_.-]"), "_").take(48)

    private fun injectField(
        testInstance: Any,
        fieldName: String,
        value: Any,
    ) {
        try {
            val field = testInstance::class.java.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(testInstance, value)
        } catch (_: NoSuchFieldException) {
            // Field not declared in test class — skip injection.
        }
    }
}
