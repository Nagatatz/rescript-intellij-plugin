// About page. Demonstrates how to receive an array prop from the server.
@react.component
let make = (~title, ~stack: array<string>) => {
  <MainLayout>
    <h1> {React.string(title)} </h1>
    <p> {React.string("This template stitches together:")} </p>
    <ul>
      {stack
      ->Array.mapWithIndex((tech, i) =>
        <li key={Int.toString(i)}> {React.string(tech)} </li>
      )
      ->React.array}
    </ul>
    <p>
      <InertiaBindings.Link href="/"> {React.string("← Back home")} </InertiaBindings.Link>
    </p>
  </MainLayout>
}
