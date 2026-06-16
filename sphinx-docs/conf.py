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

# The short X.Y version and the full version, kept in sync with
# gradle.properties `pluginVersion`.
version = "0.1.15"
release = "0.1.15"

# -- General configuration ---------------------------------------------------

extensions = [
    "myst_parser",
    "sphinx_copybutton",
    "sphinx_design",
    "sphinxext.opengraph",
    "sphinx_sitemap",
    "notfound.extension",
    "sphinx_tippy",
    "sphinx_last_updated_by_git",
    "sphinx_llms_txt",
    "sphinxcontrib.budoux",
    "atsphinx.htmx_boost",
    "sphinxcontrib.mermaid",
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

_github_repo_url = "https://github.com/Nagatatz/rescript-intellij-plugin"

html_theme_options = {
    "sidebar_hide_name": False,
    "navigation_with_keys": True,
    "top_of_page_button": "edit",
    "source_repository": _github_repo_url,
    "source_branch": "main",
    "source_directory": "sphinx-docs/",
    "footer_icons": [
        {
            "name": "GitHub",
            "url": _github_repo_url,
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
html_css_files = ["css/custom.css", "css/keymap-visualizer.css", "css/settings-generator.css"]
html_js_files = ["js/keymap-visualizer.js", "js/settings-generator.js"]
templates_path = ["_templates"]

# Site prefix for GitHub Pages (e.g., "/rescript-intellij-plugin")
# Set SPHINX_SITE_PREFIX env var for deployment; empty for local dev.
html_context = {
    "site_prefix": os.environ.get("SPHINX_SITE_PREFIX", ""),
}

# Pagefind search page (replaces default Sphinx search)
html_additional_pages = {"search": "search.html"}

# -- Open Graph (social sharing previews) -----------------------------------

# `html_baseurl` and `ogp_site_url` are finalised in `setup(app)` below so
# that `make build-ja` (which passes `-D language=ja`) routes OGP and sitemap
# URLs to the `/ja/` prefix instead of the `/en/` prefix.
html_baseurl = "https://nagatatz.github.io/rescript-intellij-plugin/en/"
ogp_site_url = html_baseurl
ogp_site_name = "ReScript IntelliJ Plugin"
ogp_type = "website"

# Augment automatic Open Graph tags with Twitter Card and theme metadata.
# `og:locale` is appended per language in `setup(app)` below.
ogp_custom_meta_tags = [
    '<meta name="twitter:card" content="summary_large_image" />',
    '<meta name="theme-color" content="#E6484F" />',
]

# Map Sphinx language codes to Open Graph locale identifiers (BCP 47 → POSIX).
_OGP_LOCALES = {"en": "en_US", "ja": "ja_JP"}

# -- Sitemap (SEO) -----------------------------------------------------------

sitemap_url_scheme = "{link}"
sitemap_locales = ["en", "ja"]

# -- 404 page ----------------------------------------------------------------

notfound_urls_prefix = os.environ.get("SPHINX_SITE_PREFIX", "") + "/en/"

# -- Tooltip previews (sphinx-tippy) -----------------------------------------

tippy_anchor_parent_selector = "div.content"
tippy_enable_mathjax = False

# -- Last updated by git -----------------------------------------------------

git_last_updated_timezone = "Asia/Tokyo"

# -- LLM documentation (llms.txt) --------------------------------------------

# URI template uses html_baseurl automatically; no override needed

# -- BudouX (Japanese line breaking) -----------------------------------------

budoux_targets = ["h1", "h2", "h3"]

# -- HTMX Boost (SPA-like page transitions) ----------------------------------

htmx_boost_preload = "mouseover"

# Suppress toctree warnings for locale files
suppress_warnings = ["toc.excluded"]

# -- Link check --------------------------------------------------------------

# Flaky external sites (React Native docs, etc.) frequently time out in CI.
# Double the default 30s timeout and retry up to three times so transient
# failures don't red the whole pipeline, while still surfacing genuinely
# broken links.
linkcheck_timeout = 60
linkcheck_retries = 3

# Endpoints that consistently time out or rate-limit CI runners. Keep this
# list short and review it when upstream improves.
linkcheck_ignore = [
    # reactnative.dev intermittently times out under the linkcheck bot
    # (both the legacy docs path and the bare landing page have caused CI
    # reds). Ignore the whole host since we only link to it for reference.
    r"^https://reactnative\.dev(/.*)?$",
    # Local dev server URLs documented in template guides — unreachable from CI.
    r"^http://localhost(:\d+)?(/.*)?$",
    # npmjs.com returns 403 to the linkcheck bot's HEAD/GET requests.
    r"^https://www\.npmjs\.com/package/.*",
    # v2.tauri.app intermittently times out from CI runners (200 OK locally);
    # verified 2026-06-12.
    r"^https://v2\.tauri\.app(/.*)?$",
]


# -- Locale-aware OGP wiring -------------------------------------------------


def setup(app):  # noqa: D401
    """Rewrite ``html_baseurl`` / ``ogp_site_url`` and append ``og:locale``.

    Sphinx applies ``-D language=ja`` overrides *after* this module is loaded,
    so we wait for the ``config-inited`` event before reading the resolved
    language. This keeps Japanese builds (``make build-ja``) from emitting
    ``/en/`` URLs in OGP / sitemap output and ensures Open Graph scrapers
    receive the correct ``og:locale`` per language.
    """

    def _apply_locale_aware_ogp(_app, config):
        lang = (config.language or "en").split("_")[0]
        base = f"https://nagatatz.github.io/rescript-intellij-plugin/{lang}/"
        config.html_baseurl = base
        config.ogp_site_url = base
        og_locale = _OGP_LOCALES.get(lang, "en_US")
        og_locale_tag = f'<meta property="og:locale" content="{og_locale}" />'
        if og_locale_tag not in config.ogp_custom_meta_tags:
            config.ogp_custom_meta_tags = [
                *config.ogp_custom_meta_tags,
                og_locale_tag,
            ]

    app.connect("config-inited", _apply_locale_aware_ogp)
    return {"parallel_read_safe": True, "parallel_write_safe": True}
