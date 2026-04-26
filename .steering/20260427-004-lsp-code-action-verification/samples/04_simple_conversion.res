// Verification sample for `simpleConversion` LSP code action.
// Place caret on `toInt("42")` and trigger Alt+Enter.
// Expected: quick fix wrapping the string with an int conversion helper.
let toInt: int => unit = _ => ()

let _ = toInt("42")
