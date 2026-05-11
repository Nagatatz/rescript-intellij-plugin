// Cross-file occurrences for the Rename Variant Constructor intention.
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
