package com.rescript.plugin.folding

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Integration test for ReScript code folding using the full IDE platform.
 *
 * Verifies that folding regions are correctly created for modules, block comments,
 * and nested structures by using IntelliJ's testFolding infrastructure with
 * marker-annotated test data files.
 */
@ExtendWith(IntelliJPlatformExtension::class)
class RescriptFoldingIntegrationTest {
    private lateinit var myFixture: CodeInsightTestFixture

    private val testDataPath: String = "src/test/testData/folding"

    @Test
    fun testBasicFolding() {
        myFixture.testFolding("$testDataPath/Basic.res")
    }
}
