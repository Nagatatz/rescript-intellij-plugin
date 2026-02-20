package com.rescript.plugin.editor

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import com.rescript.plugin.lang.RescriptTokenTypes
import java.awt.Color

/**
 * Provides color preview swatches in the editor gutter for color literals.
 *
 * Recognizes hex colors (#RGB, #RRGGBB, #RRGGBBAA), rgb()/rgba(), and hsl()/hsla()
 * within string literals. Clicking the gutter icon opens a color chooser dialog.
 */
class RescriptColorProvider : ElementColorProvider {
    companion object {
        // #RGB, #RRGGBB, #RRGGBBAA
        private val HEX_PATTERN = Regex("""^"#([0-9a-fA-F]{3,8})"$""")

        // rgb(r, g, b) or rgba(r, g, b, a)
        private val RGB_PATTERN = Regex("""^"rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)(?:\s*,\s*([\d.]+))?\s*\)"$""")

        // hsl(h, s%, l%) or hsla(h, s%, l%, a)
        private val HSL_PATTERN = Regex("""^"hsla?\(\s*(\d+)\s*,\s*(\d+)%\s*,\s*(\d+)%(?:\s*,\s*([\d.]+))?\s*\)"$""")
    }

    override fun getColorFrom(element: PsiElement): Color? {
        val tokenType = element.node?.elementType ?: return null
        if (tokenType != RescriptTokenTypes.STRING_VALUE) return null

        val text = element.text
        return parseHexColor(text) ?: parseRgbColor(text) ?: parseHslColor(text)
    }

    override fun setColorTo(
        element: PsiElement,
        color: Color,
    ) {
        // Replace the string literal with the new hex color
        val hex = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
        val newText = "\"$hex\""
        val document = element.containingFile?.viewProvider?.document ?: return
        val range = element.textRange
        document.replaceString(range.startOffset, range.endOffset, newText)
    }

    /**
     * Parses a hex color string (#RGB, #RRGGBB, or #RRGGBBAA).
     *
     * @param text the string literal text including surrounding quotes
     * @return the parsed Color, or null if not a valid hex color
     */
    internal fun parseHexColor(text: String): Color? {
        val match = HEX_PATTERN.matchEntire(text) ?: return null
        val hex = match.groupValues[1]
        return when (hex.length) {
            3 -> {
                val r = hex[0].digitToInt(16) * 17
                val g = hex[1].digitToInt(16) * 17
                val b = hex[2].digitToInt(16) * 17
                Color(r, g, b)
            }
            6 -> {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                Color(r, g, b)
            }
            8 -> {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                val a = hex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> null
        }
    }

    /**
     * Parses an rgb() or rgba() color string.
     *
     * @param text the string literal text including surrounding quotes
     * @return the parsed Color, or null if not a valid rgb/rgba color
     */
    internal fun parseRgbColor(text: String): Color? {
        val match = RGB_PATTERN.matchEntire(text) ?: return null
        val r = match.groupValues[1].toIntOrNull()?.coerceIn(0, 255) ?: return null
        val g = match.groupValues[2].toIntOrNull()?.coerceIn(0, 255) ?: return null
        val b = match.groupValues[3].toIntOrNull()?.coerceIn(0, 255) ?: return null
        val a =
            match.groupValues[4]
                .takeIf { it.isNotEmpty() }
                ?.toFloatOrNull()
                ?.coerceIn(0f, 1f)
        return if (a != null) Color(r, g, b, (a * 255).toInt()) else Color(r, g, b)
    }

    /**
     * Parses an hsl() or hsla() color string by converting HSL to RGB.
     *
     * Uses the standard HSL-to-RGB conversion algorithm with chroma,
     * intermediate value, and lightness adjustment.
     *
     * @param text the string literal text including surrounding quotes
     * @return the parsed Color, or null if not a valid hsl/hsla color
     */
    internal fun parseHslColor(text: String): Color? {
        val match = HSL_PATTERN.matchEntire(text) ?: return null
        val h = match.groupValues[1].toFloatOrNull() ?: return null
        val s = (match.groupValues[2].toFloatOrNull() ?: return null) / 100f
        val l = (match.groupValues[3].toFloatOrNull() ?: return null) / 100f

        // HSL to RGB conversion
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        val (r1, g1, b1) =
            when {
                h < 60 -> Triple(c, x, 0f)
                h < 120 -> Triple(x, c, 0f)
                h < 180 -> Triple(0f, c, x)
                h < 240 -> Triple(0f, x, c)
                h < 300 -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
        val r = ((r1 + m) * 255).toInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255).toInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255).toInt().coerceIn(0, 255)

        val a =
            match.groupValues[4]
                .takeIf { it.isNotEmpty() }
                ?.toFloatOrNull()
                ?.coerceIn(0f, 1f)
        return if (a != null) Color(r, g, b, (a * 255).toInt()) else Color(r, g, b)
    }
}
