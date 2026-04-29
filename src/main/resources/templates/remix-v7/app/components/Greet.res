@react.component
let make = (~name: string) => {
  <section>
    <h1> {React.string("Hello, " ++ name ++ "!")} </h1>
    <p> {React.string("Greet.res renders this heading from ReScript.")} </p>
  </section>
}
