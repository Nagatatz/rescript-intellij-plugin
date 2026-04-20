// Server-rendered component (no state, no client-only APIs).
@genType @react.component
let make = () => {
  <section>
    <h1> {React.string("Welcome to {{projectName}}")} </h1>
    <p>
      {React.string("This block is a Server Component. " ++ "The form below is a Client Component.")}
    </p>
  </section>
}