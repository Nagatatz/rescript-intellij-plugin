// Subcommands live in nested modules so `Cli.res` can dispatch with
// `Commands.<Name>.run(args)`. Add new ones as siblings below.
module Greet = {
  // `greet <name> [--shout]` — demonstrates positional + boolean flag.
  let run = (args: array<string>) => {
    switch args->Array.get(0) {
    | None =>
      Console.error("greet: missing required argument <name>")
    | Some(name) =>
      let shout = args->Args.hasFlag("--shout")
      let greeting = `Hello, ${name}!`
      Console.log(shout ? greeting->String.toUpperCase : greeting)
    }
  }
}

module Init = {
  // `init --name <project-name> --dir <path>` — demonstrates a subcommand that
  // validates its options through `Validation.parseInitOptions` before doing
  // any work. Swap the `Validation` backend (zod / sury) in the Wizard to see
  // how each library reports bad input.
  let run = (args: array<string>) => {
    let raw =
      Dict.fromArray([
        ("name", args->Args.namedValue("--name")->Option.map(JSON.Encode.string)->Option.getOr(JSON.Encode.null)),
        ("dir", args->Args.namedValue("--dir")->Option.map(JSON.Encode.string)->Option.getOr(JSON.Encode.null)),
      ])->JSON.Encode.object
    switch Validation.parseInitOptions(raw) {
    | Error(message) => Console.error(`init: ${message}`)
    | Ok({name, dir}) =>
      Console.log(`Initializing new \"{{projectName}}\"-style project "${name}" at ${dir}...`)
      Console.log("(In a real CLI, this would create files on disk.)")
    }
  }
}