// JS Interop Risk Map fixture.
//
// Open the "ReScript Interop Risk" tool window. Each call site below
// should show up in the panel with a HIGH / MEDIUM / LOW risk score:
//
//   HIGH    %raw / %%raw / Obj.magic — the type system is bypassed
//   MEDIUM  untyped or coerced externals
//   LOW     well-typed @module / @bs.module externals

%%raw(`console.log("module init")`)

let unsafeMath = (): int => %raw(`Math.floor(Math.random() * 100)`)

external rawNode: int => int = "rawNode"

@module("fs") external readFileSync: string => string = "readFileSync"

@bs.module("path") external join: (string, string) => string = "join"

let coerced: int = Obj.magic("not really an int")
