// Server-rendered component (no state, no client-only APIs).
//
// `serverGeneratedAt` is produced inside the async Server Component
// `app/page.tsx` and threaded down as a prop. It demonstrates the
// canonical RSC pattern: data fetched on the server reaches client
// markup without any browser-side waterfall.
@genType @react.component
let make = (~serverGeneratedAt: string) => {
  <section>
    <h1> {React.string("Welcome to {{projectName}}")} </h1>
    <p>
      {React.string("This block is a Server Component. " ++ "The form below is a Client Component.")}
    </p>
    <p style={{color: "#666", fontSize: "0.875rem"}}>
      {React.string("Rendered on the server at " ++ serverGeneratedAt)}
    </p>
  </section>
}
