// Type Coverage Heat Map fixture.
//
// Open the bottom "ReScript Type Coverage" tool window. This file
// should rank LOW (red, < 30%) when sorted by coverage ascending,
// because most top-level `let`s below have no explicit annotation.
// Adding `: T` annotations should move the file up the table.

let inferredOne = 1
let inferredTwo = "two"
let inferredList = [1, 2, 3]
let inferredFn = x => x + 1
let inferredPair = (1, "one")
let inferredRecord = {"key": "value"}

let annotatedOne: int = 1
let annotatedFn: int => int = x => x * 2
