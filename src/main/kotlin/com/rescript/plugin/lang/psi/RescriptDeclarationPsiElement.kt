package com.rescript.plugin.lang.psi

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

/**
 * PSI element for ReScript top-level declarations backed by a [RescriptDeclarationStub].
 *
 * Supports both AST-based construction (during parsing) and stub-based
 * construction (when restoring from the index). The declared name can be
 * retrieved from the stub without reparsing the file.
 */
class RescriptDeclarationPsiElement : StubBasedPsiElementBase<RescriptDeclarationStub> {
    /** AST-based constructor used during parsing. */
    constructor(node: ASTNode) : super(node)

    /** Stub-based constructor used when restoring from the index. */
    constructor(stub: RescriptDeclarationStub, type: IStubElementType<*, *>) : super(stub, type)

    /**
     * Returns the declared name, preferring the stub value when available.
     *
     * @return the name from the stub, or extracted from the AST if no stub is present
     */
    @Suppress("unused") // Public API for stub-based name resolution
    fun getDeclarationName(): String = greenStub?.name ?: RescriptPsiUtils.extractName(this)

    override fun toString(): String = "RescriptDeclarationPsiElement(${node.elementType})"
}
