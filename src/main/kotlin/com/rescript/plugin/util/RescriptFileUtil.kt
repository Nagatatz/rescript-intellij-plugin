package com.rescript.plugin.util

import com.intellij.openapi.vfs.VirtualFile

/**
 * Utility functions for ReScript file extension checks and counterpart file lookup.
 *
 * Centralizes `.res`/`.resi` extension checks and interface-implementation
 * file pairing logic that was previously duplicated across 20+ files.
 */
object RescriptFileUtil {
    const val RES_EXTENSION = "res"
    const val RESI_EXTENSION = "resi"

    @JvmField
    val RESCRIPT_EXTENSIONS: Set<String> = setOf(RES_EXTENSION, RESI_EXTENSION)

    /** Returns true if the file has a `.res` or `.resi` extension. */
    fun isRescriptFile(file: VirtualFile): Boolean = file.extension in RESCRIPT_EXTENSIONS

    /** Returns true if the file has a `.res` extension. */
    fun isResFile(file: VirtualFile): Boolean = file.extension == RES_EXTENSION

    /** Returns true if the file has a `.resi` extension. */
    fun isResiFile(file: VirtualFile): Boolean = file.extension == RESI_EXTENSION

    /** Returns true if the file name ends with `.res` or `.resi`. */
    fun isRescriptFileName(fileName: String): Boolean =
        fileName.endsWith(".$RES_EXTENSION") || fileName.endsWith(".$RESI_EXTENSION")

    /** Returns true if the file name ends with `.res`. */
    fun isResFileName(fileName: String): Boolean = fileName.endsWith(".$RES_EXTENSION")

    /** Returns true if the file name ends with `.resi`. */
    fun isResiFileName(fileName: String): Boolean = fileName.endsWith(".$RESI_EXTENSION")

    /**
     * Finds the counterpart file for a ReScript file (`.res` <-> `.resi`).
     *
     * @param file a `.res` or `.resi` file
     * @return the counterpart file in the same directory, or null if not found
     */
    fun findCounterpartFile(file: VirtualFile): VirtualFile? {
        val targetExt =
            when (file.extension) {
                RES_EXTENSION -> RESI_EXTENSION
                RESI_EXTENSION -> RES_EXTENSION
                else -> return null
            }
        return file.parent?.findChild("${file.nameWithoutExtension}.$targetExt")
    }

    /**
     * Finds the `.resi` interface file corresponding to a `.res` file.
     *
     * @param resFile a `.res` source file
     * @return the `.resi` file in the same directory, or null if not found
     */
    fun findInterfaceFile(resFile: VirtualFile): VirtualFile? {
        if (resFile.extension != RES_EXTENSION) return null
        return resFile.parent?.findChild("${resFile.nameWithoutExtension}.$RESI_EXTENSION")
    }
}
