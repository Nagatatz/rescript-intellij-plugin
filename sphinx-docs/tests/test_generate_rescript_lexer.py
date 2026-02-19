"""Tests for the ReScript Pygments lexer generator."""

import sys
from pathlib import Path

import pytest

# Add _ext to path so we can import the generator
sys.path.insert(0, str(Path(__file__).parent.parent / "_ext"))

from generate_rescript_lexer import (
    generate_lexer_module,
    parse_color_scheme,
    parse_flex_keywords,
)

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
FLEX_PATH = REPO_ROOT / "src/main/java/com/rescript/plugin/lang/Rescript.flex"
DARCULA_PATH = REPO_ROOT / "src/main/resources/colorSchemes/RescriptDarcula.xml"
DEFAULT_PATH = REPO_ROOT / "src/main/resources/colorSchemes/RescriptDefault.xml"


@pytest.fixture()
def flex_text() -> str:
    """Read the Rescript.flex source file."""
    return FLEX_PATH.read_text(encoding="utf-8")


@pytest.fixture()
def sample_flex_text() -> str:
    """Provide a minimal Rescript.flex-like text for unit testing."""
    return """\
%%

%{
  // header
%}

%%

<INITIAL> {
  "let"         { return RescriptTokenTypes.LET; }
  "type"        { return RescriptTokenTypes.TYPE; }
  "module"      { return RescriptTokenTypes.MODULE; }
  "switch"      { return RescriptTokenTypes.SWITCH; }
  "if"          { return RescriptTokenTypes.IF; }
  "else"        { return RescriptTokenTypes.ELSE; }
  "async"       { return RescriptTokenTypes.ASYNC; }
  "await"       { return RescriptTokenTypes.AWAIT; }
  "mod"         { return RescriptTokenTypes.MOD; }
  "land"        { return RescriptTokenTypes.LAND; }
  "lor"         { return RescriptTokenTypes.LOR; }
  "true"        { return RescriptTokenTypes.BOOL_VALUE; }
  "false"       { return RescriptTokenTypes.BOOL_VALUE; }
  "Some"        { return RescriptTokenTypes.SOME; }
  "None"        { return RescriptTokenTypes.NONE; }
  "raise"       { return RescriptTokenTypes.RAISE; }
  "=>"          { return RescriptTokenTypes.ARROW; }
  "->"          { return RescriptTokenTypes.RIGHT_ARROW; }
  "|>"          { return RescriptTokenTypes.PIPE_FORWARD; }
  "=="          { return RescriptTokenTypes.EQEQ; }
  "+"           { return RescriptTokenTypes.PLUS; }
  "-"           { return RescriptTokenTypes.MINUS; }
}
"""


