// Client Component body. ReScript cannot emit a `"use client"` directive at
// the top of a `.res.mjs` file, so the boundary is declared in the thin
// CounterClient.tsx wrapper that re-exports this module's `make` function.
@react.component
let make = (~initial: int) => {
  let (count, setCount) = React.useState(_ => initial)
  <div style={ReactDOM.Style.make(~marginTop="1rem", ())}>
    <h2> {React.string("Interactive Client Component")} </h2>
    <p> {React.string("Count: " ++ Belt.Int.toString(count))} </p>
    <button onClick={_ => setCount(prev => prev + 1)}>
      {React.string("Increment")}
    </button>
  </div>
}
