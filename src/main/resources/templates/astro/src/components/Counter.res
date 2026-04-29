@react.component
let make = (~initial: int) => {
  let (count, setCount) = React.useState(_ => initial)
  <div className="counter-island">
    <h2> {React.string("Interactive Island")} </h2>
    <p> {React.string("Count: " ++ Belt.Int.toString(count))} </p>
    <button onClick={_ => setCount(prev => prev + 1)}>
      {React.string("Increment")}
    </button>
  </div>
}
