package com.rescript.plugin

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.runInEdtAndWait
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

/**
 * JUnit 5 extension that replaces [com.intellij.testFramework.fixtures.BasePlatformTestCase].
 *
 * Manages the lifecycle of a [CodeInsightTestFixture] using
 * [IdeaTestFixtureFactory], providing the same test infrastructure
 * (light project, PSI, editors) without inheriting from JUnit 3's TestCase.
 *
 * Test methods are automatically dispatched to the EDT (Event Dispatch Thread)
 * to match the threading model of [com.intellij.testFramework.fixtures.BasePlatformTestCase].
 *
 * Usage:
 * ```
 * @ExtendWith(IntelliJPlatformExtension::class)
 * class MyTest {
 *     private lateinit var myFixture: CodeInsightTestFixture
 *     private lateinit var project: Project
 *
 *     @Test fun testSomething() {
 *         myFixture.configureByText("test.res", "let x = 1")
 *     }
 * }
 * ```
 *
 * The extension injects values into `myFixture` and `project` fields
 * via reflection before each test and tears down afterwards.
 *
 * For tests that need a custom test data path, declare a `testDataPath` field:
 * ```
 * private val testDataPath: String = "src/test/testData/folding"
 * ```
 *
 * @see CodeInsightTestFixture
 * @see IdeaTestFixtureFactory
 */
class IntelliJPlatformExtension :
    BeforeEachCallback,
    AfterEachCallback,
    InvocationInterceptor {
    private var fixture: CodeInsightTestFixture? = null

    override fun beforeEach(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance

        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val builder =
            factory.createLightFixtureBuilder(
                LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR,
                context.displayName,
            )
        val codeInsightFixture = factory.createCodeInsightFixture(builder.fixture)

        // Check if test class has a testDataPath field
        val testDataPath = findTestDataPath(testInstance)
        if (testDataPath != null) {
            codeInsightFixture.testDataPath = testDataPath
        }

        codeInsightFixture.setUp()
        fixture = codeInsightFixture

        // Inject myFixture field
        injectField(testInstance, "myFixture", codeInsightFixture)

        // Inject project field
        injectField(testInstance, "project", codeInsightFixture.project)
    }

    override fun afterEach(context: ExtensionContext) {
        try {
            fixture?.tearDown()
        } finally {
            fixture = null
        }
    }

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void?>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext,
    ) {
        // Run test methods on EDT, matching BasePlatformTestCase behavior
        runInEdtAndWait {
            invocation.proceed()
        }
    }

    private fun findTestDataPath(testInstance: Any): String? =
        try {
            val field = testInstance::class.java.getDeclaredField("testDataPath")
            field.isAccessible = true
            field.get(testInstance) as? String
        } catch (_: NoSuchFieldException) {
            null
        }

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
            // Field not declared in test class — skip injection
        }
    }
}
