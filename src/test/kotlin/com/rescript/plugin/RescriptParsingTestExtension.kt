package com.rescript.plugin

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.runInEdtAndWait
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

/**
 * JUnit 5 extension that replaces [com.intellij.testFramework.ParsingTestCase]
 * for ReScript parser tests.
 *
 * Uses [IdeaTestFixtureFactory] to create a light project fixture with
 * proper application initialization, then obtains [PsiFileFactory] from
 * the project to parse ReScript source code.
 *
 * Injects a [ParsingTestHelper] instance into test classes for creating
 * and inspecting PSI files.
 *
 * @see ParsingTestHelper
 * @see RescriptParserDefinition
 */
class RescriptParsingTestExtension :
    BeforeEachCallback,
    AfterEachCallback,
    InvocationInterceptor {
    private var fixture: com.intellij.testFramework.fixtures.CodeInsightTestFixture? = null

    override fun beforeEach(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance

        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val builder =
            factory.createLightFixtureBuilder(
                LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR,
                context.displayName,
            )
        val codeInsightFixture = factory.createCodeInsightFixture(builder.fixture)
        codeInsightFixture.setUp()
        fixture = codeInsightFixture

        val project = codeInsightFixture.project
        val fileFactory = PsiFileFactory.getInstance(project)

        val helper =
            ParsingTestHelper(
                fileFactory = fileFactory,
                language = RescriptLanguage,
                fileExtension = "res",
            )

        // Inject helper
        injectField(testInstance, "parsingHelper", helper)
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
        // Run test methods on EDT, matching ParsingTestCase behavior
        runInEdtAndWait {
            invocation.proceed()
        }
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

/**
 * Helper object providing parsing utilities for JUnit 5 parser tests.
 *
 * Replaces the protected methods of [com.intellij.testFramework.ParsingTestCase]
 * with an injectable, composable API.
 */
class ParsingTestHelper(
    private val fileFactory: PsiFileFactory,
    private val language: com.intellij.lang.Language,
    private val fileExtension: String,
) {
    /**
     * Creates and parses a PSI file from the given source code.
     */
    fun parseCode(code: String): PsiFile {
        val file =
            fileFactory.createFileFromText(
                "test.$fileExtension",
                language,
                code,
            )
        // Force parsing
        file.node
        return file
    }

    /**
     * Finds all AST nodes of the given element type in the PSI tree.
     */
    fun findElements(
        file: PsiFile,
        type: com.intellij.psi.tree.IElementType,
    ): List<com.intellij.lang.ASTNode> {
        val result = mutableListOf<com.intellij.lang.ASTNode>()

        fun walk(node: com.intellij.lang.ASTNode) {
            if (node.elementType == type) result.add(node)
            var child = node.firstChildNode
            while (child != null) {
                walk(child)
                child = child.treeNext
            }
        }
        walk(file.node)
        return result
    }

    /**
     * Finds all ERROR_ELEMENT nodes in the PSI tree.
     */
    fun findErrors(file: PsiFile): List<com.intellij.lang.ASTNode> =
        findElements(file, com.intellij.psi.TokenType.ERROR_ELEMENT)

    /**
     * Asserts that parsing the given code produces exactly [expectedCount]
     * elements of the given [type].
     */
    fun assertHasElements(
        code: String,
        type: com.intellij.psi.tree.IElementType,
        expectedCount: Int,
    ) {
        val file = parseCode(code)
        val elements = findElements(file, type)
        org.junit.jupiter.api.Assertions.assertEquals(
            expectedCount,
            elements.size,
            "Expected $expectedCount $type in: $code",
        )
    }
}
