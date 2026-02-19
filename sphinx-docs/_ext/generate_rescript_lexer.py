#!/usr/bin/env python3
"""Generate a Pygments lexer and styles for ReScript from Rescript.flex and color scheme XMLs.

This script parses the JFlex lexer definition and IntelliJ color scheme files
to produce a Pygments lexer module (`rescript_lexer.py`) with accurate token
rules and two color styles (light/dark) matching the plugin's themes.

Usage:
    python _ext/generate_rescript_lexer.py

Input files (relative to repo root):
    - src/main/java/com/rescript/plugin/lang/Rescript.flex
    - src/main/resources/colorSchemes/RescriptDarcula.xml
    - src/main/resources/colorSchemes/RescriptDefault.xml

Output:
    - _ext/rescript_lexer.py  (auto-generated, do not edit)
"""

import re
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
FLEX_PATH = REPO_ROOT / "src/main/java/com/rescript/plugin/lang/Rescript.flex"
DARCULA_PATH = REPO_ROOT / "src/main/resources/colorSchemes/RescriptDarcula.xml"
DEFAULT_PATH = REPO_ROOT / "src/main/resources/colorSchemes/RescriptDefault.xml"
OUTPUT_PATH = Path(__file__).resolve().parent / "rescript_lexer.py"


def parse_flex_keywords(flex_text: str) -> dict:
    """Extract keyword lists from Rescript.flex INITIAL state."""
    keywords = []
    keyword_operators = []
    builtins = []
    option_types = []
    bool_values = []

    # Match lines like:  "async"       { return RescriptTokenTypes.ASYNC; }
    pattern = re.compile(
        r'^\s*"(\w+)"\s*\{\s*(?:yybegin\([^)]*\);\s*)?return\s+RescriptTokenTypes\.(\w+);\s*\}',
        re.MULTILINE,
    )

    in_initial = False
    for line in flex_text.splitlines():
        if re.match(r"^<INITIAL>\s*\{", line):
            in_initial = True
            continue
        if in_initial and line.strip() == "}":
            break
        if not in_initial:
            continue

        m = pattern.match(line)
        if not m:
            continue

        word, token = m.group(1), m.group(2)

        # Classify by token type
        if token in ("MOD", "LAND", "LOR", "LXOR", "LSL", "LSR", "ASR"):
            keyword_operators.append(word)
        elif token in ("UNIT", "REF", "RAISE", "METHOD", "PRIVATE", "MATCH"):
            builtins.append(word)
        elif token in ("OPTION", "NONE", "SOME"):
            option_types.append(word)
        elif token == "BOOL_VALUE":
            bool_values.append(word)
        elif token not in ("LIDENT", "UIDENT", "INT_VALUE", "FLOAT_VALUE",
                           "TYPE_ARGUMENT", "POLY_VARIANT", "JS_STRING_OPEN",
                           "STRING_VALUE", "MULTI_COMMENT", "SINGLE_COMMENT",
                           "CHAR_VALUE", "UNDERSCORE"):
            keywords.append(word)

    # Extract multi-char operators from INITIAL state
    operators = []
    op_pattern = re.compile(
        r'^\s*"([^"\\]+(?:\\.[^"\\]*)*)"\s*\{.*return\s+RescriptTokenTypes\.(\w+);\s*\}',
        re.MULTILINE,
    )
    in_initial = False
    for line in flex_text.splitlines():
        if re.match(r"^<INITIAL>\s*\{", line):
            in_initial = True
            continue
        if in_initial and line.strip() == "}":
            break
        if not in_initial:
            continue

        m = op_pattern.match(line)
        if not m:
            continue

        symbol, token = m.group(1), m.group(2)
        # Unescape JFlex
        symbol = symbol.replace("\\^", "^").replace("\\\\", "\\")

        # Filter: only operator tokens, not keywords/strings/punctuation
        if token in (
            "STRING_CONCAT", "SHARPSHARP", "SHORTCUT", "ARROW", "RIGHT_ARROW",
            "LEFT_ARROW", "PIPE_FORWARD", "TAG_LT_SLASH", "TAG_AUTO_CLOSE",
            "EQEQEQ", "EQEQ", "EQ", "NOT_EQEQ", "NOT_EQ", "COLON_EQ",
            "COLON_GT", "LT_OR_EQUAL", "L_AND", "L_OR",
            "PLUSDOT", "MINUSDOT", "SLASHDOT", "STARDOT",
            "PLUS", "MINUS", "SLASH", "STAR", "PERCENT", "CARRET",
        ):
            operators.append(symbol)

    return {
        "keywords": sorted(set(keywords)),
        "keyword_operators": sorted(set(keyword_operators)),
        "builtins": sorted(set(builtins)),
        "option_types": sorted(set(option_types)),
        "bool_values": sorted(set(bool_values)),
        "operators": operators,
    }


