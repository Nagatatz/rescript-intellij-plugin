# Configuration file for the Sphinx documentation builder.
# https://www.sphinx-doc.org/en/master/usage/configuration.html

import os
import sys
from pathlib import Path

# Add _ext to Python path for custom extensions
sys.path.insert(0, str(Path(__file__).parent / "_ext"))

project = "ReScript IntelliJ Plugin"
copyright = "2026, Nagatatz"
author = "Claude Code with Nagatatz"

# -- General configuration ---------------------------------------------------

extensions = [
    "myst_parser",
    "sphinx_copybutton",
    "sphinx_design",
]

# MyST Parser settings
myst_enable_extensions = [
    "colon_fence",
    "deflist",
    "fieldlist",
    "attrs_inline",
]

# Auto-generate heading anchors up to level 3 (h1-h3)
myst_heading_anchors = 3

# Source file suffixes
source_suffix = {
    ".md": "markdown",
}

# The master toctree document
master_doc = "index"

# Exclude patterns
exclude_patterns = ["_build", ".venv", ".pytest_cache", "Thumbs.db", ".DS_Store"]

# -- Internationalization ----------------------------------------------------

language = "en"
locale_dirs = ["locale/"]
gettext_compact = False  # One .po file per source document

# -- ReScript Pygments lexer & styles (auto-generated from Rescript.flex) ----

from rescript_lexer import (  # noqa: E402, F401
    RescriptDarculaStyle,
    RescriptDefaultStyle,
    RescriptLexer,
)
from sphinx.highlighting import lexer_classes  # noqa: E402

# Register the custom ReScript lexer
lexer_classes["rescript"] = RescriptLexer

# -- HTML output -------------------------------------------------------------

html_theme = "furo"

html_theme_options = {
    "sidebar_hide_name": False,
    "navigation_with_keys": True,
    "footer_icons": [
        {
            "name": "GitHub",
            "url": "https://github.com/Nagatatz/rescript-intellij-plugin",
            "html": '<svg stroke="currentColor" fill="currentColor" stroke-width="0" '
            'viewBox="0 0 16 16"><path fill-rule="evenodd" d="M8 0C3.58 0 0 3.58 0 8c0 '
            "3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37"
            "-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 "
            "1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64"
            "-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 "
            "2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82"
            ".44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95"
            ".29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013"
            ' 0 0016 8c0-4.42-3.58-8-8-8z"></path></svg>',
            "class": "",
        },
    ],
}

# Use plugin-matching Pygments styles for light/dark modes
pygments_style = "rescript_lexer.RescriptDefaultStyle"
pygments_dark_style = "rescript_lexer.RescriptDarculaStyle"

html_static_path = ["_static"]
html_css_files = ["css/custom.css"]
templates_path = ["_templates"]

# Site prefix for GitHub Pages (e.g., "/rescript-intellij-plugin")
# Set SPHINX_SITE_PREFIX env var for deployment; empty for local dev.
html_context = {
    "site_prefix": os.environ.get("SPHINX_SITE_PREFIX", ""),
}

# Pagefind search page (replaces default Sphinx search)
html_additional_pages = {"search": "search.html"}

# Suppress toctree warnings for locale files
suppress_warnings = ["toc.excluded"]
