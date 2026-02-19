package com.rescript.plugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * Project-level persistent settings for the ReScript plugin.
 *
 * Stores configuration for the LSP server path, Node.js interpreter path,
 * and incremental type-checking toggle. Persisted in `rescriptSettings.xml`
 * within the project's `.idea/` directory.
 */
@Service(Service.Level.PROJECT)
@State(name = "RescriptSettings", storages = [Storage("rescriptSettings.xml")])
class RescriptProjectSettings : PersistentStateComponent<RescriptProjectSettings.State> {
    private var state = State()

    class State {
        var lspServerPath: String = ""
        var nodePath: String = ""
        var incrementalTypecheckingEnabled: Boolean = true
        var removeUnusedOpensEnabled: Boolean = true
    }

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var lspServerPath: String
        get() = state.lspServerPath
        set(value) {
            state.lspServerPath = value
        }

    var nodePath: String
        get() = state.nodePath
        set(value) {
            state.nodePath = value
        }

    var incrementalTypecheckingEnabled: Boolean
        get() = state.incrementalTypecheckingEnabled
        set(value) {
            state.incrementalTypecheckingEnabled = value
        }

    var removeUnusedOpensEnabled: Boolean
        get() = state.removeUnusedOpensEnabled
        set(value) {
            state.removeUnusedOpensEnabled = value
        }

    companion object {
        fun getInstance(project: Project): RescriptProjectSettings = project.service()
    }
}
