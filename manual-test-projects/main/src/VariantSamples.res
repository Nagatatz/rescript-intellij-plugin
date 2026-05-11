// Variant fixture for two intentions:
//
//   1. Add Missing Switch Arms — caret inside `render`'s body, `Alt+Enter`.
//      Should insert `| Loaded(_) => todo` and `| Failed(_) => todo`.
//   2. Rename Variant Constructor — caret on `Loading` below (UIDENT),
//      `Alt+Enter`, "Rename variant constructor". Should also reach
//      occurrences in `VariantUsage.res` (4 total, 2 files).

type loadState<'a> =
  | Loading
  | Loaded('a)
  | Failed(string)

let render = (state: loadState<int>) =>
  switch state {
  | Loading => "..."
  // intentionally missing: Loaded(_), Failed(_) — fix via Alt+Enter
  }

let describe = (state: loadState<string>) =>
  switch state {
  | Loaded(s) => "loaded: " ++ s
  | _ => "other"
  }

let pendingValue = Loading
let successValue = Loaded(42)
let failureValue = Failed("boom")
