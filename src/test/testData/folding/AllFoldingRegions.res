// Test all folding region types

<fold text='module ... { ... }'>module FoldableModule = {
  let x = 1
  let y = 2
  let z = 3
}</fold>

<fold text='/* ... */'>/* This is a
   multi-line block
   comment */</fold>

<fold text='/** ... */'>/**
 * This is a doc comment
 * that spans multiple lines
 */</fold>

type record = <fold text='{ ... }'>{
  name: string,
  age: int,
  address: string,
}</fold>

let func = () => <fold text='{ ... }'>{
  let a = 1
  let b = 2
  a + b
}</fold>

<fold text='module ... { ... }'>module Nested = {
  <fold text='module ... { ... }'>module Inner = {
    let value = 42
  }</fold>
  let outer = 1
}</fold>

switch x <fold text='{ ... }'>{
| 1 => "one"
| 2 => "two"
| _ => "other"
}</fold>

//<fold text='#region Region Name'>
//#region Region Name
let regionContent1 = 1
let regionContent2 = 2
//#endregion</fold>
