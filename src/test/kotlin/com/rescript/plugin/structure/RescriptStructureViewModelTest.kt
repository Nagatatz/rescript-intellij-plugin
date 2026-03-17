package com.rescript.plugin.structure

import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Unit tests for [RescriptStructureViewModel]'s configuration properties.
 *
 * Verifies that the structure view model reports the correct sorter set
 * and leaf/plus display behavior. Uses mocked PsiFile and Editor to
 * instantiate the model without the full IntelliJ PSI infrastructure.
 *
 * @see RescriptStructureViewModel
 */
class RescriptStructureViewModelTest {
    private val mockPsiFile = mock(PsiFile::class.java)
    private val mockEditor = mock(Editor::class.java)

    private fun createModel(): RescriptStructureViewModel = RescriptStructureViewModel(mockPsiFile, mockEditor)

    @Test
    fun testGetSortersContainsAlphaSorter() {
        val model = createModel()
        val sorters = model.sorters
        assertEquals(1, sorters.size)
        assertEquals(Sorter.ALPHA_SORTER, sorters[0])
    }

    @Test
    fun testIsAlwaysShowsPlusReturnsFalse() {
        val model = createModel()
        assertFalse(model.isAlwaysShowsPlus(null))
    }

    @Test
    fun testIsAlwaysLeafReturnsFalse() {
        val model = createModel()
        assertFalse(model.isAlwaysLeaf(null))
    }

    @Test
    fun testModelCanBeInstantiatedWithNullEditor() {
        val model = RescriptStructureViewModel(mockPsiFile, null)
        assertNotNull(model)
        assertNotNull(model.sorters)
    }

    @Test
    fun testGetSortersReturnsConsistentResult() {
        val model = createModel()
        val first = model.sorters
        val second = model.sorters
        assertEquals(first.size, second.size)
        assertEquals(first[0], second[0])
    }
}
