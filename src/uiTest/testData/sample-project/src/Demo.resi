type user = {
  name: string,
  age: int,
  email: option<string>,
}

type role =
  | Admin
  | Editor
  | Viewer({readOnly: bool})

let greet: user => string
let permissionLevel: role => int
let users: array<user>
let result: string
let admins: array<user>

module UserUtils: {
  let findByName: (array<user>, string) => option<user>
  let countWithEmail: array<user> => int
}
