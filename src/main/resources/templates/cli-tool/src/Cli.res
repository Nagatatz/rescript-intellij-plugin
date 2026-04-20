// Entry point: reads argv, dispatches to a subcommand, or prints usage.
let rec run = () => {
  let allArgs = Args.positional()
  let (subcommand, remaining) = Args.splitSubcommand(allArgs)

  switch subcommand {
  | Some("greet") => Commands.Greet.run(remaining)
  | Some("init") => Commands.Init.run(remaining)
  | Some("--help") | Some("-h") | None => printUsage()
  | Some(cmd) =>
    Console.error(`Unknown subcommand: ${cmd}`)
    printUsage()
  }
}

and printUsage = () => {
  Console.log("Usage: {{projectName}} <command> [options]")
  Console.log("")
  Console.log("Commands:")
  Console.log("  greet <name> [--shout]   Say hello to someone")
  Console.log("  init <project-name>      Scaffold a new project")
  Console.log("  --help, -h               Show this help message")
}

run()