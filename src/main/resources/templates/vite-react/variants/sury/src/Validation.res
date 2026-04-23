// Form input validation for the greet form using sury. `App.res` calls
// `parseGreetForm` on submit to catch empty / oversized names before they
// hit the network.
type greetForm = {name: string}

let greetFormSchema: S.t<greetForm> = S.object(s => {
  name: s.field("name", S.string),
})

let parseGreetForm = (name: string): result<greetForm, string> => {
  let trimmed = name->String.trim
  if trimmed == "" {
    Error("Name cannot be empty")
  } else if trimmed->String.length > 80 {
    Error("Name cannot exceed 80 characters")
  } else {
    let payload = Dict.fromArray([("name", JSON.Encode.string(trimmed))])->JSON.Encode.object
    try Ok(payload->S.parseOrThrow(greetFormSchema)) catch {
    | S.Error(err) => Error(err.message)
    }
  }
}
