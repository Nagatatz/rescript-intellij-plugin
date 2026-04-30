// Home page. Server hands `{title, message}` via `c->HonoInertia.render` in
// `Routes.res`; the labeled args below double as the page's typed props.
@react.component
let make = (~title, ~message) =>
  <MainLayout>
    <h1> {React.string(title)} </h1>
    <p> {React.string(message)} </p>
    <p>
      <InertiaBindings.Link href="/about">
        {React.string("Read about the stack →")}
      </InertiaBindings.Link>
    </p>
  </MainLayout>
