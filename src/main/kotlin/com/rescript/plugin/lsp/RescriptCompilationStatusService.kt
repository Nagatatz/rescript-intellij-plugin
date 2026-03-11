package com.rescript.plugin.lsp

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Project-level service that holds the current ReScript compilation status
 * received from the LSP server via `rescript/compilationStatus` and
 * `rescript/compilationFinished` notifications.
 */
@Service(Service.Level.PROJECT)
class RescriptCompilationStatusService(
    @Suppress("unused") // Injected by IntelliJ Platform service container
    private val project: Project,
) {
    @Volatile
    var currentStatus: CompilationStatus = CompilationStatus.UNKNOWN
        private set

    private val listeners = CopyOnWriteArrayList<CompilationStatusListener>()
    private val finishedListeners = CopyOnWriteArrayList<CompilationFinishedListener>()

    /**
     * Updates the current compilation status and notifies all registered listeners.
     *
     * @param status the new compilation status received from the LSP server
     */
    fun updateStatus(status: CompilationStatus) {
        currentStatus = status
        listeners.forEach { it.statusChanged(status) }
    }

    /**
     * Notifies all registered finished listeners that compilation has completed.
     *
     * @param params the compilation finished parameters from the LSP server
     */
    fun notifyCompilationFinished(params: RescriptLsp4jClient.CompilationFinishedParams) {
        finishedListeners.forEach { it.compilationFinished(params) }
    }

    /**
     * Registers a listener that is notified on compilation status changes.
     *
     * @param disposable the parent disposable; the listener is removed when disposed
     * @param listener the callback to invoke on status changes
     */
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

    fun interface CompilationFinishedListener {
        fun compilationFinished(params: RescriptLsp4jClient.CompilationFinishedParams)
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
