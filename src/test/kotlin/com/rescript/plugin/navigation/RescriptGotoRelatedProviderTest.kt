package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.rescript.plugin.IntelliJPlatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(IntelliJPlatformExtension::class)
class RescriptGotoRelatedProviderTest {
    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var project: Project

    private val provider = RescriptGotoRelatedProvider()

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    // Tests below require project.guessProjectDir() to return the fixture's
    // temp dir. This works with BasePlatformTestCase but not with our JUnit 5
    // Extension due to VFS protocol mismatch (temp:// vs file://).
    // TODO: Re-enable when IntelliJ Platform provides JUnit 5 BasePlatformTestCase alternative.

    @Disabled("Requires BasePlatformTestCase's guessProjectDir() setup")
    @Test
    fun testResFileWithGeneratedBsJs() {
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

    @Disabled("Requires BasePlatformTestCase's guessProjectDir() setup")
    @Test
    fun testResFileWithGeneratedMjs() {
        val resFile = myFixture.addFileToProject("MjsMod.res", "let x = 1")
        myFixture.addFileToProject("lib/js/MjsMod.mjs", "var x = 1;")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, resFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        val fileNames = items.map { it.element?.containingFile?.name }
        assertTrue("MjsMod.mjs" in fileNames)
    }

    @Disabled("Requires BasePlatformTestCase's guessProjectDir() setup")
    @Test
    fun testResFileWithGeneratedPlainJs() {
        val resFile = myFixture.addFileToProject("JsMod.res", "let x = 1")
        myFixture.addFileToProject("lib/js/JsMod.js", "var x = 1;")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, resFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        val fileNames = items.map { it.element?.containingFile?.name }
        assertTrue("JsMod.js" in fileNames)
    }

    @Disabled("Requires BasePlatformTestCase's guessProjectDir() setup")
    @Test
    fun testResFileInSubdirectoryWithGeneratedJs() {
        val resFile = myFixture.addFileToProject("src/components/Button.res", "let make = () => <div/>")
        myFixture.addFileToProject("lib/js/src/components/Button.bs.js", "var make = function() {};")

        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .add(CommonDataKeys.VIRTUAL_FILE, resFile.virtualFile)
                .build()

        val items = provider.getItems(context)

        val fileNames = items.map { it.element?.containingFile?.name }
        assertTrue("Button.bs.js" in fileNames)
    }

    @Disabled("Requires BasePlatformTestCase's guessProjectDir() setup")
    @Test
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

    @Test
    fun testEmptyContextReturnsEmpty() {
        val context =
            SimpleDataContext
                .builder()
                .build()

        val items = provider.getItems(context)

        assertTrue(items.isEmpty())
    }

    @Test
    fun testContextWithoutFileReturnsEmpty() {
        val context =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
                .build()

        val items = provider.getItems(context)

        assertTrue(items.isEmpty())
    }
}
