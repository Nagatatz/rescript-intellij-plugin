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
  // `init <project-name>` — demonstrates a subcommand that would normally
  // scaffold files. In this template it just logs what it would do.
  let run = (args: array<string>) => {
    switch args->Array.get(0) {
    | None =>
      Console.error("init: missing required argument <project-name>")
    | Some(target) =>
      Console.log(`Initializing new \"{{projectName}}\"-style project at ${target}...`)
      Console.log("(In a real CLI, this would create files on disk.)")
    }
  }
}