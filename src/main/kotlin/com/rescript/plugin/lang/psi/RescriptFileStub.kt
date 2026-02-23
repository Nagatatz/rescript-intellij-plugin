package com.rescript.plugin.lang.psi

import com.intellij.psi.stubs.PsiFileStubImpl

/**
 * File-level stub for ReScript source and interface files.
 *
 * Serves as the root stub in the stub tree hierarchy, enabling child
 * declaration stubs to be attached and indexed.
 */
class RescriptFileStub(
    file: RescriptFile?,
) : PsiFileStubImpl<RescriptFile>(file)
