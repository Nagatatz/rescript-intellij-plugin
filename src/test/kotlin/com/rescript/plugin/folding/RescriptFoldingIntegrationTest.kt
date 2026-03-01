package com.rescript.plugin.folding

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Integration test for ReScript code folding using the full IDE platform.
 *
 * Verifies that folding regions are correctly created for modules, block comments,
 * and nested structures by using IntelliJ's testFolding infrastructure with
 * marker-annotated test data files.
 */
class RescriptFoldingIntegrationTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/testData/folding"

    fun testBasicFolding() {
        myFixture.testFolding("$testDataPath/Basic.res")
    }
}
