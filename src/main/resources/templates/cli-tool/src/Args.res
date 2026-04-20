// Flag-parsing helpers backed by `process.argv`. Tested via Vitest.
@val external argv: array<string> = "process.argv"

/** Returns arguments excluding the node binary and script path. */
let positional = () => argv->Array.sliceToEnd(~start=2)

/** Splits the first positional argument (subcommand) from the remaining args. */
let splitSubcommand = (args: array<string>): (option<string>, array<string>) =>
  switch args {
  | [] => (None, [])
  | _ => (args->Array.get(0), args->Array.sliceToEnd(~start=1))
  }

/** Returns `true` if [flag] appears anywhere in [args]. */
@genType
let hasFlag = (args: array<string>, flag: string): bool =>
  args->Array.some(a => a === flag)

/** Reads the value following [flag] (returns None if [flag] is absent). */
@genType
let namedValue = (args: array<string>, flag: string): option<string> =>
  switch args->Array.indexOf(flag) {
  | -1 => None
  | index => args->Array.get(index + 1)
  }