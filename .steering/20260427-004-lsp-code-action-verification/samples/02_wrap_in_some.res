// Verification sample for `wrapInSome` LSP code action.
// Place caret on the diagnostic at `foo(42)` and trigger Alt+Enter.
// Expected: quick fix to wrap the argument as `Some(42)`.
let foo: option<int> => unit = _ => ()

let _ = foo(42)
