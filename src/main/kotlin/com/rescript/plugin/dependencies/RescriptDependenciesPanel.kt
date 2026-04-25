package com.rescript.plugin.dependencies

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.rescript.plugin.util.RescriptSecurityUtils
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * Panel that displays ReScript package dependencies in a tree view.
 *
 * Reads `rescript.json` to extract `dependencies`, `dev-dependencies`, and
 * `pinned-dependencies` (plus the legacy `bs-` aliases for older projects),
 * then resolves each package's installed version from
 * `node_modules/<pkg>/package.json`.
 *
 * @param project the current IntelliJ project
 */
class RescriptDependenciesPanel(
    private val project: Project,
) {
    private val log = logger<RescriptDependenciesPanel>()
    private val rootNode = DefaultMutableTreeNode("ReScript Dependencies")
    private val treeModel = DefaultTreeModel(rootNode)
    private val tree = Tree(treeModel)
    private val panel = JPanel(BorderLayout())

    val component: JComponent
        get() = panel

    init {
        panel.add(JBScrollPane(tree), BorderLayout.CENTER)
        loadDependencies()
    }

    /**
     * Loads dependencies from `rescript.json` and populates the tree model.
     *
     * Searches for `rescript.json` in the project base directory, parses it with Gson,
     * and creates tree nodes for each dependency category. Installed versions are resolved
     * from `node_modules/<pkg>/package.json`.
     */
    private fun loadDependencies() {
        rootNode.removeAllChildren()

        val basePath = project.basePath ?: return
        val rescriptJson =
            VirtualFileManager
                .getInstance()
                .findFileByUrl("file://$basePath/rescript.json") ?: return

        try {
            val content = String(rescriptJson.contentsToByteArray(), Charsets.UTF_8)
            val jsonObj =
                com.google.gson.JsonParser
                    .parseString(content)
                    .asJsonObject

            // Modern key names (current rescript.json schema). The legacy aliases
            // are still accepted by the compiler but emit deprecation warnings —
            // we display whichever the user's project happens to have.
            addDependencyCategory(jsonObj, "dependencies", basePath)
            addDependencyCategory(jsonObj, "dev-dependencies", basePath)
            addDependencyCategory(jsonObj, "pinned-dependencies", basePath)
            // Legacy key names (rescript < latest). Render only if the project
            // hasn't migrated yet, so we don't double-list dependencies.
            if (!jsonObj.has("dependencies")) {
                addDependencyCategory(jsonObj, "bs-dependencies", basePath)
            }
            if (!jsonObj.has("dev-dependencies")) {
                addDependencyCategory(jsonObj, "bs-dev-dependencies", basePath)
            }
        } catch (e: java.io.IOException) {
            log.debug("Failed to read rescript.json", e)
            rootNode.add(DefaultMutableTreeNode("Error reading rescript.json"))
        } catch (e: com.google.gson.JsonParseException) {
            log.debug("Invalid rescript.json format", e)
            rootNode.add(DefaultMutableTreeNode("Error reading rescript.json"))
        } catch (e: IllegalStateException) {
            log.debug("Unexpected rescript.json structure", e)
            rootNode.add(DefaultMutableTreeNode("Error reading rescript.json"))
        }

        treeModel.reload()
        expandAll()
    }

    /**
     * Adds a dependency category node to the tree if the category exists in rescript.json.
     *
     * @param jsonObj the parsed rescript.json object
     * @param category the dependency category key (e.g., "dependencies")
     * @param basePath the project base directory path
     */
    private fun addDependencyCategory(
        jsonObj: com.google.gson.JsonObject,
        category: String,
        basePath: String,
    ) {
        val deps = jsonObj.getAsJsonArray(category) ?: return
        if (deps.size() == 0) return

        val categoryNode = DefaultMutableTreeNode(category)
        for (dep in deps) {
            val pkgName = dep.asString
            // Validate package name to prevent path traversal via crafted rescript.json
            if (!RescriptSecurityUtils.isValidPackageName(pkgName)) continue
            val version = resolveVersion(basePath, pkgName)
            val label = if (version != null) "$pkgName ($version)" else pkgName
            categoryNode.add(DefaultMutableTreeNode(label))
        }
        rootNode.add(categoryNode)
    }

    /** Expands all tree nodes. */
    private fun expandAll() {
        var row = 0
        while (row < tree.rowCount) {
            tree.expandRow(row)
            row++
        }
    }

    companion object {
        /**
         * Resolves the installed version of a package from `node_modules/<pkg>/package.json`.
         *
         * @param basePath the project base directory path
         * @param pkgName the npm package name
         * @return the version string, or null if not found
         */
        internal fun resolveVersion(
            basePath: String,
            pkgName: String,
        ): String? {
            val pkgJsonPath = "$basePath/node_modules/$pkgName/package.json"
            val pkgJsonFile =
                VirtualFileManager.getInstance().findFileByUrl("file://$pkgJsonPath")
                    ?: return null
            return try {
                val content = String(pkgJsonFile.contentsToByteArray(), Charsets.UTF_8)
                val jsonObj =
                    com.google.gson.JsonParser
                        .parseString(content)
                        .asJsonObject
                jsonObj.get("version")?.asString
            } catch (_: Exception) {
                null
            }
        }
    }
}
