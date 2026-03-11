// All ReScript token types for comprehensive lexer testing

// Keywords
let x = 1
type t = int
module M = {}
external e: int = "e"
open Belt
include Belt
exception Exn
if true { () } else { () }
switch x { | 1 => () }
for i in 0 to 10 { () }
while true { () }
try { () } catch { | Exn => () }
as and assert constraint downto lazy mutable private rec sig struct unpack when

// Literals
let _int = 42
let _negInt = -1
let _hex = 0xFF
let _oct = 0o77
let _bin = 0b1010
let _float = 3.14
let _floatE = 1.5e10
let _string = "hello world"
let _templateString = `hello ${name}`
let _char = 'a'
let _bool = true
let _boolF = false
let _unit = ()

// Operators
let _ = 1 + 2
let _ = 1 - 2
let _ = 1 * 2
let _ = 1 / 2
let _ = 1 == 2
let _ = 1 === 2
let _ = 1 != 2
let _ = 1 !== 2
let _ = 1 < 2
let _ = 1 > 2
let _ = 1 <= 2
let _ = 1 >= 2
let _ = true && false
let _ = true || false
let _ = !true
let _ = list{1, 2}->List.map(x => x + 1)
let _ = x |> f
let _ = x->f
let _ = ref(1) := 2

// Decorators
@module("fs")
external readFile: string => string = "readFileSync"

@genType
let exported = 42

@val @scope("console")
external log: string => unit = "log"

// JSX
let make = () => {
  <div className="app">
    <span> {React.string("hi")} </span>
    <br />
    <> <span /> </>
  </div>
}

// Comments
// Line comment
/* Block comment */
/** Doc comment */
/***
 * Multi-line doc comment
 */

// Pattern matching
let result = switch x {
| 1 => "one"
| 2 | 3 => "two or three"
| _ => "other"
}

// Records and variants
type person = {name: string, age: int, mutable score: float}
type shape =
  | Circle(float)
  | Rectangle(float, float)
  | Point

// Polymorphic variants
type color = [#red | #green | #blue]

// Module types and functors
module type Printable = {
  type t
  let toString: t => string
}

// Extension points
let raw = %raw(`console.log("hello")`)
let re = %re("/hello/g")
