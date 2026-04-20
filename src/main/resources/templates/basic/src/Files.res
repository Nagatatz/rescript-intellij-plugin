// Thin bindings over Node's fs/promises API, just enough for the starter app.
type encoding = Utf8

@module("node:fs/promises")
external readFileUtf8Raw: (string, @as("utf8") _) => promise<string> = "readFile"

@module("node:fs/promises")
external writeFileUtf8Raw: (string, string, @as("utf8") _) => promise<unit> = "writeFile"

/** Reads [path] as UTF-8 text. Rejects if the file does not exist. */
let read = (path: string): promise<string> => readFileUtf8Raw(path)

/** Writes [contents] to [path] as UTF-8. Creates or overwrites the file. */
let write = (path: string, contents: string): promise<unit> =>
  writeFileUtf8Raw(path, contents)