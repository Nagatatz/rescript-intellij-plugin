package com.rescript.plugin.lang.psi

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.rescript.plugin.RescriptLanguage
import com.rescript.plugin.indexing.RescriptModuleIndex
import com.rescript.plugin.indexing.RescriptNameIndex

/**
 * Stub-aware element type for ReScript declaration nodes.
 *
 * Implements [IStubElementType] to enable PSI stub indexing for top-level
 * declarations. Handles serialization/deserialization of declaration stubs
 * and populates [RescriptNameIndex] and [RescriptModuleIndex] during indexing.
 */
class RescriptDeclarationElementType(
    private val debugName: String,
) : IStubElementType<RescriptDeclarationStub, RescriptDeclarationPsiElement>(
        debugName,
        RescriptLanguage,
    ) {
    override fun getExternalId(): String = "rescript.$debugName"

    override fun createStub(
        psi: RescriptDeclarationPsiElement,
        parentStub: StubElement<out com.intellij.psi.PsiElement>?,
    ): RescriptDeclarationStub {
        val name = RescriptPsiUtils.extractName(psi)
        return RescriptDeclarationStub(parentStub, this, name, debugName)
    }

    override fun serialize(
        stub: RescriptDeclarationStub,
        dataStream: StubOutputStream,
    ) {
        dataStream.writeName(stub.name)
        dataStream.writeName(stub.declarationType)
    }

    override fun deserialize(
        dataStream: StubInputStream,
        parentStub: StubElement<*>?,
    ): RescriptDeclarationStub {
        val name = dataStream.readNameString() ?: ""
        val declarationType = dataStream.readNameString() ?: ""
        return RescriptDeclarationStub(parentStub, this, name, declarationType)
    }

    override fun indexStub(
        stub: RescriptDeclarationStub,
        sink: IndexSink,
    ) {
        val name = stub.name
        if (name.isNotEmpty() && name != "(anonymous)" && name != "(unknown)") {
            sink.occurrence(RescriptNameIndex.KEY, name)
            if (stub.declarationType == "MODULE_DECLARATION") {
                sink.occurrence(RescriptModuleIndex.KEY, name)
            }
        }
    }

    override fun createPsi(stub: RescriptDeclarationStub): RescriptDeclarationPsiElement =
        RescriptDeclarationPsiElement(stub, this)

    override fun toString(): String = debugName
}
