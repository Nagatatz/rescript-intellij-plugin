// Complex structure view test

let topLevelLet = 1

type color = Red | Green | Blue

module Utils = {
  let utilFunc = () => ()
  type config = {debug: bool}

  module Logging = {
    let info = msg => Js.log(msg)
    let error = msg => Js.log2("ERROR:", msg)
  }
}

external parseInt: string => int = "parseInt"

exception NotFound
exception InvalidArg(string)

module type Printable = {
  type t
  let toString: t => string
}

module IntPrintable: Printable with type t = int = {
  type t = int
  let toString = Int.toString
}

open Belt

let rec fib = n => n <= 1 ? n : fib(n - 1) + fib(n - 2)

type rec tree<'a> = Leaf | Node(tree<'a>, 'a, tree<'a>)

include Belt.Array

module Deeply = {
  module Nested = {
    module Structure = {
      let value = 42
    }
  }
}
