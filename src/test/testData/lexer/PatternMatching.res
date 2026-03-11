// Pattern matching edge cases

type shape = Circle(float) | Rectangle(float, float) | Triangle(float, float, float)
type result<'a, 'e> = Ok('a) | Error('e)

// Basic switch
let basic = switch x {
| 1 => "one"
| 2 => "two"
| _ => "other"
}

// Variant patterns
let shapeArea = switch shape {
| Circle(r) => Js.Math._PI *. r *. r
| Rectangle(w, h) => w *. h
| Triangle(a, b, c) => {
    let s = (a +. b +. c) /. 2.0
    Js.Math.sqrt(s *. (s -. a) *. (s -. b) *. (s -. c))
  }
}

// Nested patterns
let nested = switch (a, b) {
| (Some(1), Some(2)) => "both"
| (Some(_), None) => "first only"
| (None, Some(_)) => "second only"
| (None, None) => "neither"
}

// Or patterns
let orPattern = switch x {
| 1 | 2 | 3 => "small"
| _ => "large"
}

// Guard patterns
let guarded = switch x {
| n if n > 0 => "positive"
| n if n < 0 => "negative"
| _ => "zero"
}

// Record patterns
let recordMatch = switch person {
| {name: "Alice", age} => `Alice is ${age->Int.toString}`
| {name, age: 0} => `${name} is a baby`
| {name, age} => `${name} is ${age->Int.toString}`
}

// List patterns
let listMatch = switch xs {
| list{} => "empty"
| list{x} => `single: ${x}`
| list{x, y, ...rest} => `first: ${x}, second: ${y}`
}

// Array patterns
let arrayMatch = switch arr {
| [] => "empty"
| [x] => `single: ${x}`
| [x, y] => `pair: ${x}, ${y}`
| _ => "many"
}

// Exception pattern
let withException = switch {
| exception Not_found => "not found"
| result => result
}

// Polymorphic variant patterns
let polyMatch = switch color {
| #red => "red"
| #green => "green"
| #blue => "blue"
| #rgb(r, g, b) => `rgb(${r}, ${g}, ${b})`
}

// As pattern
let asPattern = switch x {
| (_, _) as pair => pair
}
