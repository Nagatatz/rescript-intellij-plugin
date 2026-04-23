// Entry point. Builds a greeting from CLI arguments, optionally reads a text
// file to append its first line, and — if `--config <path>` is supplied —
// validates that JSON file's shape via `Validation.parseConfig` before using
// `greeting` / `repeat` to drive the output.
let name = Args.positional()->Array.get(0)->Option.getOr("world")

let run = async () => {
  let greeting = switch Args.named("--config") {
  | Some(path) =>
    try {
      let raw = await Files.read(path)
      switch Validation.parseConfig(raw->JSON.parseExn) {
      | Ok({greeting, repeat}) =>
        let line = `${greeting}, ${name}! — welcome to {{projectName}}.`
        Array.make(~length=repeat->Float.toInt, line)->Array.join("\n")
      | Error(message) =>
        Console.error(`config: ${message}`)
        `Hello, ${name}! — welcome to {{projectName}}.`
      }
    } catch {
    | JsExn(err) =>
      Console.error(`Could not read ${path}: ${err->JsExn.message->Option.getOr("unknown error")}`)
      `Hello, ${name}! — welcome to {{projectName}}.`
    }
  | None => `Hello, ${name}! — welcome to {{projectName}}.`
  }
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