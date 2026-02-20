package com.rescript.plugin.lang

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.rescript.plugin.RescriptLanguage

/** Creates a token element type for the ReScript language. */
private fun token(name: String) = IElementType(name, RescriptLanguage)

/**
 * All token types produced by the ReScript lexer, plus commonly used [TokenSet]s.
 */
object RescriptTokenTypes {
    // ── Whitespace & EOL ──────────────────────────────────────────────
    @JvmField val EOL = token("EOL")

    // ── Comments ──────────────────────────────────────────────────────
    @JvmField val SINGLE_COMMENT = token("SINGLE_COMMENT")

    @JvmField val MULTI_COMMENT = token("MULTI_COMMENT")

    // ── Keywords ──────────────────────────────────────────────────────
    @JvmField val ASYNC = token("ASYNC")

    @JvmField val AWAIT = token("AWAIT")

    @JvmField val AND = token("AND")

    @JvmField val AS = token("AS")

    @JvmField val ASSERT = token("ASSERT")

    @JvmField val BEGIN = token("BEGIN")

    @JvmField val CATCH = token("CATCH")

    @JvmField val CLASS = token("CLASS")

    @JvmField val CONSTRAINT = token("CONSTRAINT")

    @JvmField val DO = token("DO")

    @JvmField val DONE = token("DONE")

    @JvmField val DOWNTO = token("DOWNTO")

    @JvmField val ELSE = token("ELSE")

    @JvmField val END = token("END")

    @JvmField val EXCEPTION = token("EXCEPTION")

    @JvmField val EXTERNAL = token("EXTERNAL")

    @JvmField val FOR = token("FOR")

    @JvmField val FUNCTOR = token("FUNCTOR")

    @JvmField val IF = token("IF")

    @JvmField val IN = token("IN")

    @JvmField val INCLUDE = token("INCLUDE")

    @JvmField val INHERIT = token("INHERIT")

    @JvmField val INITIALIZER = token("INITIALIZER")

    @JvmField val LAZY = token("LAZY")

    @JvmField val LET = token("LET")

    @JvmField val LIST = token("LIST")

    @JvmField val DICT = token("DICT")

    @JvmField val MODULE = token("MODULE")

    @JvmField val MUTABLE = token("MUTABLE")

    @JvmField val NEW = token("NEW")

    @JvmField val NONREC = token("NONREC")

    @JvmField val OBJECT = token("OBJECT")

    @JvmField val OF = token("OF")

    @JvmField val OPEN = token("OPEN")

    @JvmField val OR = token("OR")

    @JvmField val PUB = token("PUB")

    @JvmField val PRI = token("PRI")

    @JvmField val RAW = token("RAW")

    @JvmField val FFI = token("FFI")

    @JvmField val REC = token("REC")

    @JvmField val SIG = token("SIG")

    @JvmField val STRUCT = token("STRUCT")

    @JvmField val SWITCH = token("SWITCH")

    @JvmField val THEN = token("THEN")

    @JvmField val TRY = token("TRY")

    @JvmField val TYPE = token("TYPE")

    @JvmField val UNPACK = token("UNPACK")

    @JvmField val VAL = token("VAL")

    @JvmField val VIRTUAL = token("VIRTUAL")

    @JvmField val WHEN = token("WHEN")

    @JvmField val WHILE = token("WHILE")

    @JvmField val WITH = token("WITH")

    // ── Keyword operators ─────────────────────────────────────────────
    @JvmField val MOD = token("MOD")

    @JvmField val LAND = token("LAND")

    @JvmField val LOR = token("LOR")

    @JvmField val LXOR = token("LXOR")

    @JvmField val LSL = token("LSL")

    @JvmField val LSR = token("LSR")

    @JvmField val ASR = token("ASR")

    // ── Built-ins ─────────────────────────────────────────────────────
    @JvmField val UNIT = token("UNIT")

    @JvmField val REF = token("REF")

    @JvmField val RAISE = token("RAISE")

    @JvmField val METHOD = token("METHOD")

    @JvmField val PRIVATE = token("PRIVATE")

