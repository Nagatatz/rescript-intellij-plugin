// Basic ReScript code for highlighting integration test
let greeting = "hello"
let count = 42
let pi = 3.14

type color = Red | Green | Blue

module Utils = {
  let add = (a, b) => a + b
  let name = "utils"
}

external setTimeout: (unit => unit, int) => unit = "setTimeout"

open Belt

// JSX component
let make = () => {
  <div className="app">
    <h1> {React.string("Hello")} </h1>
  </div>
}

/* Block comment */
let _ = list{1, 2, 3}->List.map(x => x * 2)
