# LSP Code Action verification

Fixtures for [issue #92](https://github.com/Nagatatz/rescript-intellij-plugin/issues/92),
restored unchanged from the parent of commit `8bfdb4ef`.
This project isolates the nine cases from the other manual test projects.

```bash
cd manual-test-projects/lsp-code-actions
npm install
npm run watch
```

From the repository root, run `./gradlew runIde` and open this directory in
the sandbox IDE. Wait for the language server to connect and for diagnostics
to appear. Compiler errors in these fixtures are intentional: they trigger
the fixes under test. The project enables reanalyze dead-code analysis.

Only one fixture directory is compiled at a time. Before testing each case,
set the `dir` in `rescript.json` to `src/01` through `src/09`, then rebuild
(or restart the watcher). This keeps the intentional errors in other cases
from interfering with diagnostics and reanalyze. Case 06 must build successfully
before checking the unused-code diagnostic; case 07 permits a new `Inner.res`
in the same source directory.

For each file, place the caret at the location described in its header,
open Alt+Enter, record whether the LSP action appears, apply it, and inspect
the resulting edit. Distinguish LSP actions from native intentions with similar
names. Undo edits before repeating a test; remove `Inner.res` after checking
module extraction. Record the IDE, plugin, Node.js, ReScript, and LSP versions
and relevant `idea.log` errors in issue #92.

| File | Action | Expected edit |
| --- | --- | --- |
| `01_missing_cases.res` | `simpleAddMissingCases` | Add South, East, and West branches |
| `02_wrap_in_some.res` | `wrapInSome` | Replace `42` with `Some(42)` |
| `03_record_missing_fields.res` | `addUndefinedRecordFields` | Add age and email fields |
| `04_simple_conversion.res` | `simpleConversion` | Wrap the string with a conversion helper |
| `05_did_you_mean.res` | `didYouMean` | Replace myValu with myValue |
| `06_remove_unused.res` | `removeUnusedCode` | Remove unusedFunction after a reanalyze diagnostic |
| `07_extract_local_module.res` | `extractLocalModuleToFile` | Create Inner.res and move the module body |
| `08_expand_catch_all.res` | `expandCatchAllPatterns` | Expand the catch-all into Green and Blue |
| `09_apply_uncurried.res` | `applyUncurried` | Convert a curried call, or record N/A if unavailable in this compiler version |

Record display and application separately using OK / NG / PARTIAL / N/A.
A successful compiler or protocol check alone does not establish that the
action displays and applies in the IDE.

## Result record

The nine cases have **not yet been verified in the sandbox IDE**. Keep an
unobserved case as `Pending`; use N/A only after checking that the action cannot
be triggered with the selected compiler version. Fill in this table and copy
the observed results to issue #92.

| Action ID | Display | Apply | Evidence / follow-up issue |
| --- | --- | --- | --- |
| `simpleAddMissingCases` | Pending | Pending | |
| `wrapInSome` | Pending | Pending | |
| `addUndefinedRecordFields` | Pending | Pending | |
| `simpleConversion` | Pending | Pending | |
| `didYouMean` | Pending | Pending | |
| `removeUnusedCode` | Pending | Pending | |
| `extractLocalModuleToFile` | Pending | Pending | |
| `expandCatchAllPatterns` | Pending | Pending | |
| `applyUncurried` | Pending | Pending | |

Record the action label, caret position or selection, before/after source,
created files, and diagnostic changes. Use **Help > Show Log in Finder** in the
sandbox IDE to locate its `idea.log`. For each NG / PARTIAL result, include the
relevant log excerpt, suspected cause, and PSI implementation feasibility in
an individual native Quick Fix issue. A tool failing to address the sandbox
window is a verification blocker, not an NG result for the code action.
