// Minimal CLI argument helpers backed by `process.argv`.
@val external argv: array<string> = "process.argv"

/** Returns arguments excluding the node binary and script path. */
let positional = () => argv->Array.sliceToEnd(~start=2)

/** Extracts the value that follows `--flag` (returns None if absent). */
let named = (flag: string): option<string> => {
  let args = positional()
  switch args->Array.indexOf(flag) {
  | -1 => None
  | index => args->Array.get(index + 1)
  }
}