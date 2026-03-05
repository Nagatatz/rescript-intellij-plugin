package com.rescript.plugin.util

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.OutputStream

class RescriptFileUtilTest {
    /**
     * Creates a minimal VirtualFile stub with the given extension and name.
     * VirtualFile is an abstract class so we use a concrete anonymous subclass.
     */
    private fun stubFile(
        extension: String?,
        nameWithoutExt: String = "Foo",
        parent: VirtualFile? = null,
    ): VirtualFile =
        object : VirtualFile() {
            override fun getName(): String = if (extension != null) "$nameWithoutExt.$extension" else nameWithoutExt

            override fun getNameWithoutExtension(): String = nameWithoutExt

            override fun getExtension(): String? = extension

            override fun getFileSystem(): VirtualFileSystem = throw UnsupportedOperationException()

            override fun getPath(): String = "/stub/$name"

            override fun isWritable(): Boolean = false

            override fun isDirectory(): Boolean = false

            override fun isValid(): Boolean = true

            override fun getParent(): VirtualFile? = parent

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

    /**
     * Creates a directory stub that can resolve child files by name.
     */
    private fun stubParent(children: Map<String, VirtualFile>): VirtualFile =
        object : VirtualFile() {
            override fun getName(): String = "parent"

            override fun getFileSystem(): VirtualFileSystem = throw UnsupportedOperationException()

            override fun getPath(): String = "/stub/parent"

            override fun isWritable(): Boolean = false

            override fun isDirectory(): Boolean = true

            override fun isValid(): Boolean = true

            override fun getParent(): VirtualFile? = null

            override fun getChildren(): Array<VirtualFile> = children.values.toTypedArray()

            override fun findChild(name: String): VirtualFile? = children[name]

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

    // ── isRescriptFile ────────────────────────────────────────────

    @Test
    fun `isRescriptFile returns true for res`() {
        assertTrue(RescriptFileUtil.isRescriptFile(stubFile("res")))
    }

    @Test
    fun `isRescriptFile returns true for resi`() {
        assertTrue(RescriptFileUtil.isRescriptFile(stubFile("resi")))
    }

    @Test
    fun `isRescriptFile returns false for js`() {
        assertFalse(RescriptFileUtil.isRescriptFile(stubFile("js")))
    }

    @Test
    fun `isRescriptFile returns false for null extension`() {
        assertFalse(RescriptFileUtil.isRescriptFile(stubFile(null)))
    }

    // ── isResFile ─────────────────────────────────────────────────

    @Test
    fun `isResFile returns true for res`() {
        assertTrue(RescriptFileUtil.isResFile(stubFile("res")))
    }

    @Test
    fun `isResFile returns false for resi`() {
        assertFalse(RescriptFileUtil.isResFile(stubFile("resi")))
    }

    @Test
    fun `isResFile returns false for null extension`() {
        assertFalse(RescriptFileUtil.isResFile(stubFile(null)))
    }

    // ── isResiFile ────────────────────────────────────────────────

    @Test
    fun `isResiFile returns true for resi`() {
        assertTrue(RescriptFileUtil.isResiFile(stubFile("resi")))
    }

    @Test
    fun `isResiFile returns false for res`() {
        assertFalse(RescriptFileUtil.isResiFile(stubFile("res")))
    }

    @Test
    fun `isResiFile returns false for null extension`() {
        assertFalse(RescriptFileUtil.isResiFile(stubFile(null)))
    }

    // ── File name checks ──────────────────────────────────────────

    @Test
    fun `isRescriptFileName with res`() {
        assertTrue(RescriptFileUtil.isRescriptFileName("Foo.res"))
    }

    @Test
    fun `isRescriptFileName with resi`() {
        assertTrue(RescriptFileUtil.isRescriptFileName("Foo.resi"))
    }

    @Test
    fun `isRescriptFileName with js`() {
        assertFalse(RescriptFileUtil.isRescriptFileName("Foo.js"))
    }

    @Test
    fun `isResFileName returns true for res`() {
        assertTrue(RescriptFileUtil.isResFileName("Bar.res"))
    }

    @Test
    fun `isResFileName returns false for resi`() {
        assertFalse(RescriptFileUtil.isResFileName("Bar.resi"))
    }

    @Test
    fun `isResiFileName returns true for resi`() {
        assertTrue(RescriptFileUtil.isResiFileName("Bar.resi"))
    }

    @Test
    fun `isResiFileName returns false for res`() {
        assertFalse(RescriptFileUtil.isResiFileName("Bar.res"))
    }

    // ── findCounterpartFile ───────────────────────────────────────

    @Test
    fun `findCounterpartFile res finds resi`() {
        val resi = stubFile("resi")
        val parent = stubParent(mapOf("Foo.resi" to resi))
        val res = stubFile("res", "Foo", parent)
        assertSame(resi, RescriptFileUtil.findCounterpartFile(res))
    }

    @Test
    fun `findCounterpartFile resi finds res`() {
        val res = stubFile("res")
        val parent = stubParent(mapOf("Foo.res" to res))
        val resi = stubFile("resi", "Foo", parent)
        assertSame(res, RescriptFileUtil.findCounterpartFile(resi))
    }

    @Test
    fun `findCounterpartFile non-rescript returns null`() {
        assertNull(RescriptFileUtil.findCounterpartFile(stubFile("js")))
    }

    @Test
    fun `findCounterpartFile no counterpart returns null`() {
        val parent = stubParent(emptyMap())
        val res = stubFile("res", "Foo", parent)
        assertNull(RescriptFileUtil.findCounterpartFile(res))
    }

    // ── findInterfaceFile ─────────────────────────────────────────

    @Test
    fun `findInterfaceFile res finds resi`() {
        val resi = stubFile("resi")
        val parent = stubParent(mapOf("Foo.resi" to resi))
        val res = stubFile("res", "Foo", parent)
        assertSame(resi, RescriptFileUtil.findInterfaceFile(res))
    }

    @Test
    fun `findInterfaceFile resi returns null`() {
        assertNull(RescriptFileUtil.findInterfaceFile(stubFile("resi")))
    }

    @Test
    fun `findInterfaceFile no interface returns null`() {
        val parent = stubParent(emptyMap())
        val res = stubFile("res", "Bar", parent)
        assertNull(RescriptFileUtil.findInterfaceFile(res))
    }

    // ── Constants ─────────────────────────────────────────────────

    @Test
    fun `RESCRIPT_EXTENSIONS contains res and resi`() {
        assertEquals(setOf("res", "resi"), RescriptFileUtil.RESCRIPT_EXTENSIONS)
    }
}
