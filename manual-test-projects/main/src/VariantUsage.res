// Cross-file occurrences for the Rename Variant Constructor intention.
// This is a fixture — there is no plugin feature literally named
// "Variant Usage". For per-switch decision trees use the
// `ReScript Switch Flow` tool window; for cross-file constructor
// rename, place the caret on a UIDENT and hit Alt+Enter.
// The intention's classifier needs to recognise CONSTRUCTOR uses below
// (none should be classified as MODULE_QUALIFIED_TAIL or OTHER).

let pending = VariantSamples.Loading
let successful = VariantSamples.Loaded(7)

let summarise = (s: VariantSamples.loadState<int>) =>
  switch s {
  | VariantSamples.Loading => "wait"
  | VariantSamples.Loaded(_) => "ok"
  | VariantSamples.Failed(_) => "ng"
  }
