module Outer = {
  module Inner = {
    let x = 1
  }
}

let f = () => {
  let a = 1
  switch a {
  | 1 => {
      let b = 2
      b + 1
    }
  | _ => 0
  }
}

type t = {
  name: string,
  age: int,
}

if true {
  let x = 1
  if x > 0 {
    Js.log("positive")
  }
}
