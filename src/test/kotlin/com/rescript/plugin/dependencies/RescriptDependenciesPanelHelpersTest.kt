package com.rescript.plugin.dependencies

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Pure tests for the [RescriptDependenciesPanel] helpers that do not
 * depend on the IDE — the panel class itself remains exempt under the
 * Swing-UI clause in `.claude/rules/testing.md`. These cover the label
 * formatting and the [RescriptDependenciesPanel.PackageNode] payload
 * used by the tree double-click handler.
 */
class RescriptDependenciesPanelHelpersTest {
    @Test
    fun `displayLabelFor renders version when present`() {
        assertEquals(
            "react (18.2.0)",
            RescriptDependenciesPanel.displayLabelFor("react", "18.2.0"),
        )
    }

    @Test
    fun `displayLabelFor renders bare name when version is null`() {
        assertEquals(
            "@some/pkg",
            RescriptDependenciesPanel.displayLabelFor("@some/pkg", null),
        )
    }

    @Test
    fun `PackageNode toString delegates to displayLabel`() {
        val node =
            RescriptDependenciesPanel.PackageNode(
                pkgName = "react",
                packageJsonFile = null,
                displayLabel = "react (18.2.0)",
            )
        assertEquals("react (18.2.0)", node.toString())
    }

    @Test
    fun `PackageNode equality compares all fields`() {
        val a =
            RescriptDependenciesPanel.PackageNode(
                pkgName = "react",
                packageJsonFile = null,
                displayLabel = "react",
            )
        val b =
            RescriptDependenciesPanel.PackageNode(
                pkgName = "react",
                packageJsonFile = null,
                displayLabel = "react",
            )
        assertEquals(a, b)
    }
}
