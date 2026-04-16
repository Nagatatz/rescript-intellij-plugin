"""Validate code examples embedded in user/recipes/*.md.

Extracts fenced code blocks tagged as ``rescript`` or ``javascript`` from each
recipe and runs lightweight syntactic checks. The goal is to catch obvious
corruption (unbalanced braces, malformed fences, empty blocks) without
requiring a full compiler in CI.
"""

from __future__ import annotations

from pathlib import Path

import pytest
from markdown_it import MarkdownIt

RECIPES_DIR = Path(__file__).parent.parent / "user" / "recipes"
CHECKED_LANGUAGES = {"rescript", "res", "javascript", "js", "ts", "typescript"}


def _recipe_files() -> list[Path]:
    return sorted(p for p in RECIPES_DIR.glob("*.md") if p.name != "index.md")


def _extract_code_blocks(md_path: Path) -> list[tuple[str, str, int]]:
    """Return (language, content, line_number) for each fenced code block."""
    md = MarkdownIt()
    tokens = md.parse(md_path.read_text())
    blocks: list[tuple[str, str, int]] = []
    for token in tokens:
        if token.type == "fence" and token.info:
            lang = token.info.strip().split()[0].lower()
            line_number = token.map[0] + 1 if token.map else 0
            blocks.append((lang, token.content, line_number))
    return blocks


def _balanced_brackets(code: str) -> bool:
    """Check `{}`, `()`, `[]` balance, ignoring characters inside strings.

    Single quotes are NOT treated as string delimiters because ReScript uses
    them for polymorphic type variables (e.g., ``'a``, ``'builder``).
    """
    pairs = {"}": "{", ")": "(", "]": "["}
    stack: list[str] = []
    in_string: str | None = None
    prev = ""
    for ch in code:
        if in_string:
            if ch == in_string and prev != "\\":
                in_string = None
        elif ch in {'"', "`"}:
            in_string = ch
        elif ch in "({[":
            stack.append(ch)
        elif ch in ")}]":
            if not stack or stack[-1] != pairs[ch]:
                return False
            stack.pop()
        prev = ch
    return not stack


@pytest.mark.parametrize("recipe", _recipe_files(), ids=lambda p: p.name)
def test_recipe_code_blocks_are_well_formed(recipe: Path) -> None:
    """Fenced code blocks for checked languages must have balanced brackets.

    Recipes without code blocks (step-only guides) are accepted as-is.
    """
    for lang, code, line in _extract_code_blocks(recipe):
        if lang not in CHECKED_LANGUAGES:
            continue
        assert code.strip(), f"{recipe.name}:{line} has empty {lang} block"
        assert _balanced_brackets(code), (
            f"{recipe.name}:{line} has unbalanced brackets in {lang} block"
        )
