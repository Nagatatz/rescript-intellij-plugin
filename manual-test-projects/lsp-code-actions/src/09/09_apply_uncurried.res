// Verification sample for `applyUncurried` LSP code action.
// Note: ReScript v11+ is uncurried by default and may not surface this fix.
// If running against ReScript v10/v11 with curried mode, place caret on
// `f(42)` and trigger Alt+Enter. Expected: quick fix converting to `f(.42)`.
@uncurry
let f = (. x) => x + 1

let _ = f(42)
