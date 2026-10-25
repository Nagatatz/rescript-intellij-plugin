// Verification sample for `extractLocalModuleToFile` LSP code action.
// Place caret on the `Inner` module declaration and trigger Alt+Enter.
// Expected: quick fix that extracts `Inner` into a new `Inner.res` file.
module Inner = {
  let value = 42
  let double = x => x * 2
}

let _ = Inner.double(Inner.value)
