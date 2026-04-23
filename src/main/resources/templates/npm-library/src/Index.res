/** Returns a greeting addressed to [name]. */
@genType
let greet = (name: string) => {
  `Hello from {{projectName}}, ${name}!`
}

/**
 * Validates an untyped JSON input before greeting. Returns an [Error] message
 * for TS/JS consumers that pass a malformed payload instead of throwing.
 */
@genType
let greetChecked = (input: JSON.t) =>
  switch Validation.parseGreetInput(input) {
  | Ok({name}) => Ok(greet(name))
  | Error(message) => Error(message)
  }

/** Exposed helpers for JS/TS consumers. */
@genType
let chunk = ListUtils.chunk

@genType
let fetchWithTimeout = Fetcher.fetchWithTimeout