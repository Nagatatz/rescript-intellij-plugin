// All top-level declaration types for parser testing

// Let bindings
let simple = 1
let withType: int = 1
let destructured = {name: "Alice", age: 30}
let {name, age} = destructured
let (a, b) = (1, 2)
let rec fibonacci = n => n <= 1 ? n : fibonacci(n - 1) + fibonacci(n - 2)

// Type declarations
type point = {x: float, y: float}
type color = Red | Green | Blue
type result<'a, 'e> = Ok('a) | Error('e)
type rec tree<'a> = Leaf | Node(tree<'a>, 'a, tree<'a>)
type callback = (~name: string, ~age: int=?, unit) => unit

// Module declarations
module Empty = {}
module WithContent = {
  let x = 1
  type t = int
}
module Alias = Belt.Array
module Rec = {
  module type S = {
    type t
    let make: unit => t
  }
}

// External declarations
external parseInt: string => int = "parseInt"
external createElement: (string, 'props) => React.element = "createElement"
@module("path") external join: (string, string) => string = "join"

// Open declarations
open Belt
open Belt.Array

// Include declarations
// include Belt.Array

// Exception declarations
exception NotFound
exception InvalidInput(string)
exception HttpError({code: int, message: string})
