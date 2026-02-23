package com.rescript.plugin.lang.psi

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

/**
 * Stub element for ReScript top-level declarations (let, type, module, external, exception).
 *
 * Stores the declared name and declaration type so that they can be retrieved
 * from the stub index without parsing the full PSI tree.
 */
class RescriptDeclarationStub(
    parent: StubElement<*>?,
    elementType: IStubElementType<*, *>,
    val name: String,
    val declarationType: String,
) : StubBase<RescriptDeclarationPsiElement>(parent, elementType)
