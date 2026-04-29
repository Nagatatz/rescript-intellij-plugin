// Shared chrome wrapping every Inertia page. `usePage()` exposes server-sent
// props on every navigation so the layout can react to the active route.
@react.component
let make = (~children) => {
  let page = InertiaBindings.usePage()

  <main style={{maxWidth: "640px", margin: "2rem auto", fontFamily: "sans-serif"}}>
    <header
      style={{display: "flex", gap: "1rem", borderBottom: "1px solid #ddd", paddingBottom: "1rem"}}>
      <strong> {React.string("ReScript + Hono + Inertia")} </strong>
      <nav style={{display: "flex", gap: "0.75rem", marginLeft: "auto"}}>
        <InertiaBindings.Link href="/"> {React.string("Home")} </InertiaBindings.Link>
        <InertiaBindings.Link href="/about"> {React.string("About")} </InertiaBindings.Link>
      </nav>
    </header>
    <section style={{padding: "1.5rem 0"}}> children </section>
    <footer style={{borderTop: "1px solid #ddd", paddingTop: "0.75rem", fontSize: "0.8rem"}}>
      {React.string(`Current path: ${page.url}`)}
    </footer>
  </main>
}
