// Verification sample for `addUndefinedRecordFields` LSP code action.
// Place caret on the literal `{name: "Ada"}` and trigger Alt+Enter.
// Expected: quick fix that inserts `age` and `email` placeholders.
type user = {name: string, age: int, email: string}

let u: user = {name: "Ada"}