    @JvmField val MATCH = token("MATCH")

    @JvmField val OPTION = token("OPTION")

    @JvmField val NONE = token("NONE")

    @JvmField val SOME = token("SOME")

    // ── Literals ──────────────────────────────────────────────────────
    @JvmField val BOOL_VALUE = token("BOOL_VALUE")

    @JvmField val STRING_VALUE = token("STRING_VALUE")

    @JvmField val FLOAT_VALUE = token("FLOAT_VALUE")

    @JvmField val CHAR_VALUE = token("CHAR_VALUE")

    @JvmField val INT_VALUE = token("INT_VALUE")

    // ── Identifiers ───────────────────────────────────────────────────
    @JvmField val LIDENT = token("LIDENT")

    @JvmField val UIDENT = token("UIDENT")

    @JvmField val UNDERSCORE = token("UNDERSCORE")

    @JvmField val TYPE_ARGUMENT = token("TYPE_ARGUMENT")

    @JvmField val POLY_VARIANT = token("POLY_VARIANT")

    // ── Template strings ──────────────────────────────────────────────
    @JvmField val JS_STRING_OPEN = token("JS_STRING_OPEN")

    @JvmField val JS_STRING_CLOSE = token("JS_STRING_CLOSE")

    @JvmField val DOLLAR = token("DOLLAR")

    // ── Multi-character operators ─────────────────────────────────────
    @JvmField val STRING_CONCAT = token("STRING_CONCAT")

    @Suppress("unused")
    @JvmField
    val SHARPSHARP = token("SHARPSHARP")

    @JvmField val SHORTCUT = token("SHORTCUT")

    @JvmField val ARROW = token("ARROW")

    @JvmField val RIGHT_ARROW = token("RIGHT_ARROW")

    @JvmField val LEFT_ARROW = token("LEFT_ARROW")

    @JvmField val PIPE_FORWARD = token("PIPE_FORWARD")

    @JvmField val TAG_LT_SLASH = token("TAG_LT_SLASH")

    @JvmField val TAG_AUTO_CLOSE = token("TAG_AUTO_CLOSE")

    @JvmField val TAG_LT = token("TAG_LT")

    @JvmField val TAG_GT = token("TAG_GT")

    @JvmField val JSX_TAG_NAME = token("JSX_TAG_NAME")

    @JvmField val JSX_COMPONENT_NAME = token("JSX_COMPONENT_NAME")

    // ── Comparison / assignment ────────────────────────────────────────
    @JvmField val EQEQEQ = token("EQEQEQ")

    @JvmField val EQEQ = token("EQEQ")

    @JvmField val EQ = token("EQ")

    @JvmField val NOT_EQEQ = token("NOT_EQEQ")

    @JvmField val NOT_EQ = token("NOT_EQ")

    @JvmField val COLON_EQ = token("COLON_EQ")

    @JvmField val COLON_GT = token("COLON_GT")

    @JvmField val LT_OR_EQUAL = token("LT_OR_EQUAL")

    @JvmField val L_AND = token("L_AND")

    @JvmField val L_OR = token("L_OR")

    // ── Punctuation ───────────────────────────────────────────────────
    @JvmField val COMMA = token("COMMA")

    @JvmField val COLON = token("COLON")

    @JvmField val SEMI = token("SEMI")

    @Suppress("unused")
    @JvmField
    val SINGLE_QUOTE = token("SINGLE_QUOTE")

    @JvmField val DOTDOTDOT = token("DOTDOTDOT")

    @JvmField val DOTDOT = token("DOTDOT")

    @JvmField val DOT = token("DOT")

    @JvmField val PIPE = token("PIPE")

    @JvmField val LPAREN = token("LPAREN")

    @JvmField val RPAREN = token("RPAREN")

    @JvmField val LBRACE = token("LBRACE")

    @JvmField val RBRACE = token("RBRACE")

    @JvmField val LBRACKET = token("LBRACKET")

    @JvmField val RBRACKET = token("RBRACKET")

    @JvmField val ARROBASE = token("ARROBASE")

