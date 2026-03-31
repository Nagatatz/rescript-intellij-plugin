// JSX component demo for ReScript + React

module Button = {
  @react.component
  let make = (~label: string, ~onClick: unit => unit, ~disabled=false) => {
    <button onClick={_ => onClick()} disabled>
      {React.string(label)}
    </button>
  }
}

module Badge = {
  @react.component
  let make = (~count: int, ~color: string) => {
    <span style={ReactDOM.Style.make(~backgroundColor=color, ())}>
      {React.int(count)}
    </span>
  }
}

module App = {
  @react.component
  let make = () => {
    let (count, setCount) = React.useState(() => 0)
    let (name, setName) = React.useState(() => "World")

    <div className="app">
      <h1> {React.string(`Hello, ${name}!`)} </h1>
      <Badge count color="#4CAF50" />
      <input
        type_="text"
        value={name}
        onChange={e => {
          let value = (e->ReactEvent.Form.target)["value"]
          setName(_ => value)
        }}
      />
      <Button
        label={`Clicked ${count->Int.toString} times`}
        onClick={() => setCount(c => c + 1)}
      />
    </div>
  }
}
