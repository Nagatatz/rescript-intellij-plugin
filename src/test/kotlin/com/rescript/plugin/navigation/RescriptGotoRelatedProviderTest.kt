package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RescriptGotoRelatedProviderTest : BasePlatformTestCase() {
    private val provider = RescriptGotoRelatedProvider()

    fun testResFileShowsResiRelated() {
        val resFile = myFixture.addFileToProject("Foo.res", "let x = 1")
        myFixture.addFileToProject("Foo.resi", "let x: int")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, resFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        assertEquals(1, items.size)
        assertEquals("Foo.resi", items[0].element?.containingFile?.name)
    }

    fun testResiFileShowsResRelated() {
        myFixture.addFileToProject("Bar.res", "let y = 2")
        val resiFile = myFixture.addFileToProject("Bar.resi", "let y: int")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, resiFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        assertEquals(1, items.size)
        assertEquals("Bar.res", items[0].element?.containingFile?.name)
    }

    fun testNoRelatedFileReturnsEmpty() {
        val resFile = myFixture.addFileToProject("Alone.res", "let z = 3")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, resFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        assertTrue(items.isEmpty())
    }

    fun testNonRescriptFileReturnsEmpty() {
        val txtFile = myFixture.addFileToProject("test.txt", "hello")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, txtFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        assertTrue(items.isEmpty())
    }

    fun testResFileWithGeneratedJs() {
        val resFile = myFixture.addFileToProject("Module.res", "let x = 1")
        myFixture.addFileToProject("lib/js/Module.bs.js", "var x = 1;")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, resFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        val fileNames = items.map { it.element?.containingFile?.name }
        assertTrue("Module.bs.js" in fileNames)
    }

    fun testResFileWithResiAndJs() {
        val resFile = myFixture.addFileToProject("Full.res", "let x = 1")
        myFixture.addFileToProject("Full.resi", "let x: int")
        myFixture.addFileToProject("lib/js/Full.bs.js", "var x = 1;")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, resFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        val fileNames = items.map { it.element?.containingFile?.name }
        assertTrue("Full.resi" in fileNames)
        assertTrue("Full.bs.js" in fileNames)
    }
}
