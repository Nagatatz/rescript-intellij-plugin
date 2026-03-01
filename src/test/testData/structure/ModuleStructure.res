let topLevel = 1

type color = Red | Green | Blue

module Utils = {
  let helper = () => ()
  type config = {debug: bool}

  module Inner = {
    let nested = "hello"
  }
}

external log: string => unit = "console.log"

exception NotFound
