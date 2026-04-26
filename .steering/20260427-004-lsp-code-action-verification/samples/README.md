# LSP Code Action Verification Samples

Each `.res` file in this directory exercises a single LSP code action returned
by `@rescript/language-server`. They are intentionally minimal so the
diagnostic that triggers the code action is unambiguous.

## How to use

1. `./gradlew runIde` to launch the sandbox IDE.
2. Create a Basic ReScript project from the New Project wizard.
3. Copy these samples into `src/` of the sandbox project.
4. Run `npm install` to install `@rescript/language-server` and `rescript`.
5. Open each sample, place the caret as described in the file's header
   comment, and trigger `Alt+Enter`.
6. Record the displayed quick fix and applied edit in `../findings.md`.

## File index

| File | LSP code action |
|---|---|
| `01_missing_cases.res` | `simpleAddMissingCases` |
| `02_wrap_in_some.res` | `wrapInSome` / `unwrapOptional` |
| `03_record_missing_fields.res` | `addUndefinedRecordFields` (V10/V11) |
| `04_simple_conversion.res` | `simpleConversion` |
| `05_did_you_mean.res` | `didYouMean` |
| `06_remove_unused.res` | `removeUnusedCode` (reanalyze) |
| `07_extract_local_module.res` | `extractLocalModuleToFile` |
| `08_expand_catch_all.res` | `expandCatchAllPatterns` |
| `09_apply_uncurried.res` | `applyUncurried` (ReScript v10/v11 curried mode) |
