# Requirements: CI Optimization

## Goals
1. Merge the separate `verify` CI job into the `build` job to eliminate ~2min of duplicate setup
2. Reorder Kover tasks for faster failure
3. Check GrammarKit version currency

## Acceptance Criteria
- [ ] CI has no separate `verify` job
- [ ] `verifyPlugin` step runs in the `build` job
- [ ] Kover tasks ordered: test → koverVerify → koverXmlReport → koverHtmlReport
- [ ] GrammarKit version documented
