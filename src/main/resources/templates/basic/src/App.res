// Entry point. Builds a greeting from CLI arguments and optionally
// reads a text file to append its first line.
let name = Args.positional()->Array.get(0)->Option.getOr("world")

let run = async () => {
  let greeting = `Hello, ${name}! — welcome to {{projectName}}.`
  Console.log(greeting)

  switch Args.named("--file") {
  | Some(path) =>
    try {
      let contents = await Files.read(path)
      Console.log(`First line of ${path}:`)
      Console.log(contents->String.split("\n")->Array.get(0)->Option.getOr(""))
    } catch {
    | JsExn(err) =>
      Console.error(`Could not read ${path}: ${err->JsExn.message->Option.getOr("unknown error")}`)
    }
  | None => ()
  }
}

run()->ignore