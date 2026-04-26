// Verification sample for `removeUnusedCode` LSP code action.
// Requires reanalyze to be enabled in the sandbox project.
// Place caret on `unusedFunction` and trigger Alt+Enter once the reanalyze
// warning surfaces. Expected: quick fix that removes the unused declaration.
let unusedFunction = () => "never called"
