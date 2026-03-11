// Type expression patterns for lexer testing

// Basic types
type t1 = int
type t2 = float
type t3 = string
type t4 = bool
type t5 = unit
type t6 = char

// Optional type
type t7 = option<int>
type t8 = option<option<string>>

// Array and list types
type t9 = array<int>
type t10 = list<string>

// Tuple types
type t11 = (int, string)
type t12 = (int, string, bool)

// Function types
type t13 = int => string
type t14 = (int, string) => bool
type t15 = (~name: string, ~age: int=?, unit) => string

// Record types
type person = {
  name: string,
  age: int,
  mutable score: float,
  address: option<string>,
}

// Variant types
type color = Red | Green | Blue
type shape =
  | Circle({radius: float})
  | Rectangle({width: float, height: float})
  | Point

// Polymorphic variant types
type cssColor = [
  | #red
  | #green
  | #blue
  | #hex(string)
  | #rgb(int, int, int)
]

// Generic types
type container<'a> = {value: 'a, label: string}
type result<'ok, 'err> = Ok('ok) | Error('err)
type tree<'a> = Leaf | Node(tree<'a>, 'a, tree<'a>)

// Recursive types
type rec jsonValue =
  | JsonNull
  | JsonBool(bool)
  | JsonNumber(float)
  | JsonString(string)
  | JsonArray(array<jsonValue>)
  | JsonObject(array<(string, jsonValue)>)

// Type constraints
type stringMap<'v> = Belt.Map.String.t<'v>

// Abstract type
type opaque

// Extensible variant
type event = ..
type event += Click | Hover(int, int)
