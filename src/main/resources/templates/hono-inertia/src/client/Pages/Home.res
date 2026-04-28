// Home page. Server hands a `{title, message}` object via `c->HonoInertia.render`
// in `Routes.res`. The shape is checked at runtime through Inertia's protocol.
type props = {
  title: string,
  message: string,
}

@react.component
let make = (~title, ~message) => {
  let _ = ({title, message}: props)

  <MainLayout>
    <h1> {React.string(title)} </h1>
    <p> {React.string(message)} </p>
    <p>
      <InertiaBindings.Link href="/about">
        {React.string("Read about the stack →")}
      </InertiaBindings.Link>
    </p>
  </MainLayout>
}
