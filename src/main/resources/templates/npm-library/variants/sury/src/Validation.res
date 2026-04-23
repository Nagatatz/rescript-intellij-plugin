// Runtime validation for public API inputs using sury. `Index.greetChecked` calls
// `parseGreetInput` so TS/JS consumers that pass untyped payloads get an error
// result instead of a TypeError deep inside the library.
@genType
type greetInput = {name: string}

let greetInputSchema: S.t<greetInput> = S.object(s => {
  name: s.field("name", S.string),
})

@genType
let parseGreetInput = (json: JSON.t): result<greetInput, string> =>
  try Ok(json->S.parseOrThrow(greetInputSchema)) catch {
  | S.Error(err) => Error(err.message)
  }
