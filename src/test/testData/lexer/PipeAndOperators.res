// Pipe operators and chaining patterns

// Pipe first (->)
let result1 = [1, 2, 3]->Array.map(x => x + 1)->Array.filter(x => x > 2)

// Pipe last (|>)
let result2 = [1, 2, 3] |> Array.map(_, x => x + 1)

// Long pipe chain
let result3 =
  [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
  ->Array.map(x => x * 2)
  ->Array.filter(x => x > 5)
  ->Array.reduce(0, (acc, x) => acc + x)

// Pipe with method calls
let result4 = str->String.trim->String.toLowerCase->String.split(" ")

// Comparison operators
let _ = 1 == 2
let _ = 1 === 2
let _ = 1 != 2
let _ = 1 !== 2
let _ = 1 < 2
let _ = 1 <= 2
let _ = 1 > 2
let _ = 1 >= 2

// Arithmetic operators
let _ = 1 + 2
let _ = 1 - 2
let _ = 1 * 2
let _ = 1 / 2
let _ = 5 mod 3
let _ = 1.0 +. 2.0
let _ = 1.0 -. 2.0
let _ = 1.0 *. 2.0
let _ = 1.0 /. 2.0

// String operators
let _ = "hello" ++ " " ++ "world"

// Boolean operators
let _ = true && false
let _ = true || false
let _ = !true

// Bitwise operators
let _ = lor(1, 2)
let _ = land(3, 1)
let _ = lxor(5, 3)
let _ = lsl(1, 3)
let _ = lsr(8, 2)

// Ref operators
let r = ref(0)
let _ = r.contents
let _ = r := 1

// Arrow function
let f = x => x + 1
let g = (x, y) => x + y
let h = (~name, ~age=0, ()) => `${name}: ${age->Int.toString}`

// Ternary
let _ = true ? "yes" : "no"

// Spread
let arr = [1, ...otherArr]
let rec_ = {...baseRecord, name: "new"}
