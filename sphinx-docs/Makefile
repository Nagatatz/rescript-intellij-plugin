SPHINXOPTS    ?=
SPHINXBUILD   ?= uv run sphinx-build
SPHINXINTL    ?= uv run sphinx-intl
SOURCEDIR     = .
BUILDDIR      = _build

.PHONY: help install clean html gettext update-po build-ja build-all serve liveserve linkcheck generate-lexer pagefind lint typecheck test check check-po a11y

help:
	@echo "Usage:"
	@echo "  make install         Install dependencies via uv"
	@echo "  make generate-lexer  Generate Pygments lexer from Rescript.flex"
	@echo "  make html            Build English HTML"
	@echo "  make gettext         Extract translatable strings (.pot)"
	@echo "  make update-po       Update Japanese .po files from .pot"
	@echo "  make build-ja        Build Japanese HTML"
	@echo "  make build-all       Build EN + JA and assemble final site with Pagefind"
	@echo "  make serve           Serve the built site on localhost:8000"
	@echo "  make liveserve       Live-reload dev server (auto-rebuilds on save)"
	@echo "  make clean           Remove build artifacts"
	@echo "  make linkcheck       Check for broken links"
	@echo "  make pagefind        Build Pagefind search index"
	@echo "  make lint            Run ruff linter and formatter check"
	@echo "  make typecheck       Run mypy type checker"
	@echo "  make test            Run pytest"
	@echo "  make check           Run all quality checks (lint + typecheck + test)"
	@echo "  make check-po        Validate .po translation files with pofilter"
	@echo "  make a11y            Run pa11y-ci accessibility audit on built site"

install:
	uv sync

generate-lexer:
	uv run python _ext/generate_rescript_lexer.py

clean:
	rm -rf $(BUILDDIR)

html: generate-lexer
	$(SPHINXBUILD) -b html $(SPHINXOPTS) $(SOURCEDIR) $(BUILDDIR)/html

gettext: generate-lexer
	$(SPHINXBUILD) -b gettext $(SPHINXOPTS) $(SOURCEDIR) $(BUILDDIR)/gettext

update-po: gettext
	$(SPHINXINTL) update -p $(BUILDDIR)/gettext -l ja

build-ja: generate-lexer
	$(SPHINXBUILD) -b html -D language=ja $(SPHINXOPTS) $(SOURCEDIR) $(BUILDDIR)/html_ja

pagefind:
	npx pagefind --site $(BUILDDIR)/site

build-all: html build-ja
	@echo "Assembling final site..."
	rm -rf $(BUILDDIR)/site
	mkdir -p $(BUILDDIR)/site
	cp -r $(BUILDDIR)/html $(BUILDDIR)/site/en
	cp -r $(BUILDDIR)/html_ja $(BUILDDIR)/site/ja
	@echo '<!DOCTYPE html><html><head><meta http-equiv="refresh" content="0;url=en/"></head><body><a href="en/">English</a></body></html>' > $(BUILDDIR)/site/index.html
	npx pagefind --site $(BUILDDIR)/site
	@echo "Site assembled at $(BUILDDIR)/site/"

serve: build-all
	@echo "Serving at http://localhost:8000"
	uv run python -m http.server 8000 --directory $(BUILDDIR)/site

liveserve: generate-lexer
	@echo "Live server at http://127.0.0.1:8000 (auto-rebuilds on changes)"
	uv run sphinx-autobuild $(SPHINXOPTS) $(SOURCEDIR) $(BUILDDIR)/html --watch _ext

linkcheck: generate-lexer
	$(SPHINXBUILD) -b linkcheck $(SPHINXOPTS) $(SOURCEDIR) $(BUILDDIR)/linkcheck

lint:
	uv run ruff check .
	uv run ruff format --check .

typecheck:
	uv run mypy _ext/generate_rescript_lexer.py

test:
	uv run pytest

check: lint typecheck test

# Validate Japanese .po translation files for serious quality issues
# (corrupted escapes/placeholders/tags/brackets). Excludes filters that
# legitimately differ between English and Japanese (punctuation, sentence
# count, quote style), and accepts untranslated/unchanged entries since
# loanwords like "LSP", "ReScript", feature names are intentionally left
# in English.
check-po:
	@echo "Validating .po files with pofilter..."
	@mkdir -p $(BUILDDIR)/pofilter
	@uv run pofilter \
		--test=escapes \
		--test=newlines \
		--test=tabs \
		--test=printf \
		--test=variables \
		--progress=none \
		--input=locale/ja/LC_MESSAGES \
		--output=$(BUILDDIR)/pofilter
	@issue_count=$$(find $(BUILDDIR)/pofilter -name '*.po' -exec awk '/^msgid "[^"]/{c++} END{print c+0}' {} \; 2>/dev/null | awk '{s+=$$1} END {print s+0}'); \
	if [ "$$issue_count" -gt 0 ]; then \
		echo "Translation issues found ($$issue_count entries):"; \
		find $(BUILDDIR)/pofilter -name '*.po' -exec sh -c 'awk "/^msgid \"[^\\\"]/{found=1} END{exit !found}" "$$1" && echo "--- $$1" && cat "$$1"' _ {} \;; \
		exit 1; \
	else \
		echo "No translation quality issues found."; \
	fi

a11y: build-all
	@echo "Starting local HTTP server for pa11y..."
	@cd $(BUILDDIR)/site && (uv run python -m http.server 8765 &) && sleep 2
	npx --yes pa11y-ci --config .pa11yci.json || (pkill -f "http.server 8765" || true; exit 1)
	@pkill -f "http.server 8765" || true
