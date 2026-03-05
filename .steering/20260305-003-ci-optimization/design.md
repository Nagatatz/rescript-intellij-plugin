# Design: CI Optimization

## 5A. Merge verify Job into build Job

The `verify` job (ci.yml lines 120-138) duplicates checkout + JDK + Gradle setup. Move `./gradlew verifyPlugin` into the `build` job after the existing `verifyPluginProjectConfiguration` step. Delete the entire `verify:` job block.

## 5B. Reorder Kover Tasks

Change line 54 from:
  ./gradlew test koverXmlReport koverHtmlReport koverVerify
to:
  ./gradlew test koverVerify koverXmlReport koverHtmlReport

This fails faster if coverage threshold is not met.

## 5C. GrammarKit Version

Check if a newer version of org.jetbrains.grammarkit exists. The current version is 2023.3.0.2. Document findings.

### GrammarKit Version Check Results

- Current version: `2023.3.0.2`
- Latest version: `2023.3.0.3` (released 2026-03-02)
- A minor patch update is available. This is a non-breaking change and can be updated separately.
