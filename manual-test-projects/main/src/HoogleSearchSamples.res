// Hoogle-style Type Signature Search fixtures.
//
// Open Search Everywhere with `Shift+Shift`, switch to the
// "ReScript Types" tab, then try:
//
//   string => int             →  matches stringLength (EXACT)
//   (int, int) => int         →  matches add, sub (EXACT)
//   array<'a> => option<'a>   →  matches head, last (TVAR_MATCH)
//   option<'a> => 'a          →  matches unwrap (TVAR_MATCH)
//   => option<'a>             →  return-type mode, matches anything
//                                returning an option (PARTIAL)
//   => result<int, string>    →  matches mapResult (PARTIAL)

let stringLength: string => int = s => String.length(s)

let add: (int, int) => int = (a, b) => a + b
let sub: (int, int) => int = (a, b) => a - b

let head: array<'a> => option<'a> = arr =>
  if Array.length(arr) == 0 {
    None
  } else {
    Some(arr->Array.getUnsafe(0))
  }

let last: array<'a> => option<'a> = arr => {
  let n = Array.length(arr)
  if n == 0 {
    None
  } else {
    Some(arr->Array.getUnsafe(n - 1))
  }
}

let unwrap: option<'a> => 'a = opt =>
  switch opt {
  | Some(v) => v
  | None => assert(false)
  }

let mapResult: (result<int, string>, int => int) => result<int, string> = (r, f) =>
  switch r {
  | Ok(v) => Ok(f(v))
  | Error(e) => Error(e)
  }

external rawConcat: (string, string) => string = "concat"
