// Verification sample for `simpleAddMissingCases` LSP code action.
// Place caret on the `switch` line and trigger Alt+Enter.
// Expected: quick fix to insert remaining cases (South, East, West).
type direction = North | South | East | West

let describe = (d: direction) =>
  switch d {
  | North => "up"
  }
