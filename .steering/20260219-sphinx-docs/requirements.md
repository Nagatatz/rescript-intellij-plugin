# Requirements: Sphinx Documentation Site

## Overview

Build a Sphinx-based documentation site for the ReScript IntelliJ Plugin, providing both user-facing and developer-facing documentation. The site uses English as the source language with Japanese translation support via sphinx-intl (gettext), and is deployed to GitHub Pages.

## Background

- The existing `docs/` directory contains internal project design documents (product-requirements.md, functional-design.md, development-guidelines.md) written in Japanese. These are **not** intended as public-facing documentation.
- There is currently no user guide, installation guide, or contributor documentation.
- The plugin has grown to include 60+ features and needs structured documentation for both users and contributors.

## Goals

1. **User documentation** — Help plugin users install, configure, and use all features effectively
2. **Developer documentation** — Help contributors understand the architecture, build the project, and add new features
3. **Internationalization** — Support English (primary) and Japanese (secondary) languages
4. **Automated deployment** — Publish to GitHub Pages via GitHub Actions on every push to `main`

## Functional Requirements

### FR-1: Sphinx Infrastructure
- Use Sphinx with MyST Parser (Markdown-based authoring)
- Use Furo theme (modern, dark mode support, responsive)
- Configure sphinx-intl for gettext-based i18n
- Provide a `Makefile` with targets: `html`, `gettext`, `update-po`, `build-ja`, `build-all`, `serve`, `install`, `clean`, `linkcheck`

### FR-2: User Documentation (English)
- Installation guide (plugin install + LSP setup)
- Quick start guide (first-time usage flow)
- Feature guides organized by category:
  - Syntax highlighting
  - Code completion (LSP)
  - Navigation (Go to Definition, Symbol, Related, etc.)
  - Code editing (folding, formatting, surround, intentions, etc.)
  - Run & Build (run configurations, gutter icons, build status)
  - Testing (test runner integration)
  - Code analysis (inspections, reanalyze, import optimization)
  - Advanced features (Code Lens, Compiled JS Preview, Module Hierarchy, etc.)
- Configuration guide (Settings UI, project settings)
- ReScript basics (project setup, basic syntax reference for newcomers)
- Keyboard shortcuts reference
- Troubleshooting / FAQ
- Changelog

### FR-3: Developer Documentation (English)
- Architecture overview (hybrid lexer + LSP approach)
- Development environment setup (JDK 21+, Gradle, IDE)
- Build & run commands
- Project structure guide
- Extending the plugin (Extension Point patterns)
- Testing guide
- Contributing guide

### FR-4: Internationalization (i18n)
- Extract translatable strings via gettext (.pot files)
- Generate Japanese .po files via sphinx-intl
- Translate key pages as proof-of-concept: index, installation, quickstart
- Language switcher UI in the page footer (EN ↔ JA)
- URL structure: `/en/` and `/ja/` subdirectories
- Root `index.html` redirects to `/en/`

### FR-5: GitHub Actions Deployment
- Workflow triggers on changes to `sphinx-docs/**` on `main` branch
- Build both English and Japanese HTML in parallel
- Deploy using `actions/deploy-pages@v4`
- On pull requests: run link check only (no deployment)

### FR-6: Language Switcher
- Footer-based EN/JA toggle links
- Symmetric URL mapping: `/en/page.html` ↔ `/ja/page.html`
- Current language highlighted
- Minimal JavaScript (hash fragment preservation only)

## Non-Functional Requirements

### NFR-1: Maintainability
- Markdown (MyST) for easy editing by contributors
- Content sourced from existing internal docs where applicable (no duplication of effort)
- Clear separation between Sphinx docs (`sphinx-docs/`) and internal docs (`docs/`)

### NFR-2: Accessibility
- Responsive design (Furo theme)
- Dark mode support (built-in with Furo)
- Semantic HTML structure

### NFR-3: Performance
- Static HTML output (fast loading)
- No heavy JavaScript dependencies

## Constraints

- Existing `docs/` directory must remain untouched (internal design documents)
- All Sphinx files go under `sphinx-docs/` directory
- Python dependencies managed via `sphinx-docs/requirements.txt`
- Documentation content is written in English first; Japanese translations follow

## Out of Scope

- Full Japanese translation of all pages (only key pages as PoC)
- Custom Sphinx extensions
- Search functionality customization (use Sphinx built-in)
- PDF/ePub output formats
