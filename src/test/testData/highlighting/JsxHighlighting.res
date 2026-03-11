// JSX highlighting integration test

module App = {
  @react.component
  let make = (~title, ~children) => {
    let (count, setCount) = React.useState(() => 0)

    <div className="app">
      <h1> {React.string(title)} </h1>
      <button onClick={_ => setCount(prev => prev + 1)}>
        {React.string(`Count: ${count->Int.toString}`)}
      </button>
      <> {children} </>
      {count > 10
        ? <span className="warning"> {React.string("High count!")} </span>
        : React.null}
    </div>
  }
}
