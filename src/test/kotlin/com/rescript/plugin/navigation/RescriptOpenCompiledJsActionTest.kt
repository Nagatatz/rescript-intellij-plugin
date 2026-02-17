package com.rescript.plugin.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RescriptOpenCompiledJsActionTest : BasePlatformTestCase() {
    private val action = RescriptOpenCompiledJsAction()

    // ── update() tests ──────────────────────────────────────────────

    fun testUpdateEnabledForResFile() {
        val resFile = myFixture.addFileToProject("Foo.res", "let x = 1")
        val event = createEvent(resFile.virtualFile)

        action.update(event)

        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testUpdateEnabledForResiFile() {
        val resiFile = myFixture.addFileToProject("Foo.resi", "let x: int")
        val event = createEvent(resiFile.virtualFile)

        action.update(event)

        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testUpdateDisabledForTxtFile() {
        val txtFile = myFixture.addFileToProject("test.txt", "hello")
        val event = createEvent(txtFile.virtualFile)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testUpdateDisabledForJsFile() {
        val jsFile = myFixture.addFileToProject("test.js", "var x = 1;")
        val event = createEvent(jsFile.virtualFile)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testUpdateDisabledWhenNoFile() {
        val event = createEvent(null)

        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }

    fun testActionUpdateThreadIsBGT() {
        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    // ── findCompiledJsFile() tests ──────────────────────────────────

    fun testFindCompiledJsFileBsJs() {
        val resFile = myFixture.addFileToProject("src/Module.res", "let x = 1")
        myFixture.addFileToProject("lib/js/src/Module.bs.js", "var x = 1;")

        val result = RescriptOpenCompiledJsAction.findCompiledJsFile(project, resFile.virtualFile)

        assertNotNull(result)
        assertEquals("Module.bs.js", result!!.name)
    }

    fun testFindCompiledJsFileMjs() {
        val resFile = myFixture.addFileToProject("src/Module2.res", "let x = 1")
        myFixture.addFileToProject("lib/js/src/Module2.mjs", "var x = 1;")

        val result = RescriptOpenCompiledJsAction.findCompiledJsFile(project, resFile.virtualFile)

        assertNotNull(result)
        assertEquals("Module2.mjs", result!!.name)
    }

    fun testFindCompiledJsFileJs() {
        val resFile = myFixture.addFileToProject("src/Module3.res", "let x = 1")
        myFixture.addFileToProject("lib/js/src/Module3.js", "var x = 1;")

        val result = RescriptOpenCompiledJsAction.findCompiledJsFile(project, resFile.virtualFile)

        assertNotNull(result)
        assertEquals("Module3.js", result!!.name)
    }

    fun testFindCompiledJsFilePriorityOrder() {
        // .bs.js should be preferred over .mjs and .js
        val resFile = myFixture.addFileToProject("src/Priority.res", "let x = 1")
        myFixture.addFileToProject("lib/js/src/Priority.bs.js", "var x = 1;")
        myFixture.addFileToProject("lib/js/src/Priority.mjs", "var x = 1;")
        myFixture.addFileToProject("lib/js/src/Priority.js", "var x = 1;")

        val result = RescriptOpenCompiledJsAction.findCompiledJsFile(project, resFile.virtualFile)

        assertNotNull(result)
        assertEquals("Priority.bs.js", result!!.name)
    }

    fun testFindCompiledJsFileFromResi() {
        val resiFile = myFixture.addFileToProject("src/Iface.resi", "let x: int")
        myFixture.addFileToProject("lib/js/src/Iface.bs.js", "var x = 1;")

        val result = RescriptOpenCompiledJsAction.findCompiledJsFile(project, resiFile.virtualFile)

        assertNotNull(result)
        assertEquals("Iface.bs.js", result!!.name)
    }

    fun testFindCompiledJsFileReturnsNullWhenNotFound() {
        val resFile = myFixture.addFileToProject("src/Missing.res", "let x = 1")

        val result = RescriptOpenCompiledJsAction.findCompiledJsFile(project, resFile.virtualFile)

        assertNull(result)
    }

    fun testFindCompiledJsFileNoLibJsDir() {
        val resFile = myFixture.addFileToProject("src/NoLib.res", "let x = 1")

        val result = RescriptOpenCompiledJsAction.findCompiledJsFile(project, resFile.virtualFile)

        assertNull(result)
    }

    // ── Helper ──────────────────────────────────────────────────────

    private fun createEvent(file: com.intellij.openapi.vfs.VirtualFile?): AnActionEvent {
        val builder =
            SimpleDataContext
                .builder()
                .add(CommonDataKeys.PROJECT, project)
        if (file != null) {
            builder.add(CommonDataKeys.VIRTUAL_FILE, file)
        }
        return AnActionEvent.createFromDataContext("test", Presentation(), builder.build())
    }
}
