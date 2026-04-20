# IntelliJ Plugin Release Flow

Implements the strict ordering defined in `.claude/rules/release.md`. The critical invariant: **`plugin.xml` `<change-notes>` and `gradle.properties` `pluginVersion` MUST be committed before the tag is created**, because both are baked into the artifact that `publishPlugin` uploads to JetBrains Marketplace. Nothing after the tag can fix a missed entry.

## When to use

- User asks to release version `X.Y.Z`
- User asks to draft change notes for an upcoming release
- Post-merge, user asks "what's next before we tag?"

## Preconditions (verify before touching any file)

- `git branch --show-current` is `main`
- `git status` is clean
- `git pull --ff-only origin main` succeeds
- CI is green on `main` (`gh run list --branch main --limit 1 --json conclusion,status`)
- Target version is SemVer-valid (`MAJOR.MINOR.PATCH`) and strictly greater than current `pluginVersion` in `gradle.properties`

## Workflow

### 1. Collect change-log inputs

```bash
PREV_TAG=$(git describe --tags --abbrev=0)
git log "$PREV_TAG"..HEAD --pretty=format:'%s'
```

Classify each commit by its emoji prefix (per `.claude/rules/git-conventions.md`):

| Emoji | Category heading |
|-------|------------------|
| ✨ | New Features |
| 🐛 | Bug Fixes |
| ♻️ | Refactoring |
| ⚡ | Performance |
| 🔧 / ⬆ | Infrastructure |

Drop categories with no entries. Translate each commit subject into a user-facing English sentence that explains the value, not the mechanics. **Never ship Japanese in release notes** (`.claude/rules/language.md`).

### 2. Update `<change-notes>` in `src/main/resources/META-INF/plugin.xml`

Insert at the top of the existing `<change-notes>` block:

```xml
<h3>X.Y.Z</h3>
<h4>New Features</h4>
<ul>
    <li>User-facing sentence explaining the value.</li>
</ul>
<h4>Bug Fixes</h4>
<ul>
    <li>Symptom + fix in one sentence.</li>
</ul>
```

Only include `<h4>` sections that have at least one entry.

### 3. Bump `pluginVersion` in `gradle.properties`

```
pluginVersion=X.Y.Z
```

### 4. Update Kover coverage ratchet

```bash
./gradlew test koverHtmlReport
# Open build/reports/kover/html/index.html and note the INSTRUCTION coverage percentage.
```

Then in `build.gradle.kts`, update `kover { reports { verify { rule { bound { minValue = <measured - 3> } } } } }`:

- Ratchet rule: `minValue` must **never decrease** across releases.
- If measured coverage fell below the existing floor, add tests **before** releasing; do not lower the floor.

### 5. Verify the build

```bash
./gradlew clean buildPlugin
```

Must succeed with no new warnings. `buildPlugin` exercises `verifyPluginStructure`; failures here are blockers.

### 6. Commit the bump (single commit)

```bash
git add gradle.properties src/main/resources/META-INF/plugin.xml build.gradle.kts
git commit -m "⬆ Bump version to X.Y.Z"
```

The commit emoji is `⬆` (per user preference in memory), not `🔧`.

### 7. Create an annotated tag

The tag message becomes the release narrative — include the categorized change-log from step 1. **Annotated, never lightweight.**

```bash
git tag -a vX.Y.Z -m "$(cat <<'EOF'
## New Features
- …

## Bug Fixes
- …

**Full Changelog**: https://github.com/<owner>/<repo>/compare/vPREV...vX.Y.Z
EOF
)"
```

### 8. Push commit + tag together

```bash
git push origin main vX.Y.Z
```

Atomic push is mandatory. Never push the tag without the commit (Marketplace publish would fail) and never push the commit without the tag (skips the release workflow).

### 9. Rewrite the GitHub Release notes

`release.yml` runs with `generate_release_notes: true`, producing an auto-generated commit list. Replace it with the hand-written, categorized English notes:

```bash
gh release edit vX.Y.Z --notes "$(cat <<'EOF'
## New Features
- **Headline feature** — one-sentence elaboration.

## Bug Fixes
- …

## Refactoring
- …

## Infrastructure
- …

**Full Changelog**: https://github.com/<owner>/<repo>/compare/vPREV...vX.Y.Z
EOF
)"
```

### 10. Verify Marketplace publish

`release.yml` runs `publishPlugin`. Confirm:

- `gh run list --workflow release.yml --limit 1` shows `completed / success`
- JetBrains Marketplace plugin page lists version `X.Y.Z` (may lag a few minutes)

## Hard stops (abort and fix before continuing)

- `plugin.xml` `<change-notes>` was not updated → the Marketplace version will have a blank changelog forever. Revert the tag, fix, re-tag.
- Lightweight tag (`git tag vX.Y.Z` without `-a`) was created → delete locally and remotely, recreate with `-a`.
- Pushed the tag without the commit → `git push origin main` immediately; Marketplace publish will then succeed on retry.
- Kover `minValue` was lowered → revert, add tests, re-release.

## Reference

- Rule: `.claude/rules/release.md`
- Emoji prefixes: `.claude/rules/git-conventions.md`
- Language policy: `.claude/rules/language.md`
- Workflow: `.github/workflows/release.yml`
