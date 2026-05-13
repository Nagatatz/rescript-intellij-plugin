// Type Narrowing Visualizer fixture.
//
// Requires `@rescript/language-server` running (so the inlay hint
// provider can fetch hover info). Each `switch` arm below should
// display a small grey inlay showing the narrowed type of the
// scrutinee — e.g. `: Loaded(int)` next to the binding in the arm.

type loadState =
  | Loading
  | Loaded(int)
  | Failed(string)

let label = (s: loadState) =>
  switch s {
  | Loading => "(empty)"
  | Loaded(n) => "n = " ++ Belt.Int.toString(n)
  | Failed(e) => "error: " ++ e
  }

type rec expr =
  | Num(int)
  | Add(expr, expr)

let rec eval = (e: expr) =>
  switch e {
  | Num(n) => n
  | Add(a, b) => eval(a) + eval(b)
  }
