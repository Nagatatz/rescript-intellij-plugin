let x = 1

<fold text='module ... { ... }'>module MyModule = {
  let a = 1
  let b = 2
}</fold>

<fold text='/* ... */'>/* This is a
   block comment */</fold>

type t = {name: string, age: int}

<fold text='module ... { ... }'>module Nested = {
  <fold text='module ... { ... }'>module Inner = {
    let value = 42
  }</fold>
}</fold>
