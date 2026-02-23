package com.rescript.plugin.lang.psi

/**
 * Holder for stub-backed element types used by the PSI stub index.
 *
 * This class is registered as a `stubElementTypeHolder` in plugin.xml and must
 * contain only [RescriptDeclarationElementType] fields. Non-stub element types
 * remain in [RescriptElementTypes].
 *
 * The fields here are canonical instances — [RescriptElementTypes] delegates to them
 * to maintain backward compatibility with existing code that references
 * `RescriptElementTypes.LET_DECLARATION` etc.
 */
object RescriptStubElementTypes {
    @JvmField val LET_DECLARATION = RescriptDeclarationElementType("LET_DECLARATION")

    @JvmField val TYPE_DECLARATION = RescriptDeclarationElementType("TYPE_DECLARATION")

    @JvmField val MODULE_DECLARATION = RescriptDeclarationElementType("MODULE_DECLARATION")

    @JvmField val EXTERNAL_DECLARATION = RescriptDeclarationElementType("EXTERNAL_DECLARATION")

    @JvmField val EXCEPTION_DECLARATION = RescriptDeclarationElementType("EXCEPTION_DECLARATION")
}