    @JvmField val ANNOTATION_NAME = token("ANNOTATION_NAME")

    @Suppress("unused")
    @JvmField
    val SHARP = token("SHARP")

    @JvmField val QUESTION_MARK = token("QUESTION_MARK")

    @JvmField val EXCLAMATION_MARK = token("EXCLAMATION_MARK")

    @JvmField val TILDE = token("TILDE")

    @Suppress("unused")
    @JvmField
    val AMPERSAND = token("AMPERSAND")

    @JvmField val LT = token("LT")

    @JvmField val GT = token("GT")

    // ── Arithmetic ────────────────────────────────────────────────────
    @JvmField val CARRET = token("CARRET")

    @JvmField val PLUSDOT = token("PLUSDOT")

    @JvmField val MINUSDOT = token("MINUSDOT")

    @JvmField val SLASHDOT = token("SLASHDOT")

    @JvmField val STARDOT = token("STARDOT")

    @JvmField val PLUS = token("PLUS")

    @JvmField val MINUS = token("MINUS")

    @JvmField val SLASH = token("SLASH")

    @JvmField val STAR = token("STAR")

    @JvmField val PERCENT = token("PERCENT")

    @Suppress("unused")
    @JvmField
    val BACKSLASH = token("BACKSLASH")

    // ── Token sets ────────────────────────────────────────────────────

    @JvmField
    val KEYWORDS: TokenSet =
        TokenSet.create(
            ASYNC,
            AWAIT,
            AND,
            AS,
            ASSERT,
            BEGIN,
            CATCH,
            CLASS,
            CONSTRAINT,
            DO,
            DONE,
            DOWNTO,
            ELSE,
            END,
            EXCEPTION,
            EXTERNAL,
            FOR,
            FUNCTOR,
            IF,
            IN,
            INCLUDE,
            INHERIT,
            INITIALIZER,
            LAZY,
            LET,
            LIST,
            DICT,
            MODULE,
            MUTABLE,
            NEW,
            NONREC,
            OBJECT,
            OF,
            OPEN,
            OR,
            PUB,
            PRI,
            RAW,
            FFI,
            REC,
            SIG,
            STRUCT,
            SWITCH,
            THEN,
            TRY,
            TYPE,
            UNPACK,
            VAL,
            VIRTUAL,
            WHEN,
            WHILE,
            WITH,
            UNIT,
            REF,
            RAISE,
            METHOD,
            PRIVATE,
            MATCH,
            OPTION,
            NONE,
            SOME,
            BOOL_VALUE,
        )

    @JvmField val COMMENTS: TokenSet = TokenSet.create(SINGLE_COMMENT, MULTI_COMMENT)

    @JvmField val STRINGS: TokenSet = TokenSet.create(STRING_VALUE, JS_STRING_OPEN, JS_STRING_CLOSE)

    @JvmField val NUMBERS: TokenSet = TokenSet.create(INT_VALUE, FLOAT_VALUE)

    @JvmField
    val OPERATORS: TokenSet =
        TokenSet.create(
            STRING_CONCAT,
            ARROW,
            RIGHT_ARROW,
            LEFT_ARROW,
            PIPE_FORWARD,
            EQEQEQ,
            EQEQ,
            EQ,
            NOT_EQEQ,
            NOT_EQ,
            COLON_EQ,
            COLON_GT,
            LT_OR_EQUAL,
            L_AND,
            L_OR,
            MOD,
            LAND,
            LOR,
            LXOR,
            LSL,
            LSR,
            ASR,
            PLUS,
            MINUS,
            STAR,
            SLASH,
            PERCENT,
            CARRET,
            PLUSDOT,
            MINUSDOT,
            STARDOT,
            SLASHDOT,
        )

    /** Top-level declaration keywords (used by the lightweight parser). */
    @JvmField
    val TOP_LEVEL_KEYWORDS: TokenSet =
        TokenSet.create(
            LET,
            TYPE,
            MODULE,
            EXTERNAL,
            OPEN,
            INCLUDE,
            EXCEPTION,
        )
}
