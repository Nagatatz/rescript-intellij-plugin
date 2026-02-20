package com.rescript.plugin.analysis

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

class RescriptProblemHighlightFilterTest {
    @Test
    fun `instance can be created`() {
        val filter = RescriptProblemHighlightFilter()
        assertNotNull(filter)
    }

    @Test
    fun `isExcludedPath returns true for node_modules path`() {
        val file = stubVirtualFile("/project/node_modules/@rescript/core/src/Core.res")
        assertTrue(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns true for lib_bs path`() {
        val file = stubVirtualFile("/project/lib/bs/src/App.res")
        assertTrue(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns true for lib_ocaml path`() {
        val file = stubVirtualFile("/project/lib/ocaml/src/App.res")
        assertTrue(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns false for regular src path`() {
        val file = stubVirtualFile("/project/src/App.res")
        assertFalse(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns false for root level file`() {
        val file = stubVirtualFile("/project/Main.res")
        assertFalse(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns false for path containing node_modules without slashes`() {
        val file = stubVirtualFile("/project/my_node_modules_backup/App.res")
        assertFalse(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns true for deeply nested node_modules`() {
        val file = stubVirtualFile("/project/packages/app/node_modules/some-lib/index.res")
        assertTrue(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns false for lib directory without bs or ocaml`() {
        val file = stubVirtualFile("/project/lib/src/App.res")
        assertFalse(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns true for Windows-style node_modules path with forward slashes`() {
        val file = stubVirtualFile("C:/Users/dev/project/node_modules/pkg/index.res")
        assertTrue(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    @Test
    fun `isExcludedPath returns false for empty path`() {
        val file = stubVirtualFile("")
        assertFalse(RescriptProblemHighlightFilter.isExcludedPath(file))
    }

    /**
     * Creates a minimal VirtualFile stub that only provides getPath().
     * VirtualFile is an abstract class so we use a concrete anonymous subclass.
     */
    private fun stubVirtualFile(path: String): VirtualFile =
        object : VirtualFile() {
            override fun getName(): String = path.substringAfterLast('/')

            override fun getFileSystem(): VirtualFileSystem = throw UnsupportedOperationException()

            override fun getPath(): String = path

            override fun isWritable(): Boolean = false

            override fun isDirectory(): Boolean = false

            override fun isValid(): Boolean = true

            override fun getParent(): VirtualFile? = null

            override fun getChildren(): Array<VirtualFile>? = null

            override fun getOutputStream(
                requestor: Any?,
                newModificationStamp: Long,
                newTimeStamp: Long,
            ): OutputStream = throw UnsupportedOperationException()

            override fun contentsToByteArray(): ByteArray = ByteArray(0)

            override fun getTimeStamp(): Long = 0

            override fun getLength(): Long = 0

            override fun refresh(
                asynchronous: Boolean,
                recursive: Boolean,
                postRunnable: Runnable?,
            ) {}

            override fun getInputStream(): InputStream = throw UnsupportedOperationException()
        }
}
