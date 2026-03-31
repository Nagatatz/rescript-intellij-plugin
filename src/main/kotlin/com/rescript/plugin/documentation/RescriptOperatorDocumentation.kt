package com.rescript.plugin.documentation

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.rescript.plugin.lang.RescriptTokenTypes
import com.rescript.plugin.util.RescriptSecurityUtils

/**
 * Provides operator precedence and documentation information for ReScript operators.
 *
 * Contains a mapping of operator token types to their names, precedence levels,
 * associativity, and descriptions. Used by [RescriptDocumentationProvider] to
 * show hover documentation for operator tokens.
 *
 * @see RescriptDocumentationProvider
 */
data object RescriptOperatorDocumentation {
    /**
     * Information about a ReScript operator for documentation purposes.
     *
     * @param name human-readable name of the operator
     * @param precedence numeric precedence level (higher binds tighter)
     * @param associativity left, right, or none
     * @param description brief explanation of the operator
     */
    data class OperatorInfo(
        val name: String,
        val precedence: Int,
        val associativity: String,
        val description: String,
    )

    /**
     * Maps operator token types to their precedence and description information.
     *
     * Precedence levels follow ReScript's operator precedence table,
     * from lowest (1) to highest (16).
     */
    val OPERATOR_INFO: Map<IElementType, OperatorInfo> =
        mapOf(
            // Pipe operators
            RescriptTokenTypes.PIPE_FORWARD to
                OperatorInfo(
                    "Pipe forward",
                    1,
                    "Left",
                    "Pipes the left operand as the last argument to the right function.",
                ),
            RescriptTokenTypes.ARROW to
                OperatorInfo(
                    "Pipe",
                    1,
                    "Left",
                    "Pipes the left operand as the first argument to the right function.",
                ),
            // Logical operators
            RescriptTokenTypes.L_OR to
                OperatorInfo(
                    "Logical OR",
                    2,
                    "Left",
                    "Short-circuit logical OR. Returns true if either operand is true.",
                ),
            RescriptTokenTypes.L_AND to
                OperatorInfo(
                    "Logical AND",
                    3,
                    "Left",
                    "Short-circuit logical AND. Returns true if both operands are true.",
                ),
            // Comparison operators
            RescriptTokenTypes.EQEQEQ to
                OperatorInfo(
                    "Strict equality",
                    4,
                    "Left",
                    "Compiles to JavaScript `===`. Checks referential equality.",
                ),
            RescriptTokenTypes.EQEQ to
                OperatorInfo("Structural equality", 4, "Left", "Compiles to a deep structural equality check."),
            RescriptTokenTypes.NOT_EQEQ to
                OperatorInfo(
                    "Strict inequality",
                    4,
                    "Left",
                    "Compiles to JavaScript `!==`. Checks referential inequality.",
                ),
            RescriptTokenTypes.NOT_EQ to
                OperatorInfo("Structural inequality", 4, "Left", "Compiles to a deep structural inequality check."),
            RescriptTokenTypes.LT to
                OperatorInfo("Less than", 4, "Left", "Numeric or string comparison: less than."),
            RescriptTokenTypes.GT to
                OperatorInfo("Greater than", 4, "Left", "Numeric or string comparison: greater than."),
            RescriptTokenTypes.LT_OR_EQUAL to
                OperatorInfo("Less than or equal", 4, "Left", "Numeric or string comparison: less than or equal."),
            // String concatenation
            RescriptTokenTypes.STRING_CONCAT to
                OperatorInfo("String concatenation", 5, "Right", "Concatenates two strings using the ++ operator."),
            // Arithmetic operators
            RescriptTokenTypes.PLUS to
                OperatorInfo("Addition", 6, "Left", "Integer addition."),
            RescriptTokenTypes.MINUS to
                OperatorInfo("Subtraction", 6, "Left", "Integer subtraction."),
            RescriptTokenTypes.PLUSDOT to
                OperatorInfo("Float addition", 6, "Left", "Floating-point addition."),
            RescriptTokenTypes.MINUSDOT to
                OperatorInfo("Float subtraction", 6, "Left", "Floating-point subtraction."),
            RescriptTokenTypes.STAR to
                OperatorInfo("Multiplication", 7, "Left", "Integer multiplication."),
            RescriptTokenTypes.SLASH to
                OperatorInfo("Division", 7, "Left", "Integer division."),
            RescriptTokenTypes.STARDOT to
                OperatorInfo("Float multiplication", 7, "Left", "Floating-point multiplication."),
            RescriptTokenTypes.SLASHDOT to
                OperatorInfo("Float division", 7, "Left", "Floating-point division."),
            RescriptTokenTypes.PERCENT to
                OperatorInfo("Modulo", 7, "Left", "Integer modulo (remainder)."),
            RescriptTokenTypes.CARRET to
                OperatorInfo("Exponentiation", 8, "Right", "Raises a float to a power (via Js.Math.pow)."),
            // Bitwise operators
            RescriptTokenTypes.LAND to
                OperatorInfo("Bitwise AND", 9, "Left", "Bitwise AND of two integers."),
            RescriptTokenTypes.LOR to
                OperatorInfo("Bitwise OR", 9, "Left", "Bitwise OR of two integers."),
            RescriptTokenTypes.LXOR to
                OperatorInfo("Bitwise XOR", 9, "Left", "Bitwise XOR of two integers."),
            RescriptTokenTypes.LSL to
                OperatorInfo("Left shift", 10, "Left", "Bitwise left shift."),
            RescriptTokenTypes.LSR to
                OperatorInfo("Logical right shift", 10, "Left", "Bitwise logical (unsigned) right shift."),
            RescriptTokenTypes.ASR to
                OperatorInfo("Arithmetic right shift", 10, "Left", "Bitwise arithmetic (signed) right shift."),
            // Assignment
            RescriptTokenTypes.COLON_EQ to
                OperatorInfo("Ref assignment", 11, "Right", "Assigns a new value to a ref cell."),
            // Arrow
            RescriptTokenTypes.RIGHT_ARROW to
                OperatorInfo("Function arrow", 0, "Right", "Separates function parameters from the body."),
        )

    /**
     * Generates HTML documentation for a ReScript operator token.
     *
     * Shows the operator symbol, its name, precedence level, associativity,
     * and a short description.
     *
     * @param element the PSI element (expected to be an operator token)
     * @return HTML documentation string, or null if not an operator
     */
    fun generateOperatorDoc(element: PsiElement): String? {
        val tokenType = element.node?.elementType ?: return null
        val info = OPERATOR_INFO[tokenType] ?: return null

        val escapedText = RescriptSecurityUtils.escapeHtml(element.text)
        val escapedName = RescriptSecurityUtils.escapeHtml(info.name)
        val escapedDesc = RescriptSecurityUtils.escapeHtml(info.description)

        return buildString {
            append("<div class='definition'><pre>")
            append("<b>$escapedText</b> — $escapedName")
            append("</pre></div>")
            append("<div class='content'>")
            append("<table>")
            append("<tr><td><b>Precedence:</b></td><td>${info.precedence}</td></tr>")
            append("<tr><td><b>Associativity:</b></td><td>${info.associativity}</td></tr>")
            append("</table>")
            append("<p>$escapedDesc</p>")
            append("</div>")
        }
    }
}
