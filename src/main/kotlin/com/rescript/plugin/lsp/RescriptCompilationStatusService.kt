package com.rescript.plugin.lsp

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer

/**
 * Project-level service that holds the current ReScript compilation status
 * received from the LSP server via `rescript/compilationStatus` notifications.
 */
@Service(Service.Level.PROJECT)
class RescriptCompilationStatusService(
    @Suppress("unused") // Injected by IntelliJ Platform service container
    private val project: Project,
) {
    @Volatile
    var currentStatus: CompilationStatus = CompilationStatus.UNKNOWN
        private set

    private val listeners = mutableListOf<CompilationStatusListener>()

    fun updateStatus(status: CompilationStatus) {
        currentStatus = status
        listeners.forEach { it.statusChanged(status) }
    }

    fun addListener(
        disposable: Disposable,
        listener: CompilationStatusListener,
    ) {
        listeners.add(listener)
        Disposer.register(disposable) { listeners.remove(listener) }
    }

    fun interface CompilationStatusListener {
        fun statusChanged(status: CompilationStatus)
    }

    data class CompilationStatus(
        val status: String,
        val errorCount: Int,
        val warningCount: Int,
    ) {
        companion object {
            val UNKNOWN = CompilationStatus("unknown", 0, 0)
        }
    }

    companion object {
        fun getInstance(project: Project): RescriptCompilationStatusService = project.service()
    }
}
