package com.rescript.plugin.util

import java.awt.Color

/**
 * Centralised colour-format helpers shared by panels that render HTML
 * fragments via `JEditorPane`. Until now each panel had its own copy
 * of the `String.format("#%02X%02X%02X", …)` conversion; collecting
 * the logic here keeps the format consistent and lets tests cover the
 * zero-padding behaviour once.
 */
internal object RescriptColorUtils {
    /**
     * Returns [color] as an upper-case CSS hex literal of the form
     * `#RRGGBB`. Each channel is zero-padded to two characters so the
     * output is always 7 characters long and safe to interpolate into
     * an HTML `style` attribute.
     *
     * @param color the AWT colour to format
     * @return CSS-compatible hex string starting with `#`
     */
    fun colorToHexString(color: Color): String = String.format("#%02X%02X%02X", color.red, color.green, color.blue)
}
