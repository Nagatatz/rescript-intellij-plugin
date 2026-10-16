// Verification sample for `expandCatchAllPatterns` LSP code action.
// Place caret on the `_` catch-all pattern and trigger Alt+Enter.
// Expected: quick fix that expands `_` into individual variant constructors.
type color = Red | Green | Blue

let toString = (c: color) =>
  switch c {
  | Red => "red"
  | _ => "other"
  }
