// Decorator patterns for lexer testing

// Simple decorators
@genType
let exported = 1

@deprecated("Use newFunc instead")
let oldFunc = () => ()

// Module decorators
@module("fs")
external readFile: string => string = "readFileSync"

@module("path")
external join: (string, string) => string = "join"

// Scope decorators
@val @scope("console")
external log: string => unit = "log"

@val @scope(("window", "document"))
external getElementById: string => Dom.element = "getElementById"

// Property decorators
@get external length: array<'a> => int = "length"
@set external setTitle: (Dom.element, string) => unit = "title"
@send external push: (array<'a>, 'a) => unit = "push"

// Inline decorators
@inline let maxSize = 100

// Unboxed
@unboxed type stringOrInt = String(string) | Int(int)

// As decorator
type event = {
  @as("type") type_: string,
  target: Dom.element,
}

// Return decorator
@return(nullable)
external querySelector: string => option<Dom.element> = "querySelector"

// Variadic
@variadic @send
external log2: (Dom.element, array<string>) => unit = "log"

// New
@new external createDate: unit => Js.Date.t = "Date"

// Multiple decorators on one item
@genType @deprecated("old") @inline
let oldConstant = 42

// Decorator with complex payload
@module("react") @scope("default")
external useState: 'a => ('a, ('a => 'a) => unit) = "useState"
