// Type Impact Preview fixture.
//
// Open the "ReScript Type Impact" tool window on the right, then place
// caret on the `user` type below. The panel should list every usage
// in this file and `VariantUsage.res` classified by reference kind:
// type-ref (`u: user`, `: array<user>`), constructor (`{ id, name }`
// is a record literal — won't classify as constructor here), and
// field-access (`u.name`, `u.id`).

type user = {
  id: int,
  name: string,
}

let alice: user = {id: 1, name: "Alice"}
let bob: user = {id: 2, name: "Bob"}

let greet = (u: user) => "Hello, " ++ u.name
let idOf = (u: user) => u.id

let allUsers: array<user> = [alice, bob]
let userCount: array<user> => int = Array.length
