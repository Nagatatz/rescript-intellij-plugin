// Module system patterns for lexer testing

// Simple module
module M = {
  let x = 1
  type t = int
}

// Module type
module type Comparable = {
  type t
  let compare: (t, t) => int
}

// Constrained module
module IntComparable: Comparable with type t = int = {
  type t = int
  let compare = (a, b) => a - b
}

// Functor
module MakeSet = (Item: Comparable) => {
  type t = list<Item.t>
  let empty: t = list{}
  let add = (item, set) => list{item, ...set}
}

// Module alias
module A = Belt.Array
module O = Belt.Option

// First-class module
let comparator = module(IntComparable: Comparable with type t = int)

// Nested modules
module Outer = {
  module Middle = {
    module Inner = {
      let deepValue = 42
    }
  }
}

// Open in module
module WithOpen = {
  open Belt
  let mapped = [1, 2, 3]->Array.map(x => x + 1)
}

// Recursive module
module rec Even: {
  let check: int => bool
} = {
  let check = n =>
    switch n {
    | 0 => true
    | n => Odd.check(n - 1)
    }
}
and Odd: {
  let check: int => bool
} = {
  let check = n =>
    switch n {
    | 0 => false
    | n => Even.check(n - 1)
    }
}

// Include
module Extended = {
  include M
  let y = x + 1
}

// Unpack
let module(C: Comparable with type t = int) = comparator
