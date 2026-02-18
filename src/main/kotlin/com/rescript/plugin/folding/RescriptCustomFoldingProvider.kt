package com.rescript.plugin.folding

import com.intellij.lang.folding.CustomFoldingProvider

class RescriptCustomFoldingProvider : CustomFoldingProvider() {
    override fun isCustomRegionStart(elementText: String): Boolean {
        val trimmed = elementText.trimStart()
        return trimmed.startsWith("//#region") || trimmed.startsWith("// #region")
    }

    override fun isCustomRegionEnd(elementText: String): Boolean {
        val trimmed = elementText.trimStart()
        return trimmed.startsWith("//#endregion") || trimmed.startsWith("// #endregion")
    }

    override fun getPlaceholderText(elementText: String): String {
        val trimmed = elementText.trimStart()
        val name =
            trimmed
                .removePrefix("//#region")
                .removePrefix("// #region")
                .trim()
        return name.ifEmpty { "..." }
    }

    override fun getDescription(): String = "//#region ... //#endregion"

    override fun getStartString(): String = "//#region"

    override fun getEndString(): String = "//#endregion"
}
