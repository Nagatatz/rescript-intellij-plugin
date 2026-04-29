// Server Component: renders to HTML on the server, ships zero JS.
@react.component
let make = (~name: string) => {
  <section>
    <h1> {React.string("Hello, " ++ name ++ "!")} </h1>
    <p> {React.string("Greet.res is a Server Component written in ReScript.")} </p>
  </section>
}
