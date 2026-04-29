@react.component
let make = (~name: string) => {
  <section>
    <h1> {React.string("Welcome to " ++ name)} </h1>
    <p>
      {React.string(
        "This block is rendered statically — Astro emits the HTML and ships zero JS for it.",
      )}
    </p>
  </section>
}
