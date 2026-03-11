// All highlight types for comprehensive highlighting test

// Keywords and control flow
let result = if true {
  switch Some(1) {
  | Some(n) => n
  | None => 0
  }
} else {
  try {
    raise(Not_found)
  } catch {
  | Not_found => -1
  }
}

// For/while loops
for i in 0 to 10 {
  Js.log(i)
}

while false {
  ()
}

// All declaration types
type variant = A | B(int) | C({name: string})
module M = { let x = 1 }
external log: string => unit = "console.log"
open Belt
exception Fail(string)

// Decorators
@genType @deprecated("old")
let old = 1

// Numeric literals
let _ = 42
let _ = 0xFF
let _ = 0b1010
let _ = 0o77
let _ = 3.14
let _ = 1.5e10

// String variants
let _ = "regular string"
let _ = `template ${result->Int.toString}`
let _ = 'c'

// Operators
let _ = 1 + 2 * 3 / 4 - 5
let _ = 1.0 +. 2.0 *. 3.0
let _ = "a" ++ "b"
let _ = true && false || !true
let _ = 1 == 2 && 3 !== 4
let _ = [1, 2, 3]->Array.map(x => x + 1)

// Extension points
let _ = %raw(`1 + 1`)
let _ = %re("/test/g")