class TestParseFlexKeywords:
    """Tests for parse_flex_keywords function."""

    def test_extracts_keywords_from_sample(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert "let" in result["keywords"]
        assert "type" in result["keywords"]
        assert "module" in result["keywords"]
        assert "switch" in result["keywords"]
        assert "if" in result["keywords"]
        assert "else" in result["keywords"]

    def test_extracts_keyword_operators_from_sample(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert "mod" in result["keyword_operators"]
        assert "land" in result["keyword_operators"]
        assert "lor" in result["keyword_operators"]

    def test_extracts_bool_values_from_sample(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert "true" in result["bool_values"]
        assert "false" in result["bool_values"]

    def test_extracts_option_types_from_sample(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert "Some" in result["option_types"]
        assert "None" in result["option_types"]

    def test_extracts_builtins_from_sample(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert "raise" in result["builtins"]

    def test_extracts_operators_from_sample(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert "=>" in result["operators"]
        assert "->" in result["operators"]
        assert "|>" in result["operators"]
        assert "==" in result["operators"]
        assert "+" in result["operators"]
        assert "-" in result["operators"]

    def test_keywords_do_not_include_operators(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert "mod" not in result["keywords"]
        assert "land" not in result["keywords"]

    def test_keywords_do_not_include_bool_values(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert "true" not in result["keywords"]
        assert "false" not in result["keywords"]

    def test_returns_sorted_keywords(self, sample_flex_text: str) -> None:
        result = parse_flex_keywords(sample_flex_text)
        assert result["keywords"] == sorted(result["keywords"])
        assert result["keyword_operators"] == sorted(result["keyword_operators"])

    @pytest.mark.skipif(not FLEX_PATH.exists(), reason="Rescript.flex not found")
    def test_parses_real_flex_file(self, flex_text: str) -> None:
        result = parse_flex_keywords(flex_text)
        # The real file should have a reasonable number of keywords
        assert len(result["keywords"]) >= 10
        assert len(result["keyword_operators"]) >= 3
        # Common keywords must be present
        assert "let" in result["keywords"]
        assert "type" in result["keywords"]
        assert "module" in result["keywords"]

    def test_empty_input_returns_empty_lists(self) -> None:
        result = parse_flex_keywords("")
        assert result["keywords"] == []
        assert result["keyword_operators"] == []
        assert result["builtins"] == []
        assert result["operators"] == []


class TestParseColorScheme:
    """Tests for parse_color_scheme function."""

    @pytest.mark.skipif(not DARCULA_PATH.exists(), reason="Darcula XML not found")
    def test_parses_darcula_scheme(self) -> None:
        colors = parse_color_scheme(DARCULA_PATH)
        assert isinstance(colors, dict)
        # Should contain at least some ReScript-specific color keys
        rescript_keys = [k for k in colors if k.startswith("RESCRIPT_")]
        assert len(rescript_keys) >= 1

    @pytest.mark.skipif(not DEFAULT_PATH.exists(), reason="Default XML not found")
    def test_parses_default_scheme(self) -> None:
        colors = parse_color_scheme(DEFAULT_PATH)
        assert isinstance(colors, dict)
        rescript_keys = [k for k in colors if k.startswith("RESCRIPT_")]
        assert len(rescript_keys) >= 1

    @pytest.mark.skipif(not DARCULA_PATH.exists(), reason="Darcula XML not found")
    def test_color_values_are_hex(self) -> None:
        colors = parse_color_scheme(DARCULA_PATH)
        for key, value in colors.items():
            # Color values should be hex strings (6 chars, no # prefix)
            assert len(value) == 6, f"Color {key}={value} is not 6 chars"
            assert all(c in "0123456789ABCDEFabcdef" for c in value), (
                f"Color {key}={value} is not valid hex"
            )


class TestGenerateLexerModule:
    """Tests for generate_lexer_module function."""

    def test_generates_valid_python(self, sample_flex_text: str) -> None:
        tokens = parse_flex_keywords(sample_flex_text)
        darcula = {"RESCRIPT_OPERATOR": "56B6C2", "RESCRIPT_NUMBER": "6897BB"}
        default = {"RESCRIPT_OPERATOR": "666666", "RESCRIPT_NUMBER": "0000FF"}
        source = generate_lexer_module(tokens, darcula, default)
        # Should be valid Python — compile() will raise SyntaxError if not
        compile(source, "<generated>", "exec")

    def test_contains_lexer_class(self, sample_flex_text: str) -> None:
        tokens = parse_flex_keywords(sample_flex_text)
        source = generate_lexer_module(tokens, {}, {})
        assert "class RescriptLexer" in source

    def test_contains_darcula_style(self, sample_flex_text: str) -> None:
        tokens = parse_flex_keywords(sample_flex_text)
        source = generate_lexer_module(tokens, {}, {})
        assert "class RescriptDarculaStyle" in source

    def test_contains_default_style(self, sample_flex_text: str) -> None:
        tokens = parse_flex_keywords(sample_flex_text)
        source = generate_lexer_module(tokens, {}, {})
        assert "class RescriptDefaultStyle" in source

    def test_contains_keywords(self, sample_flex_text: str) -> None:
        tokens = parse_flex_keywords(sample_flex_text)
        source = generate_lexer_module(tokens, {}, {})
        assert "'let'" in source
        assert "'type'" in source
        assert "'module'" in source

    def test_contains_auto_generated_header(self, sample_flex_text: str) -> None:
        tokens = parse_flex_keywords(sample_flex_text)
        source = generate_lexer_module(tokens, {}, {})
        assert "Auto-generated" in source
        assert "DO NOT EDIT" in source

    @pytest.mark.skipif(
        not (FLEX_PATH.exists() and DARCULA_PATH.exists() and DEFAULT_PATH.exists()),
        reason="Source files not found",
    )
    def test_full_generation_produces_valid_python(self) -> None:
        flex_text = FLEX_PATH.read_text(encoding="utf-8")
        tokens = parse_flex_keywords(flex_text)
        darcula = parse_color_scheme(DARCULA_PATH)
        default = parse_color_scheme(DEFAULT_PATH)
        source = generate_lexer_module(tokens, darcula, default)
        # Should compile without error
        compile(source, "<generated>", "exec")
