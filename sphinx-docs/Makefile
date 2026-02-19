SPHINXOPTS    ?=
SPHINXBUILD   ?= uv run sphinx-build
SPHINXINTL    ?= uv run sphinx-intl
SOURCEDIR     = .
BUILDDIR      = _build

.PHONY: help install clean html gettext update-po build-ja build-all serve linkcheck generate-lexer

help:
	@echo "Usage:"
	@echo "  make install         Install dependencies via uv"
	@echo "  make generate-lexer  Generate Pygments lexer from Rescript.flex"
	@echo "  make html            Build English HTML"
	@echo "  make gettext         Extract translatable strings (.pot)"
	@echo "  make update-po       Update Japanese .po files from .pot"
	@echo "  make build-ja        Build Japanese HTML"
	@echo "  make build-all       Build EN + JA and assemble final site"
	@echo "  make serve           Serve the built site on localhost:8000"
	@echo "  make clean           Remove build artifacts"
	@echo "  make linkcheck       Check for broken links"

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

build-all: html build-ja
	@echo "Assembling final site..."
	rm -rf $(BUILDDIR)/site
	mkdir -p $(BUILDDIR)/site
	cp -r $(BUILDDIR)/html $(BUILDDIR)/site/en
	cp -r $(BUILDDIR)/html_ja $(BUILDDIR)/site/ja
	@echo '<!DOCTYPE html><html><head><meta http-equiv="refresh" content="0;url=en/"></head><body><a href="en/">English</a></body></html>' > $(BUILDDIR)/site/index.html
	@echo "Site assembled at $(BUILDDIR)/site/"

serve: build-all
	@echo "Serving at http://localhost:8000"
	uv run python -m http.server 8000 --directory $(BUILDDIR)/site

linkcheck: generate-lexer
	$(SPHINXBUILD) -b linkcheck $(SPHINXOPTS) $(SOURCEDIR) $(BUILDDIR)/linkcheck
