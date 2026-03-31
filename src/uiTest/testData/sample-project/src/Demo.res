// ReScript IntelliJ Plugin — Feature Demo

type user = {
  name: string,
  age: int,
  email: option<string>,
}

type role =
  | Admin
  | Editor
  | Viewer({readOnly: bool})

/** Greets a user with a personalized message. */
let greet = (user: user): string => {
  let greeting = `Hello, ${user.name}!`
  switch user.email {
  | Some(email) => `${greeting} (${email})`
  | None => greeting
  }
}

/** Returns the permission level for a given role. */
let permissionLevel = (role: role): int =>
  switch role {
  | Admin => 3
  | Editor => 2
  | Viewer(_) => 1
  }

let users: array<user> = [
  {name: "Alice", age: 30, email: Some("alice@example.com")},
  {name: "Bob", age: 25, email: None},
  {name: "Charlie", age: 35, email: Some("charlie@example.com")},
]

let result = users->Array.map(greet)->Array.join(", ")

let admins = users->Array.filter(u => u.age > 28)

module UserUtils = {
  /** Finds a user by name from the given list. */
  let findByName = (users: array<user>, name: string): option<user> =>
    users->Array.find(u => u.name == name)

  /** Counts users with email addresses. */
  let countWithEmail = (users: array<user>): int =>
    users->Array.filter(u => u.email->Option.isSome)->Array.length
}
