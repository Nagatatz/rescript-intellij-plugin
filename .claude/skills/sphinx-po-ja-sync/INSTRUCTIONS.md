# Sphinx `.po` Japanese Translation Sync

Enforce `.claude/rules/documentation.md` "日本語訳の同時更新" rule: whenever any `sphinx-docs/**/*.md` file (outside `locale/`) is added or modified, the corresponding `sphinx-docs/locale/ja/LC_MESSAGES/**/*.po` must be updated in the **same commit** with a filled-in `msgstr`.

## When to use

- Added a new `.md` under `sphinx-docs/user/features/` or `sphinx-docs/dev/`
- Edited headings, paragraphs, or examples in any tracked `sphinx-docs/**/*.md`
- `git status` shows staged `.md` changes without corresponding `.po` changes
- User asks: "update translations", "sync Japanese docs", "why is `make build-ja` failing?"

## Workflow

1. **Inspect the English change**
   - `git diff sphinx-docs/**/*.md` (outside `locale/`) to identify added or modified `msgid` candidates.

2. **Regenerate `.pot` and sync `.po`**
   ```bash
   cd sphinx-docs
   make gettext      # regenerate .pot templates from current .md
   make update-po    # merge .pot into ja .po (new msgids appended with empty msgstr)
   ```

3. **Fill every empty `msgstr`** in the touched `.po` files.
   - Keep ReST/Sphinx roles (`:ref:`, `:doc:`, backticks) byte-identical with the `msgid`.
   - Preserve leading/trailing whitespace.
   - Do not translate identifiers, code fences, class names, or English technical terms that appear in `docs/glossary.md`.
   - If a `msgid` is a section heading that maps 1:1 to an existing translated page, reuse the existing Japanese wording for consistency.

4. **Verify the Japanese build**
   ```bash
   cd sphinx-docs && make build-ja
   ```
   Must exit 0. If warnings about `msgstr` mismatches appear, fix the offending entry (usually a malformed ReST role) before committing.

5. **Stage both sides together**
   - Every `sphinx-docs/**/*.md` in the commit must be accompanied by the corresponding `.po` in `sphinx-docs/locale/ja/LC_MESSAGES/**`.
   - Use explicit `git add <paths>`; never `git add .` (per `.claude/rules/definition-of-done.md` Phase 3).

## Acceptance checks (run before committing)

- [ ] `git diff --name-only --cached | grep 'sphinx-docs.*\.md$'` non-empty ⇒ matching `.po` also staged.
- [ ] No `msgstr ""` left in touched `.po` files (`grep -nE '^msgstr ""$' <file>` returns no unexpected entries — the initial header entry is allowed).
- [ ] `make build-ja` succeeds from `sphinx-docs/`.

## Allowed diff shape for `.po`

Minor churn that does NOT require new translation:

- `POT-Creation-Date:` header bumps
- Source reference line shifts (`#: user/features/foo.md:42` → `:45`)

Any new or changed `msgid` requires a Japanese `msgstr`.

## Anti-patterns

- Committing an English `.md` change and deferring the `.po` update to a follow-up commit.
- Translating code identifiers, CLI flags, file paths, or technical terms present in `docs/glossary.md`.
- Leaving `msgstr ""` because "the English is clear enough" — build-ja will still pass but the user-facing Japanese page will show English fallbacks.

## Reference

- Rule: `.claude/rules/documentation.md` → "日本語訳の同時更新"
- Glossary (do-not-translate terms): `docs/glossary.md`
- Makefile targets: `sphinx-docs/Makefile` (`gettext`, `update-po`, `build-ja`, `serve`)
