@react.component
let make = (~name: string) => {
  <section>
    <h1> {React.string("Hello, " ++ name ++ "!")} </h1>
    <p> {React.string("This component is written in ReScript.")} </p>
  </section>
}
