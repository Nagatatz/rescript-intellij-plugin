// Variant Flow Diagram fixture (purely syntactic — no LSP needed).
//
// Open the "ReScript Switch Flow" tool window on the right, then move
// the caret into the `switch` body. The diagram should render four
// branches (North / South / East / West). The toolbar exposes
// Copy Mermaid / Copy DOT.

type direction =
  | North
  | South
  | East
  | West

let opposite = (d: direction) =>
  switch d {
  | North => South
  | South => North
  | East => West
  | West => East
  }

// Nested switch — flow diagram should still trace the inner branches.
type traffic =
  | Red
  | Yellow
  | Green

let stepLight = (light: traffic, blink: bool) =>
  switch light {
  | Red => Green
  | Yellow =>
    switch blink {
    | true => Red
    | false => Yellow
    }
  | Green => Yellow
  }