def parse_color_scheme(xml_path: Path) -> dict:
    """Parse an IntelliJ color scheme XML and return name->hex color map."""
    colors = {}
    tree = ET.parse(xml_path)
    root = tree.getroot()
    for option in root.findall("option"):
        name = option.get("name", "")
        value_elem = option.find("value")
        if value_elem is not None:
            fg = value_elem.find("option[@name='FOREGROUND']")
            if fg is not None:
                colors[name] = fg.get("value", "")
    return colors


def generate_lexer_module(tokens: dict, darcula_colors: dict, default_colors: dict) -> str:
    """Generate the rescript_lexer.py source code."""
    # Build keyword/operator string literals for the lexer
    kw_list = repr(tuple(tokens["keywords"]))
    kw_op_list = repr(tuple(tokens["keyword_operators"]))
    builtin_list = repr(tuple(tokens["builtins"]))
    option_list = repr(tuple(tokens["option_types"]))
    bool_list = repr(tuple(tokens["bool_values"]))

    # Escape operators for regex
    op_escaped = []
    for op in sorted(tokens["operators"], key=len, reverse=True):
        op_escaped.append(re.escape(op))
    op_regex = "|".join(op_escaped) if op_escaped else r"\+\+"

    return f'''\
# Auto-generated by generate_rescript_lexer.py — DO NOT EDIT
# Source: Rescript.flex + RescriptDarcula.xml + RescriptDefault.xml

from pygments.lexer import RegexLexer, bygroups, words, include
from pygments.token import (
    Comment, Keyword, Name, Number, Operator, Punctuation, String, Text,
    Whitespace, Error,
)
from pygments.style import Style


# ── Keywords extracted from Rescript.flex ──

KEYWORDS = {kw_list}

KEYWORD_OPERATORS = {kw_op_list}

BUILTINS = {builtin_list}

OPTION_TYPES = {option_list}

BOOL_VALUES = {bool_list}


class RescriptLexer(RegexLexer):
    """Pygments lexer for ReScript, auto-generated from Rescript.flex."""

    name = "ReScript"
    aliases = ["rescript", "res"]
    filenames = ["*.res", "*.resi"]

    tokens = {{
        "root": [
            # Whitespace
            (r"\\s+", Whitespace),

            # Line comments
            (r"//.*$", Comment.Single),

            # Block comments (non-nested approximation)
            (r"/\\*", Comment.Multiline, "comment"),

            # Strings
            (r'"', String.Double, "string"),
            (r"`", String.Backtick, "template"),
            (r"'(?:\\\\.|\\\\x[0-9a-fA-F]{{2}}|\\\\[0-9]{{3}}|.)'", String.Char),

            # Decorators / annotations
            (r"@[a-zA-Z][a-zA-Z0-9.]*", Name.Decorator),

            # Type arguments
            (r"'[a-z_][a-zA-Z0-9_']*", Name.Label),

            # Polymorphic variants
            (r"#[a-zA-Z][a-zA-Z0-9_']*", Name.Constant),

            # Numbers
            (r"0[xX][0-9a-fA-F][0-9a-fA-F_]*", Number.Hex),
            (r"0[oO][0-7][0-7_]*", Number.Oct),
            (r"0[bB][01][01_]*", Number.Bin),
            (r"[0-9][0-9_]*\\.[0-9_]*(?:[eE][+-]?[0-9][0-9_]*)?", Number.Float),
            (r"[0-9][0-9_]*[eE][+-]?[0-9][0-9_]*", Number.Float),
            (r"[0-9][0-9_]*", Number.Integer),

            # Bool values
            (words(BOOL_VALUES, suffix=r"\\b"), Name.Builtin),

            # Option types
            (words(OPTION_TYPES, suffix=r"\\b"), Name.Builtin),

            # Keyword operators
            (words(KEYWORD_OPERATORS, suffix=r"\\b"), Operator.Word),

            # Builtins
            (words(BUILTINS, suffix=r"\\b"), Name.Builtin),

            # Keywords
            (words(KEYWORDS, suffix=r"\\b"), Keyword),

            # Module names (uppercase identifiers)
            (r"[A-Z][a-zA-Z0-9_']*", Name.Class),

            # Regular identifiers
            (r"[a-z_][a-zA-Z0-9_']*", Name),

            # JSX brackets
            (r"</|/>", Punctuation),

            # Multi-char operators
            (r"{op_regex}", Operator),

            # Single-char operators and punctuation
            (r"[|]", Operator),
            (r"[<>]", Operator),
            (r"[~?!&]", Operator),
            (r"[{{}}()\\[\\];,.]", Punctuation),
            (r":", Punctuation),

            # Catch-all
            (r".", Text),
        ],
        "comment": [
            (r"/\\*", Comment.Multiline, "#push"),
            (r"\\*/", Comment.Multiline, "#pop"),
            (r"[^/*]+", Comment.Multiline),
            (r"[/*]", Comment.Multiline),
        ],
        "string": [
            (r'\\\\[\\\\\\'"ntrb]', String.Escape),
            (r"\\\\x[0-9a-fA-F]{{2}}", String.Escape),
            (r"\\\\[0-9]{{3}}", String.Escape),
            (r"\\\\o[0-3][0-7]{{2}}", String.Escape),
            (r'"', String.Double, "#pop"),
            (r'[^\\\\"]+ ', String.Double),
            (r".", String.Double),
        ],
        "template": [
            (r"\\${{", String.Interpol, "template_interp"),
            (r"`", String.Backtick, "#pop"),
            (r"[^`$]+", String.Backtick),
            (r"\\$", String.Backtick),
        ],
        "template_interp": [
            (r"\\}}", String.Interpol, "#pop"),
            include("root"),
        ],
    }}


class RescriptDarculaStyle(Style):
    """Pygments style matching the ReScript IntelliJ Plugin Darcula theme."""

    name = "rescript-darcula"
    background_color = "#282c34"
    highlight_color = "#3e4451"

    styles = {{
        Whitespace:          "",
        Comment:             "italic #5c6370",
        Comment.Single:      "italic #5c6370",
        Comment.Multiline:   "italic #5c6370",
        Keyword:             "bold #c678dd",
        Operator:            "#{darcula_colors.get("RESCRIPT_OPERATOR", "56B6C2")}",
        Operator.Word:       "bold #c678dd",
        Name:                "#abb2bf",
        Name.Class:          "#{darcula_colors.get("RESCRIPT_MODULE_NAME", "61AFEF")}",
        Name.Builtin:        "#e5c07b",
        Name.Decorator:      "#{darcula_colors.get("RESCRIPT_ANNOTATION", "BBB529")}",
        Name.Label:          "#{darcula_colors.get("RESCRIPT_TYPE_ARGUMENT", "C678DD")}",
        Name.Constant:       "#{darcula_colors.get("RESCRIPT_POLY_VARIANT", "D19A66")}",
        String:              "#98c379",
        String.Double:       "#98c379",
        String.Backtick:     "#98c379",
        String.Char:         "#98c379",
        String.Escape:       "#d19a66",
        String.Interpol:     "#c678dd",
        Number:              "#{darcula_colors.get("RESCRIPT_NUMBER", "6897BB")}",
        Number.Integer:      "#{darcula_colors.get("RESCRIPT_NUMBER", "6897BB")}",
        Number.Float:        "#{darcula_colors.get("RESCRIPT_NUMBER", "6897BB")}",
        Number.Hex:          "#{darcula_colors.get("RESCRIPT_NUMBER", "6897BB")}",
        Number.Oct:          "#{darcula_colors.get("RESCRIPT_NUMBER", "6897BB")}",
        Number.Bin:          "#{darcula_colors.get("RESCRIPT_NUMBER", "6897BB")}",
        Punctuation:         "#abb2bf",
        Error:               "#e06c75",
    }}


class RescriptDefaultStyle(Style):
    """Pygments style matching the ReScript IntelliJ Plugin Default (light) theme."""

    name = "rescript-default"
    background_color = "#ffffff"
    highlight_color = "#e8e8e8"

    styles = {{
        Whitespace:          "",
        Comment:             "italic #808080",
        Comment.Single:      "italic #808080",
        Comment.Multiline:   "italic #808080",
        Keyword:             "bold #000080",
        Operator:            "#{default_colors.get("RESCRIPT_OPERATOR", "666666")}",
        Operator.Word:       "bold #000080",
        Name:                "#000000",
        Name.Class:          "#{default_colors.get("RESCRIPT_MODULE_NAME", "000080")}",
        Name.Builtin:        "#000080",
        Name.Decorator:      "#{default_colors.get("RESCRIPT_ANNOTATION", "808000")}",
        Name.Label:          "#{default_colors.get("RESCRIPT_TYPE_ARGUMENT", "800080")}",
        Name.Constant:       "#{default_colors.get("RESCRIPT_POLY_VARIANT", "008080")}",
        String:              "#008000",
        String.Double:       "#008000",
        String.Backtick:     "#008000",
        String.Char:         "#008000",
        String.Escape:       "#000080",
        String.Interpol:     "#000080",
        Number:              "#{default_colors.get("RESCRIPT_NUMBER", "0000FF")}",
        Number.Integer:      "#{default_colors.get("RESCRIPT_NUMBER", "0000FF")}",
        Number.Float:        "#{default_colors.get("RESCRIPT_NUMBER", "0000FF")}",
        Number.Hex:          "#{default_colors.get("RESCRIPT_NUMBER", "0000FF")}",
        Number.Oct:          "#{default_colors.get("RESCRIPT_NUMBER", "0000FF")}",
        Number.Bin:          "#{default_colors.get("RESCRIPT_NUMBER", "0000FF")}",
        Punctuation:         "#000000",
        Error:               "#ff0000",
    }}
'''


def main():
    flex_text = FLEX_PATH.read_text(encoding="utf-8")
    tokens = parse_flex_keywords(flex_text)
    darcula_colors = parse_color_scheme(DARCULA_PATH)
    default_colors = parse_color_scheme(DEFAULT_PATH)

    source = generate_lexer_module(tokens, darcula_colors, default_colors)
    OUTPUT_PATH.write_text(source, encoding="utf-8")

    print(f"Generated {OUTPUT_PATH}")
    print(f"  Keywords:          {len(tokens['keywords'])}")
    print(f"  Keyword operators: {len(tokens['keyword_operators'])}")
    print(f"  Builtins:          {len(tokens['builtins'])}")
    print(f"  Option types:      {len(tokens['option_types'])}")
    print(f"  Bool values:       {len(tokens['bool_values'])}")
    print(f"  Operators:         {len(tokens['operators'])}")


if __name__ == "__main__":
    main()
